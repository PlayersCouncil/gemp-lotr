package com.gempukku.lotro.framework;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;

import java.util.HashMap;
import java.util.Map;

public class VirtualTableScenario implements TestBase, Actions, AdHocEffects, CardProperties, Choices, Decisions,
        GameProcedures, GameProperties, PileProperties, Skirmishes, ZoneManipulation {

    public static LotroCardBlueprintLibrary _cardLibrary;
    public static LotroFormatLibrary _formatLibrary;
    public static ProductLibrary _productLibrary;

    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
    }

    // Player key, then name/card
    private Map<String, Map<String, PhysicalCardImpl>> Cards = new HashMap<>();
    private DefaultLotroGame _game;
    private GameState _gameState;
    private DefaultUserFeedback _userFeedback;
    private final ActionContext _freepsFilterContext = new DefaultActionContext(P1, _game, null, null, null);
    private final ActionContext _shadowFilterContext = new DefaultActionContext(P2, _game, null, null, null);

    /*
     * These functions are all used by the various interfaces and not so much in tests or in this class itself.
     */
    public DefaultLotroGame game() { return _game; }
    public GameState gameState() { return _gameState; }
    public DefaultUserFeedback userFeedback() { return _userFeedback; }
    public ActionContext FreepsFilterContext() { return _freepsFilterContext; }
    public ActionContext ShadowFilterContext() { return _shadowFilterContext; }

    public VirtualTableScenario(HashMap<String, String> cardIDs) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, null, null, null, Multipath);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs, HashMap<String, String> siteIDs, String ringBearerID, String ringID) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, Multipath);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format) throws CardNotFoundException, DecisionResultInvalidException {

        if(siteIDs == null ) {
            siteIDs = KingSites;
        }

        if(ringBearerID == null ) {
            ringBearerID = FOTRFrodo;
        }

        if(ringID == null) {
            ringID = RulingRing;
        }

        Map<String, LotroDeck> decks = new HashMap<>();
        decks.put(P1, new LotroDeck(P1 + "'s deck"));
        decks.put(P2, new LotroDeck(P2 + "'s deck"));

        // Strictly speaking, the names don't matter all that much, but in the event that the tester wants to retrieve
        // a specific card from deck by name, then if there are any duplicates the one returned will be random, which
        // can lead to stochastic tests that randomly fail.

        decks.put(P1, new LotroDeck(P1));
        decks.put(P2, new LotroDeck(P2));

        for(String name : siteIDs.keySet()) {
            String id = siteIDs.get(name);
            decks.get(P1).addSite(id);
            decks.get(P2).addSite(id);
        }

        for(String name : cardIDs.keySet()) {
            String id = cardIDs.get(name);
            decks.get(P1).addCard(id);
            decks.get(P2).addCard(id);
        }

        // Now that all the helper parameters have been stuffed into the decklist, we now populate an actual deck for each player.

        decks.get(P1).setRingBearer(ringBearerID);
        decks.get(P2).setRingBearer(ringBearerID);

        decks.get(P1).setRing(ringID);
        decks.get(P2).setRing(ringID);

        InitializeGameWithDecks(decks, format);

        //We want to handle this at this time before the game start process because certain game properties aren't
        // available until the player order has been determined.
        BidAndSeatPlayers();


        Cards.put(P1, new HashMap<>());
        Cards.put(P2, new HashMap<>());

        // Now that the game has been initialized, we reset any automatic drawing that was performed as part of startup
        for(var card : _gameState.getHand(P2).stream().toList()) {
            MoveCardsToTopOfDeck((PhysicalCardImpl)card);
        }

        // Next we associate all the physically-instantiated cards with the human-readable names they were given by the
        // tester.  This will now permit us to nab the exact card from anywhere without searching or collisions.
        if(cardIDs != null) {
            for(var card : _game.getGameState().getHand(P1).stream().toList()) {
                MoveCardsToTopOfDeck((PhysicalCardImpl)card);
            }

            for (var card : _game.getGameState().getDeck(P1)) {
                String name = cardIDs.entrySet()
                        .stream()
                        .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P1).containsKey(x.getKey()))
                        .map(Map.Entry::getKey)
                        .findFirst().get();

                Cards.get(P1).put(name, (PhysicalCardImpl) card);
            }

            for(var card : _game.getGameState().getHand(P2).stream().toList()) {
                MoveCardsToTopOfDeck((PhysicalCardImpl)card);
            }

            for (var card : _game.getGameState().getDeck(P2)) {
                String name = cardIDs.entrySet()
                        .stream()
                        .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P2).containsKey(x.getKey()))
                        .map(Map.Entry::getKey)
                        .findFirst().get();

                Cards.get(P2).put(name, (PhysicalCardImpl) card);
            }
        }

        if(siteIDs != null) {
            for (var card : _game.getGameState().getAdventureDeck(P1)) {
                String name = null;

                if(cardIDs != null) {
                    name = cardIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P1).containsKey(x.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                }

                if(name == null) {
                    name = siteIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()))
                            .map(Map.Entry::getKey)
                            .findFirst().get();
                }

                Cards.get(P1).put(name, (PhysicalCardImpl) card);
            }

            for (var card : _game.getGameState().getAdventureDeck(P2)) {
                String name = null;

                if(cardIDs != null) {
                    name = cardIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P2).containsKey(x.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                }

                if(name == null) {
                    name = siteIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()))
                            .map(Map.Entry::getKey)
                            .findFirst().get();
                }

                Cards.get(P2).put(name, (PhysicalCardImpl) card);
            }
        }
    }

    /**
     * Returns a card from the Free Peoples player's deck by its human-readable test alias.
     * @param cardName The human-readable name assigned at the top of each test class.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetFreepsCard(String cardName) { return Cards.get(P1).get(cardName); }
    /**
     * Returns a card from the Shadow player's deck by its human-readable test alias.
     * @param cardName The human-readable name assigned at the top of each test class.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetShadowCard(String cardName) { return Cards.get(P2).get(cardName); }
    /**
     * Returns a given player's card by its human-readable test alias.
     * @param player The player to look up a card for.
     * @param cardName The human-readable name assigned at the top of each test class.
     * @return The physical card that was instantiated for the game.
     */
    public PhysicalCardImpl GetCard(String player, String cardName) { return Cards.get(player).get(cardName); }
    public PhysicalCardImpl GetFreepsCardByID(String id) { return GetCardByID(P1, Integer.parseInt(id)); }
    public PhysicalCardImpl GetFreepsCardByID(int id) { return GetCardByID(P1, id); }
    public PhysicalCardImpl GetShadowCardByID(String id) { return GetCardByID(P2, Integer.parseInt(id)); }
    public PhysicalCardImpl GetShadowCardByID(int id) { return GetCardByID(P2, id); }
    public PhysicalCardImpl GetCardByID(String player, int id) {
        return Cards.get(player).values().stream()
                .filter(x -> x.getCardId() == id)
                .findFirst().orElse(null);
    }

    /**
     * Starts up a game of LOTR-TCG with the given decks and format.  This is used internally but may have use in certain
     * complicated test scenarios.  The vast majority of the time you do not need this.
     * @param decks A map of decks for each player in the game; key is the player name.
     * @param formatName Name of the format this table should be following.
     * @throws DecisionResultInvalidException
     */
    public void InitializeGameWithDecks(Map<String, LotroDeck> decks, String formatName) throws DecisionResultInvalidException {
        _userFeedback = new DefaultUserFeedback();

        var format = _formatLibrary.getFormat(formatName);

        _game = new DefaultLotroGame(format, decks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        _gameState = _game.getGameState();
    }

    /**
     * Handles the bidding and seating placement of both players, then removes the burdens that were bid for consistency.
     */
    public void BidAndSeatPlayers() throws DecisionResultInvalidException {
        FreepsDecided("1");
        PlayerDecided(P2, "0");

        // Seating choice
        PlayerDecided(P1, "0");
    }

    /**
     * Passes through certain setup steps at the start of the game so our test may begin at the Free Peoples player's
     * Fellowship phase.  Resets the hand so that the only cards in hand are those the tester defines manually
     * before calling this function.
     * @throws DecisionResultInvalidException
     */
    public void StartGame() throws DecisionResultInvalidException {
        StartGame(true);
    }

    /**
     * Passes through certain setup steps at the start of the game so our test may begin at the Free Peoples player's
     * Fellowship phase.  Auto-selects the provided site as the first site 1; this is only useful when using the
     * Shadow path.  Will reset the hand back to replace the initial drawn hand.
     * @param site1 The site that Free Peoples should start on.
     * @throws DecisionResultInvalidException
     */
    public void StartGame(PhysicalCardImpl site1) throws DecisionResultInvalidException {
        FreepsChooseCardBPFromSelection(site1);
        StartGame(true);
    }

    /**
     * Passes through certain setup steps at the start of the game so our test may begin at the Free Peoples player's
     * Fellowship phase.
     * @param resetHand If true, any cards drawn at the start of the game will be placed back on top of the draw deck,
     *                  ensuring that each player only has the cards in their hand that the tester manually places
     *                  before calling StartGame.  This ensures that there are no confounding variables.
     *                  If false, the default drawn hand will remain untouched.
     * @throws DecisionResultInvalidException
     */
    public void StartGame(boolean resetHand) throws DecisionResultInvalidException {
        var freepsHand = GetFreepsHand().stream().toList();
        var shadowHand = GetShadowHand().stream().toList();

        SkipStartingFellowships();

        // As a convenience, we want the tester to be able to stack their hand and other piles before the game begins.
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
    }

    /**
     * When both players are asked to select a starting fellowship, this can be used to skip past the prompt and choose
     * 0 companions to go down (since you can cheat in any companions you may need for the test scenario anyway).
     */
    public void SkipStartingFellowships() throws DecisionResultInvalidException {
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
    public void SkipMulligans() throws DecisionResultInvalidException {
        if(FreepsDecisionAvailable("Do you wish to mulligan")) {
            FreepsChooseNo();
        }
        if(ShadowDecisionAvailable("Do you wish to mulligan")) {
            ShadowChooseNo();
        }
    }

    /**
     * Low-level function used by the rest of the test rig to return a decision result back to the server.  This is the
     * beating heart of what is essentially a headless client.  You do not need to call this manually during tests.
     * @param player The player making the decision
     * @param answer What decision is being returned to the server
     * @throws DecisionResultInvalidException If there is any mismatch between what the server is expecting and your
     * answer, this test will fail.
     */
    public void PlayerDecided(String player, String answer) throws DecisionResultInvalidException {
        var decision = userFeedback().getAwaitingDecision(player);
        userFeedback().participantDecided(player);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            userFeedback().sendAwaitingDecision(player, decision);
            throw exp;
        }
        game().carryOutPendingActionsUntilDecisionNeeded();
    }




}
