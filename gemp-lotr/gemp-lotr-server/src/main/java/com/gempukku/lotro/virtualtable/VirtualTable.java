package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameExtraInfo;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A game of LOTR-TCG with a headless client attached to both seats.  Everything here is the part of that which does
 * not care who is using it: holding the game, answering its decisions, walking it through the opening procedure, and
 * looking cards up by the human-readable names they were given.
 *
 * Who the two players are, which cards go in whose deck, and how those cards get their names is left to the
 * subclass.  The unit test rig (VirtualTableScenario, in the test sources) is one such subclass.
 */
public abstract class VirtualTable implements TableBase, Actions, CardProperties, Choices, Decisions,
        GameProcedures, GameProperties, PileProperties, Skirmishes, ZoneManipulation {

    // Player key, then name/card
    protected final Map<String, Map<String, PhysicalCardImpl>> Cards = new HashMap<>();
    protected DefaultLotroGame _game;
    protected GameState _gameState;
    protected DefaultUserFeedback _userFeedback;

    /*
     * These functions are all used by the various interfaces and not so much by users of the table or in this class
     * itself.
     */
    public DefaultLotroGame game() { return _game; }
    public GameState gameState() { return _gameState; }
    public DefaultUserFeedback userFeedback() { return _userFeedback; }

    /**
     * Returns a card from the Free Peoples player's deck by its human-readable alias.
     * @param cardName The human-readable name the card was given when the table was set up.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetFreepsCard(String cardName) {
        var card =  Cards.get(P1()).get(cardName);
        if(card == null) {
            throw new NullPointerException("Card '" + cardName + "' does not exist for Free Peoples.");
        }
        return card;
    }
    /**
     * Returns a card from the Shadow player's deck by its human-readable alias.
     * @param cardName The human-readable name the card was given when the table was set up.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetShadowCard(String cardName) {
        var card =  Cards.get(P2()).get(cardName);
        if(card == null) {
            throw new NullPointerException("Card '" + cardName + "' does not exist for Shadow.");
        }
        return card;
    }
    /**
     * Returns a given player's card by its human-readable alias.
     * @param player The player to look up a card for.
     * @param cardName The human-readable name the card was given when the table was set up.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetCard(String player, String cardName) { return Cards.get(player).get(cardName); }
    public PhysicalCardImpl GetFreepsCardByID(String id) { return GetCardByID(P1(), Integer.parseInt(id)); }
    public PhysicalCardImpl GetFreepsCardByID(int id) { return GetCardByID(P1(), id); }
    public PhysicalCardImpl GetShadowCardByID(String id) { return GetCardByID(P2(), Integer.parseInt(id)); }
    public PhysicalCardImpl GetShadowCardByID(int id) { return GetCardByID(P2(), id); }
    public PhysicalCardImpl GetCardByID(String player, int id) {
        return Cards.get(player).values().stream()
                .filter(x -> x.getCardId() == id)
                .findFirst().orElse(null);
    }

    /**
     * Starts up a game of LOTR-TCG with the given decks and format, and runs it up to the first decision (the bid).
     * @param format The format this table should be following.
     * @param decks A map of decks for each player in the game; key is the player name.
     * @param library The card library that the decks' blueprints come from.
     * @param info Anything extra the format needs to know about this particular game (e.g. Race to Mount Doom
     *             meta-sites), or else null.
     */
    protected void InitializeGame(LotroFormat format, Map<String, LotroDeck> decks, LotroCardBlueprintLibrary library, GameExtraInfo info) {
        CreateGame(format, decks, library, info);
        _game.startGame();
    }

    /**
     * Builds the game without setting it going, for a caller that has to hand the game to something else first (a
     * live game's mediator, say).  The caller starts it with game().startGame().
     */
    protected void CreateGame(LotroFormat format, Map<String, LotroDeck> decks, LotroCardBlueprintLibrary library, GameExtraInfo info) {
        _userFeedback = new DefaultUserFeedback();

        _game = new DefaultLotroGame(format, decks, _userFeedback, library, TimerDescription(), SpectatorsAllowed(), GameName(), info);
        _userFeedback.setGame(_game);
        _gameState = _game.getGameState();
        BeforeGameStarts();
    }

    /** What the players are told the game is, before it starts. */
    protected String GameName() { return "Test Match"; }
    protected String TimerDescription() { return "No timer"; }
    protected boolean SpectatorsAllowed() { return false; }

    /**
     * Called once the game exists but before it has started running, for anything that has to be in place from the
     * very first step (e.g. where the game gets its random outcomes from).  Does nothing by default.
     */
    protected void BeforeGameStarts() { }

    private final List<PhysicalCardImpl> _createdCards = new ArrayList<>();

    /**
     * Registers a callback on the game state that captures any cards created dynamically
     * mid-game (e.g. from booster pack opening). Call this before the effect that creates cards.
     * Retrieved cards can be accessed via {@link #GetCreatedCards()} or {@link #GetLastCreatedCard()}.
     */
    public void StartCardCapture() {
        _createdCards.clear();
        _gameState.setCardCreationCallback(card -> _createdCards.add((PhysicalCardImpl) card));
    }

    /**
     * Stops capturing dynamically created cards and clears the callback.
     */
    public void StopCardCapture() {
        _gameState.setCardCreationCallback(null);
    }

    /**
     * Returns all cards captured since the last {@link #StartCardCapture()} call.
     */
    public List<PhysicalCardImpl> GetCreatedCards() {
        return Collections.unmodifiableList(_createdCards);
    }

    /**
     * Returns the most recently captured card, or null if none were captured.
     */
    public PhysicalCardImpl GetLastCreatedCard() {
        return _createdCards.isEmpty() ? null : _createdCards.getLast();
    }

    /**
     * Handles the bidding and seating placement of both players, then removes the burdens that were bid for consistency.
     */
    public void BidAndSeatPlayers() {
        FreepsDecided("1");
        ShadowDecided( "0");

        // Seating choice
        FreepsDecided("0");
    }

    /**
     * Passes through certain setup steps at the start of the game so play may begin at the Free Peoples player's
     * Fellowship phase.  Resets the hand so that the only cards in hand are those placed there manually
     * before calling this function.
     */
    public void StartGame() {
        StartGame(true);
    }

    /**
     * Passes through certain setup steps at the start of the game so play may begin at the Free Peoples player's
     * Fellowship phase.  Auto-selects the provided site as the first site 1; this is only useful when using the
     * Shadow path.  Will reset the hand back to replace the initial drawn hand.
     * @param site1 The site that Free Peoples should start on.
     */
    public void StartGame(PhysicalCardImpl site1) {
        FreepsChooseCardBPFromSelection(site1);
        StartGame(true);
    }

    /**
     * Passes through certain setup steps at the start of the game so play may begin at the Free Peoples player's
     * Fellowship phase.
     * @param resetHand If true, any cards drawn at the start of the game will be placed back on top of the draw deck,
     *                  ensuring that each player only has the cards in their hand that were manually placed there
     *                  before calling StartGame.  This ensures that there are no confounding variables.
     *                  If false, the default drawn hand will remain untouched.
     */
    public void StartGame(boolean resetHand) {
        StartGame(resetHand, true);
    }

    /**
     * Passes through certain setup steps at the start of the game so play may begin at the Free Peoples player's
     * Fellowship phase.
     * @param resetHand If true, any cards drawn at the start of the game will be placed back on top of the draw deck.
     * @param skipStartingFellowships If true, auto-skips the starting fellowship selection for both players.
     *                                 If false, the caller must handle starting fellowship selection manually before
     *                                 this method proceeds to mulligans.
     */
    public void StartGame(boolean resetHand, boolean skipStartingFellowships) {
        var freepsHand = GetFreepsHand().stream().toList();
        var shadowHand = GetShadowHand().stream().toList();

        if (skipStartingFellowships) {
            SkipStartingFellowships();
        }

        // As a convenience, we want the hand and other piles to be stackable before the game begins.
        // However, since a new hand will be drawn, this tramples over the careful stacking, so we will reset the
        // state of the deck + hand to what they were before the card draw.
        if(resetHand) {
            for(var card : GetFreepsHand().stream().toList().reversed()) {
                if(!freepsHand.contains(card)) {
                    MoveCardsToTopOfDeck((PhysicalCardImpl) card);
                }
            }

            for(var card : GetShadowHand().stream().toList().reversed()) {
                if(!shadowHand.contains(card)) {
                    MoveCardsToTopOfDeck((PhysicalCardImpl) card);
                }
            }
        }

        SkipMulligans();
        //Get rid of the bidded burden so that nobody has to remember it exists all the time
        RemoveBurdens(1);
    }

    /**
     * When both players are asked to select a starting fellowship, this can be used to skip past the prompt and choose
     * 0 companions to go down (since you can cheat in any companions you may need anyway).
     */
    public void SkipStartingFellowships() {
        if(FreepsDecisionAvailable("Starting fellowship")) {
            FreepsChoose("");
        }
        if(ShadowDecisionAvailable("Starting fellowship")) {
            ShadowChoose("");
        }
    }

    /**
     * When both players are prompted to mulligan their hand, this can be used to choose "no" for both players.
     */
    public void SkipMulligans() {
        if(FreepsDecisionAvailable("Do you wish to mulligan")) {
            FreepsChooseNo();
        }
        if(ShadowDecisionAvailable("Do you wish to mulligan")) {
            ShadowChooseNo();
        }
    }

    /**
     * Low-level function used by the rest of the virtual table to return a decision result back to the server.  This
     * is the beating heart of what is essentially a headless client.  You do not need to call this manually.
     * @param player The player making the decision
     * @param answer What decision is being returned to the server
     */
    public void PlayerDecided(String player, String answer) {
        var decision = userFeedback().getAwaitingDecision(player);
        userFeedback().participantDecided(player);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            userFeedback().sendAwaitingDecision(player, decision);
            userFeedback().atRest();
            throw new RuntimeException(exp);
        }
        userFeedback().decisionAccepted(player, decision, answer);
        game().carryOutPendingActionsUntilDecisionNeeded();
    }
}
