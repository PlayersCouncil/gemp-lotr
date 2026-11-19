package com.gempukku.lotro.game;

import com.gempukku.lotro.bots.AssignmentLegality;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.timing.processes.pregame.PlayerPlaysStartingFellowshipGameProcess;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A game at a mediator, with two players who answer at random and somebody counting how it ends. */
class MediatorSitting {
    static final String FORMAT = "fotr_block";
    static final List<String> PLAYERS = List.of("Alice", "Bob");

    final LotroGameMediator mediator;
    final Map<String, RandomDecisionBot> bots = new LinkedHashMap<>();
    final List<String> endings = new ArrayList<>();
    /** How the table stood each time an answer was about to be given, by how many entries the journal then held. */
    final Map<Integer, String> boardAt = new LinkedHashMap<>();

    MediatorSitting() {
        this(Boards.ARAGORN_STARTER, Boards.GANDALF_STARTER);
    }

    /**
     * @param aliceDeck Alice's deck, as DeckSerialization reads one: ring-bearer|ring|sites|the rest.
     * @param bobDeck Bob's.
     */
    MediatorSitting(String aliceDeck, String bobDeck) {
        LotroDeck alice = DeckSerialization.buildDeckFromContents("Alice's", aliceDeck, FORMAT, "Alice's");
        LotroDeck bob = DeckSerialization.buildDeckFromContents("Bob's", bobDeck, FORMAT, "Bob's");
        LotroGameParticipant[] participants = {new LotroGameParticipant("Alice", alice), new LotroGameParticipant("Bob", bob)};
        mediator = new LotroGameMediator("1", LoadedLibraries._formatLibrary.getFormat(FORMAT), participants, LoadedLibraries._cardLibrary,
                new GameTimer(false, "Test", 60 * 80, 60 * 5), true, false, false, "Casual", new MarkdownParser(), null, false, null, null);
        for (String player : PLAYERS)
            bots.put(player, new RandomDecisionBot(player));
        mediator.addGameResultListener(new GameResultListener() {
            @Override
            public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                endings.add(winnerPlayerId + " won: " + winReason);
            }

            @Override
            public void gameCancelled() {
                endings.add("cancelled");
            }
        });
    }

    String asked() {
        for (String player : PLAYERS)
            if (mediator.awaitingDecision(player) != null)
                return player;
        return null;
    }

    /**
     * Has whoever is being asked answer, that many times over or until the game ends.
     * @return How many answers were given.
     */
    int play(int answers) {
        int given = 0;
        while (given < answers && !mediator.isFinished()) {
            String player = asked();
            if (player == null)
                break;
            AwaitingDecision decision = mediator.awaitingDecision(player);
            String answer = bots.get(player).chooseAction(mediator.getGame().getGameState(), decision, new AssignmentLegality(mediator.getGame()));
            boardAt.put(mediator.journalSize(), board());
            //The bots' door: it takes an answer by name, and puts a refused one right
            mediator.botAnswered(player, decision.getAwaitingDecisionId(), answer);
            given++;
        }
        return given;
    }

    String board() {
        return Boards.of(mediator.getGame());
    }

    /** What each player is being asked, as the journal would know it. */
    String waiting() {
        StringBuilder sb = new StringBuilder();
        for (String player : PLAYERS) {
            AwaitingDecision decision = mediator.awaitingDecision(player);
            sb.append(player).append('=').append(decision == null ? "-" : GameJournal.fingerprint(decision)).append(' ');
        }
        return sb.toString();
    }

    /** Gives a player's answer to what they are being asked. */
    void answer(String player, String answer) {
        boardAt.put(mediator.journalSize(), board());
        mediator.botAnswered(player, mediator.awaitingDecision(player).getAwaitingDecisionId(), answer);
    }

    /** The first value of one of the parameters of what a player is being asked. */
    String[] parameter(String player, String name) {
        return mediator.awaitingDecision(player).getDecisionParameters().get(name);
    }

    String other(String player) {
        return player.equals(PLAYERS.get(0)) ? PLAYERS.get(1) : PLAYERS.get(0);
    }

    boolean asked(String player, String textStart) {
        AwaitingDecision decision = mediator.awaitingDecision(player);
        return decision != null && decision.getText().startsWith(textStart);
    }

    /** Starts the game and answers the bids and the choice of who goes first the plainest way. */
    String toStartingFellowships() {
        mediator.startGame();
        for (int i = 0; i < 20; i++) {
            for (String player : PLAYERS)
                if (asked(player, "Starting fellowship"))
                    return player;
            answer(asked(), "0");
        }
        throw new AssertionError("The game never came to its starting fellowships");
    }

    /**
     * Has the player choosing a starting fellowship take the first card on offer.  The fellowship is not complete
     * after it even where nothing more can be added: the player is then asked only to press DONE (see pressDone).
     */
    void pick(String player) {
        answer(player, parameter(player, "cardId")[0]);
    }

    /**
     * @return Whether the player is being asked only to press DONE: nothing more can be added to the starting
     * fellowship being chosen, and the decision carries PlayerPlaysStartingFellowshipGameProcess.CONFIRM_ONLY.
     */
    boolean askedToPressDone(String player) {
        AwaitingDecision decision = mediator.awaitingDecision(player);
        if (decision == null)
            return false;
        String[] marked = decision.getDecisionParameters().get(PlayerPlaysStartingFellowshipGameProcess.CONFIRM_ONLY);
        return marked != null && List.of(marked).contains("true");
    }

    /** Has a player who is asked only to press DONE (see askedToPressDone) press it, which completes the fellowship. */
    void pressDone(String player) {
        if (!askedToPressDone(player))
            throw new AssertionError(player + " is not being asked only to press DONE: " + waiting());
        answer(player, "");
    }

    /**
     * Both starting fellowships finished without another card, up to the first question about a mulligan: each
     * player still choosing presses DONE, whether there is more that could be added or not.
     */
    String toMulligans() {
        for (int i = 0; i < 20; i++) {
            for (String player : PLAYERS)
                if (asked(player, "Do you wish to mulligan"))
                    return player;
            answer(asked(), "");
        }
        throw new AssertionError("The game never came to its mulligans");
    }

    /**
     * From the start of the game to the first decision of the first turn, nobody taking a mulligan.
     * @return Who is asked it.
     */
    String toFirstTurn() {
        toStartingFellowships();
        String first = toMulligans();
        answer(first, "0");
        answer(other(first), "0");
        return asked();
    }

    /**
     * @return The action, among those a player is being offered, that plays a card which is laid on the table and
     * stays there (a companion, a possession and the like: nothing that draws or reveals); null if there is none.
     */
    String playOfACardThatStays(String player) {
        AwaitingDecision decision = mediator.awaitingDecision(player);
        if (decision == null || decision.getDecisionType() != AwaitingDecisionType.CARD_ACTION_CHOICE)
            return null;
        String[] actions = parameter(player, "actionId");
        String[] cards = parameter(player, "cardId");
        String[] texts = parameter(player, "actionText");
        for (int i = 0; i < actions.length; i++) {
            PhysicalCard card = mediator.getGame().getGameState().findCardById(Integer.parseInt(cards[i]));
            if (texts[i].startsWith("Play ") && card != null && STAYS.contains(card.getBlueprint().getCardType()))
                return actions[i];
        }
        return null;
    }

    /** The blueprint of the card an action on offer to a player belongs to. */
    String blueprintOf(String player, String actionId) {
        String[] actions = parameter(player, "actionId");
        for (int i = 0; i < actions.length; i++)
            if (actions[i].equals(actionId))
                //(the decision itself only says "inPlay" for a card the client already has)
                return mediator.getGame().getGameState().findCardById(Integer.parseInt(parameter(player, "cardId")[i])).getBlueprintId();
        throw new AssertionError("No action " + actionId + " is on offer");
    }

    /**
     * Has a recording kept of the game as a player sees it, the way GameRecorder does for a replay: a channel that is
     * nobody's connection.  To be called before the game starts.
     */
    GameCommunicationChannel record(String player) {
        GameCommunicationChannel recording = new GameCommunicationChannel(player, 0, false, mediator.getGame().getFormat());
        mediator.addGameStateListener(player, recording);
        return recording;
    }

    private static final Set<CardType> STAYS = Set.of(CardType.COMPANION, CardType.POSSESSION, CardType.ALLY, CardType.CONDITION, CardType.ARTIFACT);
}
