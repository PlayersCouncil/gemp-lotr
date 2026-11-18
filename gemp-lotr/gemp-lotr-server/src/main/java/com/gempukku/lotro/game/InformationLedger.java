package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.GameStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps count, as a game goes, of what each player has been shown that they had not been shown before, and of which
 * answer brought each showing about.  With the game's journal that is enough to say what taking the game back to an
 * earlier answer would undo, and for whom: see assess.
 *
 * A player is taken to know a card once they have been shown it, wherever it goes afterwards, until the deck it is in
 * is shuffled.  A card they are shown that they do not know is something learned: a card drawn, a card another
 * player plays or reveals, a card looked at in a deck.  Whether a client is shown a card is read with the same rule
 * the client's own channel uses (GameCommunicationChannel.showsCard).
 *
 * When in doubt this says a player learned something.  Wrongly saying so only means a request has to be agreed to
 * that might have gone through by itself; wrongly saying nothing was learned would let a player look and take back.
 *
 * To keep one for a game: add it to the game as a game state listener and to the game's feedback as a decision
 * observer after the journal, and give the game watching(its random) as its random, all before the game starts.  A
 * game rebuilt from its journal builds its ledger again the same way, by being played.
 */
public class InformationLedger implements GameStateListener, DecisionObserver {
    /** The name this listens to a game under.  Nobody plays under it. */
    public static final String LISTENER_NAME = "~ledger";

    private record Barrier(int cause, String what) { }

    private final List<String> _players;
    private final LotroFormat _format;
    private final GameJournal _journal;
    private LotroGame _game;

    /**
     * The cards each player has been shown and can still tell apart from the others.  The cards themselves, not their
     * IDs: a card is given a new ID as it comes into play, so that nobody can follow it there by number.
     */
    private final Map<String, Set<PhysicalCard>> _known = new HashMap<>();
    /** For each player: the answers (by where they sit in the journal) that showed them something new, soonest first. */
    private final Map<String, List<Integer>> _learnedAt = new HashMap<>();
    private final List<Barrier> _barriers = new ArrayList<>();
    /** Where in the journal the answer sits that the game is now working through; -1 before any answer. */
    private int _cause = -1;

    public InformationLedger(Collection<String> players, LotroFormat format, GameJournal journal) {
        _players = new ArrayList<>(players);
        _format = format;
        _journal = journal;
        for (String player : _players) {
            _known.put(player, Collections.newSetFromMap(new IdentityHashMap<>()));
            _learnedAt.put(player, new ArrayList<>());
        }
    }

    public void setGame(LotroGame game) {
        _game = game;
    }

    /**
     * @return The random to give the game, so that this hears of every shuffle: nobody knows where anything is in a
     * deck that has just been shuffled.
     */
    public GameRandom watching(GameRandom chance) {
        return new GameRandom() {
            @Override
            public void shuffleDeck(String playerId, List<? extends PhysicalCard> deck) {
                chance.shuffleDeck(playerId, deck);
                for (Set<PhysicalCard> known : _known.values())
                    for (PhysicalCard card : deck)
                        known.remove(card);
            }

            @Override
            public List<PhysicalCard> pickCards(String playerId, Zone zone, Collection<? extends PhysicalCard> cards, int count) {
                return chance.pickCards(playerId, zone, cards, count);
            }

            @Override
            public void shufflePlayers(List<String> players) {
                chance.shufflePlayers(players);
            }
        };
    }

    /**
     * Something has happened that no journal can bring back (a booster pack opened): the game cannot be taken back to
     * before it.
     */
    public void barrier(String what) {
        _barriers.add(new Barrier(_cause, what));
    }

    /**
     * @param entryCount How many entries of the journal are kept (see LotroGameMediator.revertTo).
     * @param requester The player who wants the game taken back.
     * @return What that would undo, for that player.
     */
    public RevertAssessment assess(int entryCount, String requester) {
        List<GameJournal.Entry> entries = _journal.entries();
        int othersAnswers = 0;
        boolean shuffled = false;
        boolean picked = false;
        for (int i = Math.max(0, entryCount); i < entries.size(); i++) {
            GameJournal.Entry entry = entries.get(i);
            switch (entry.kind()) {
                case ANSWER -> {
                    if (!entry.player().equals(requester))
                        othersAnswers++;
                }
                case SHUFFLE, ORDER -> shuffled = true;
                case PICK -> picked = true;
                default -> { }
            }
        }
        boolean learned = false;
        List<Integer> learnedAt = _learnedAt.get(requester);
        if (learnedAt == null)
            learned = true;
        else if (!learnedAt.isEmpty() && learnedAt.get(learnedAt.size() - 1) >= entryCount)
            learned = true;

        String barrier = barrierSince(entryCount);

        List<String> reasons = new ArrayList<>();
        if (learned)
            reasons.add(requester + " has seen cards since then that they had not seen before");
        if (picked)
            reasons.add("cards have been picked at random since then");
        if (othersAnswers > 0)
            reasons.add("another player has made " + othersAnswers + (othersAnswers == 1 ? " choice" : " choices") + " since then");
        if (shuffled)
            reasons.add("a deck has been shuffled since then");
        RevertAssessment.Tier tier = learned || picked ? RevertAssessment.Tier.RED
                : othersAnswers > 0 || shuffled ? RevertAssessment.Tier.YELLOW
                : RevertAssessment.Tier.GREEN;
        return new RevertAssessment(tier, reasons, barrier);
    }

