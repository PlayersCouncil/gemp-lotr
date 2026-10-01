package com.gempukku.lotro.framework;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.game.state.GameExtraInfo;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.PackBox;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.packs.ProductLibraryPackOpener;
import com.gempukku.lotro.virtualtable.VirtualTable;
import org.junit.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The unit test rig: a virtual table with two made-up players, where every card the tester names is put in both
 * players' decks so that either side can reach it.  All of the table's actual machinery lives in VirtualTable and the
 * interfaces it implements (see com.gempukku.lotro.virtualtable); what is here is only what makes it a test rig.
 */
public class VirtualTableScenario extends VirtualTable implements TestConstants, AdHocEffects {

    protected static LotroCardBlueprintLibrary _cardLibrary;
    protected static LotroFormatLibrary _formatLibrary;
    protected static ProductLibrary _productLibrary;

    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
    }

    public static LotroCardBlueprint FindCard(String bpid) throws CardNotFoundException {
        return _cardLibrary.getLotroCardBlueprint(bpid);
    }

    public static LotroFormat FindFormat(String code) throws CardNotFoundException {
        return _formatLibrary.getFormat(code);
    }

    public static PackBox FindProduct(String name) throws CardNotFoundException {
        return _productLibrary.GetProduct(name);
    }

    private final ActionContext _freepsFilterContext = new DefaultActionContext(P1, _game, null, null, null);
    private final ActionContext _shadowFilterContext = new DefaultActionContext(P2, _game, null, null, null);

    public ActionContext FreepsFilterContext() { return _freepsFilterContext; }
    public ActionContext ShadowFilterContext() { return _shadowFilterContext; }

    /*
     * The two players at a test table are always the same made-up pair.
     */
    @Override
    public String P1() { return P1; }
    @Override
    public String P2() { return P2; }

    /**
     * In a test, a failed sanity check inside the rig is a test failure like any other.
     */
    @Override
    public void Verify(boolean condition, String message) {
        Assert.assertTrue(message, condition);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, null, null, null, Multipath, null, null, null);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs, HashMap<String, String> siteIDs, String ringBearerID, String ringID) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, Multipath, null,null, null);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, format, null, null, null);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String freepsMetaSiteId, String shadowMetaSiteId) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, Multipath, freepsMetaSiteId, shadowMetaSiteId, null);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String freepsMetaSiteId, String shadowMetaSiteId,
            Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, Multipath, freepsMetaSiteId, shadowMetaSiteId, bidHandler);
    }

    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format,
            String freepsMetaSiteId, String shadowMetaSiteId,
            Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, format, freepsMetaSiteId, shadowMetaSiteId, bidHandler, null);
    }

    /**
     * @param playerPlacements Race to Mount Doom league placements per player (keyed by P1 / P2), or else null.
     *                         Only meaningful when a meta-site id was supplied.
     */
    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format,
            String freepsMetaSiteId, String shadowMetaSiteId,
            Consumer<VirtualTableScenario> bidHandler,
            Map<String, RTMDGameInfo.LeaguePlacement> playerPlacements) throws CardNotFoundException, DecisionResultInvalidException {

        if(siteIDs == null ) {
            siteIDs = KingSites;
        }

        if(ringBearerID == null ) {
            ringBearerID = MDFrodo;
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

        InitializeGameWithDecks(decks, format, freepsMetaSiteId, shadowMetaSiteId, playerPlacements);

        //We want to handle this at this time before the game start process because certain game properties aren't
        // available until the player order has been determined.
        if (bidHandler != null) {
            bidHandler.accept(this);
        } else {
            BidAndSeatPlayers();
        }

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

        // Create and place meta-site modifiers in SUPPORT zone if provided
        if (freepsMetaSiteId != null) {
            var freepsMod = _gameState.getMetaSites(P1).getFirst();
            Cards.get(P1).put("mod", (PhysicalCardImpl)freepsMod);
        }
        if (shadowMetaSiteId != null) {
            var shadowMod = _gameState.getMetaSites(P2).getFirst();
            Cards.get(P2).put("mod", (PhysicalCardImpl)shadowMod);
        }
    }


    /**
     * Starts up a game of LOTR-TCG with the given decks and format.  This is used internally but may have use in certain
     * complicated test scenarios.  The vast majority of the time you do not need this.
     * @param decks A map of decks for each player in the game; key is the player name.
     * @param formatName Name of the format this table should be following.
     * @param freepsMetaSiteId Blueprint ID of the Race to Mount Doom meta-sites used by player 1, or else null
     * @param shadowMetaSiteId Blueprint ID of the Race to Mount Doom meta-sites used by player 2, or else null
     */
    public void InitializeGameWithDecks(Map<String, LotroDeck> decks, String formatName, String freepsMetaSiteId, String shadowMetaSiteId) {
        InitializeGameWithDecks(decks, formatName, freepsMetaSiteId, shadowMetaSiteId, null);
    }

    /**
     * Starts up a game of LOTR-TCG with the given decks and format.  This is used internally but may have use in certain
     * complicated test scenarios.  The vast majority of the time you do not need this.
     * @param decks A map of decks for each player in the game; key is the player name.
     * @param formatName Name of the format this table should be following.
     * @param freepsMetaSiteId Blueprint ID of the Race to Mount Doom meta-sites used by player 1, or else null
     * @param shadowMetaSiteId Blueprint ID of the Race to Mount Doom meta-sites used by player 2, or else null
     * @param playerPlacements Race to Mount Doom league placements per player (keyed by P1 / P2), or else null
     */
    public void InitializeGameWithDecks(Map<String, LotroDeck> decks, String formatName, String freepsMetaSiteId, String shadowMetaSiteId,
            Map<String, RTMDGameInfo.LeaguePlacement> playerPlacements) {
        var format = _formatLibrary.getFormat(formatName);

        GameExtraInfo info = null;
        if(freepsMetaSiteId != null || shadowMetaSiteId != null) {
            var playerMetaSites = new HashMap<String, List<RTMDGameInfo.MetaSitePair>>();
            if(freepsMetaSiteId != null) {
                playerMetaSites.put(P1, Collections.singletonList(new RTMDGameInfo.MetaSitePair("90_1", freepsMetaSiteId)));
            }
            if(shadowMetaSiteId != null) {
                playerMetaSites.put(P2, Collections.singletonList(new RTMDGameInfo.MetaSitePair("90_1", shadowMetaSiteId)));
            }
            info = new RTMDGameInfo(playerMetaSites, playerPlacements);
        }

        InitializeGame(format, decks, _cardLibrary, info);
        _game.setPackOpener(new ProductLibraryPackOpener(_productLibrary));
    }

    /**
     * When testing the live mid-game booster pack opening, we obviously want to be able to have deterministic
     * booster pack results, which requires that we override the default pack opener with a custom one.
     */
    public void UseTestBoosters() {
        _game.setPackOpener(new TestPackOpener(_productLibrary));
    }

}