    /**
     * @return What stands in the way of taking the game back to the moment its journal held that many entries,
     * whoever agrees to it; null if nothing does.
     */
    public String barrierSince(int entryCount) {
        for (Barrier candidate : _barriers)
            if (candidate.cause() >= entryCount)
                return candidate.what();
        return null;
    }

    private void learned(String player) {
        List<Integer> learnedAt = _learnedAt.get(player);
        if (learnedAt.isEmpty() || learnedAt.get(learnedAt.size() - 1) != _cause)
            learnedAt.add(_cause);
    }

    private void shown(String player, PhysicalCard card) {
        if (_known.get(player).add(card))
            learned(player);
    }

    /* ---- what the game does ---- */

    @Override
    public void cardCreated(PhysicalCard card, boolean overrideOwnerVisibility, boolean forceVisible) {
        for (String player : _players)
            if (GameCommunicationChannel.showsCard(player, card, _format, overrideOwnerVisibility, forceVisible))
                shown(player, card);
    }

    @Override
    public void setSite(PhysicalCard card) {
        //A site laid on the adventure path is announced this way and not as a card created
        for (String player : _players)
            shown(player, card);
    }

    @Override
    public void eventPlayed(PhysicalCard card) {
        //An event is put on everybody's screen as it is played, from wherever it was
        for (String player : _players)
            shown(player, card);
    }

    @Override
    public void decisionAnswering(String playerId, AwaitingDecision decision) {
        //The answer will sit where the journal now ends (GameJournal.decisionAnswering)
        _cause = _journal.entries().size();
    }

    @Override
    public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) { }

    @Override
    public void decisionIssued(String playerId, AwaitingDecision decision) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS || !_known.containsKey(playerId))
            return;
        Collection<? extends PhysicalCard> cards = decision instanceof ArbitraryCardsSelectionDecision arbitrary ? arbitrary.getPhysicalCards() : null;
        if (cards == null) {
            //A list of cards with nothing to say which cards they are or where from: taken to be news
            learned(playerId);
            return;
        }
        boolean fellowships = _game != null && _game.getGameState().getCurrentPhase() == Phase.PLAY_STARTING_FELLOWSHIP;
        for (PhysicalCard card : cards) {
            //A starting fellowship is chosen from the whole of one's own deck, before it has an order: nothing is
            // learned by being offered it.  (Any other look into a deck is, even one's own: which cards are on top.)
            if (fellowships && card.getZone() == Zone.DECK && playerId.equals(card.getOwner()))
                _known.get(playerId).add(card);
            else
                shown(playerId, card);
        }
    }

    /* ---- everything else a game says, none of which shows anybody a card they could not see ---- */

    @Override
    public boolean isLiveConnection() {
        return false;
    }

    @Override
    public String getAssignedPlayerId() {
        return LISTENER_NAME;
    }

    @Override
    public void cardMoved(PhysicalCard card) { }

    @Override
    public void cardsFlipped(Collection<PhysicalCard> cards, boolean down) { }

    @Override
    public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards, boolean forceVisible) { }

    @Override
    public void initializeBoard(List<String> playerIds, boolean discardIsPublic) { }

    @Override
    public void initializePregameBoard(PreGameInfo preGameInfo) { }

    @Override
    public void setPlayerPosition(String playerId, int i) { }

    @Override
    public void setTwilight(int twilightPool) { }

    @Override
    public void setCurrentPlayerId(String playerId, Set<PhysicalCard> inactiveCards) { }

    @Override
    public void setCurrentPhase(String currentPhase) { }

    @Override
    public void addAssignment(PhysicalCard fp, Set<PhysicalCard> minions) { }

    @Override
    public void addPendingAssignment(PhysicalCard fp, PhysicalCard minion) { }

    @Override
    public void removeAssignment(PhysicalCard fp) { }

    @Override
    public void removePendingAssignment(PhysicalCard freePeople, PhysicalCard minion) { }

    @Override
    public void startSkirmish(PhysicalCard fp, Set<PhysicalCard> minions) { }

    @Override
    public void addToSkirmish(PhysicalCard card) { }

    @Override
    public void removeFromSkirmish(PhysicalCard card) { }

    @Override
    public void finishSkirmish() { }

    @Override
    public void addTokens(PhysicalCard card, Token token, int count) { }

    @Override
    public void removeTokens(PhysicalCard card, Token token, int count) { }

    @Override
    public void sendMessage(String message) { }

    @Override
    public void sendGameStats(GameStats gameStats) { }

    @Override
    public void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard) { }

    @Override
    public void cardActivated(String playerPerforming, PhysicalCard card) { }

    @Override
    public void decisionRequired(String playerId, AwaitingDecision awaitingDecision) { }

    @Override
    public void sendWarning(String playerId, String warning) { }

    @Override
    public void endGame() { }
}
