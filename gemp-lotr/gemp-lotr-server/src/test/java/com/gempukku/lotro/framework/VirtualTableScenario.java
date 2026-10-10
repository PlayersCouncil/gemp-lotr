package com.gempukku.lotro.framework;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.common.SitesBlock;
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
        // The real cards, and beside them the tests' own definitions (see TestCardLibrary).
        _cardLibrary = new TestCardLibrary();
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

    // The account names the two players are seated under: the constants P1 and P2 unless the test chose others.
    private final String _freepsPlayerName;
    private final String _shadowPlayerName;

    private final ActionContext _freepsFilterContext;
    private final ActionContext _shadowFilterContext;

    public ActionContext FreepsFilterContext() { return _freepsFilterContext; }
    public ActionContext ShadowFilterContext() { return _shadowFilterContext; }

    /*
     * The two players at a test table are the same made-up pair, the constants P1 and P2, unless the test seated
     * them under names of its choosing (the constructor that takes the two names).  Everything the table keys by
     * player goes by these two methods, never by the constants.
     */
    @Override
    public String P1() { return _freepsPlayerName; }
    @Override
    public String P2() { return _shadowPlayerName; }

    /**
     * In a test, a failed sanity check inside the rig is a test failure like any other.
     */
    @Override
    public void Verify(boolean condition, String message) {
        Assert.assertTrue(message, condition);
    }

    /**
     * A test table has no client at either seat, so it keeps a channel of its own to each: what a seat has been
     * sent is then there to be read (FreepsHandIsRevealed, ShadowSeesCardInHand and their kin).
     */
    @Override
    protected void BeforeGameStarts() {
        KeepSeatChannels();
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

    /**
     * A table in a format that uses Maps (PCMovie), with each player's Map named.  Both decks still hold the same
     * sites, whatever the Maps say: the game does not hold a deck to its Map, deck validation does.  In a format
     * that does not use Maps the Maps named are in the decks and the game leaves them out.
     * @param freepsMapId Blueprint ID of the Map in player 1's deck, or null for the Map of the sites' path.
     * @param shadowMapId Blueprint ID of the Map in player 2's deck, or null for the Map of the sites' path.
     */
    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format,
            String freepsMapId, String shadowMapId) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, format, freepsMapId, shadowMapId, null, null, null, null, null, null);
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
        this(cardIDs, siteIDs, ringBearerID, ringID, format, null, null, freepsMetaSiteId, shadowMetaSiteId, bidHandler, playerPlacements, null, null);
    }

    /**
     * A table whose players are seated under account names of the test's choosing, for a test of something that
     * reads a player's name (the requirement PlayerIs).  At every other table the two players are P1, "Free Peoples
     * Player", and P2, "Shadow Player".  The names given are the players' names to the game and to everything the
     * table keys by player: P1() and P2() return them, each player's cards, deck and meta-site are his under them,
     * and the Freeps and Shadow methods act for them as at any table.  The constants P1 and P2 are then not the
     * players: a test at such a table names a player with scn.P1() or scn.P2().
     * @param playerPlacements Race to Mount Doom league placements per player, or else null.  Keyed by the names
     *                         the players are seated under.
     * @param freepsPlayerName The account name player 1 is seated under, or null for the usual one (P1).
     * @param shadowPlayerName The account name player 2 is seated under, or null for the usual one (P2).  The game
     *                         tells its players apart by name, so two seats under one name are refused.
     */
    public VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format,
            String freepsMetaSiteId, String shadowMetaSiteId,
            Consumer<VirtualTableScenario> bidHandler,
            Map<String, RTMDGameInfo.LeaguePlacement> playerPlacements,
            String freepsPlayerName, String shadowPlayerName) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, siteIDs, ringBearerID, ringID, format, null, null, freepsMetaSiteId, shadowMetaSiteId, bidHandler, playerPlacements,
                freepsPlayerName, shadowPlayerName);
    }

    /**
     * The constructor every other one comes to.
     * @param freepsMapId Blueprint ID of the Map in player 1's deck, or null.  See the constructor that names Maps.
     * @param shadowMapId Blueprint ID of the Map in player 2's deck, or null.
     * @param playerPlacements Race to Mount Doom league placements per player (keyed by P1() / P2()), or else null.
     * @param freepsPlayerName The account name player 1 is seated under, or null for P1.  See the constructor that
     *                         names the players.
     * @param shadowPlayerName The account name player 2 is seated under, or null for P2.
     */
    private VirtualTableScenario(HashMap<String, String> cardIDs,
            HashMap<String, String> siteIDs,
            String ringBearerID, String ringID,
            String format,
            String freepsMapId, String shadowMapId,
            String freepsMetaSiteId, String shadowMetaSiteId,
            Consumer<VirtualTableScenario> bidHandler,
            Map<String, RTMDGameInfo.LeaguePlacement> playerPlacements,
            String freepsPlayerName, String shadowPlayerName) throws CardNotFoundException, DecisionResultInvalidException {

        _freepsPlayerName = freepsPlayerName != null ? freepsPlayerName : P1;
        _shadowPlayerName = shadowPlayerName != null ? shadowPlayerName : P2;
        if(_freepsPlayerName.equals(_shadowPlayerName)) {
            throw new IllegalArgumentException("Both players cannot be seated under one name (\"" + _freepsPlayerName
                    + "\"): the game tells its players apart by name.");
        }
        // As they have always been made: before the game exists, so neither holds one.
        _freepsFilterContext = new DefaultActionContext(_freepsPlayerName, _game, null, null, null);
        _shadowFilterContext = new DefaultActionContext(_shadowPlayerName, _game, null, null, null);

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
        decks.put(P1(), new LotroDeck(P1() + "'s deck"));
        decks.put(P2(), new LotroDeck(P2() + "'s deck"));

        // Strictly speaking, the names don't matter all that much, but in the event that the tester wants to retrieve
        // a specific card from deck by name, then if there are any duplicates the one returned will be random, which
        // can lead to stochastic tests that randomly fail.

        final HashMap<String, String> siteMap = siteIDs;
        final List<String> siteKeys = SiteKeysInDeckOrder(siteMap);
        for(String name : siteKeys) {
            String id = siteMap.get(name);
            decks.get(P1()).addSite(id);
            decks.get(P2()).addSite(id);
        }

        for(String name : cardIDs.keySet()) {
            String id = cardIDs.get(name);
            decks.get(P1()).addCard(id);
            decks.get(P2()).addCard(id);
        }

        // Now that all the helper parameters have been stuffed into the decklist, we now populate an actual deck for each player.

        decks.get(P1()).setRingBearer(ringBearerID);
        decks.get(P2()).setRingBearer(ringBearerID);

        decks.get(P1()).setRing(ringID);
        decks.get(P2()).setRing(ringID);

        // A format that uses Maps cannot seat a deck that has none.  A Map only announces its deck's site path, so
        // where the tester names none each deck gets the Map of the path its sites are on.
        if(freepsMapId == null && shadowMapId == null) {
            if(_formatLibrary.getFormat(format).usesMaps()) {
                freepsMapId = shadowMapId = MapOfSitePath(siteIDs);
            }
        }
        else {
            if(freepsMapId == null) {
                freepsMapId = MapOfSitePath(siteIDs);
            }
            if(shadowMapId == null) {
                shadowMapId = MapOfSitePath(siteIDs);
            }
        }
        decks.get(P1()).setMap(freepsMapId);
        decks.get(P2()).setMap(shadowMapId);

        RequireASiteToStartAt(decks.get(P1()).getSites(), siteIDs);

        InitializeGameWithDecks(decks, format, freepsMetaSiteId, shadowMetaSiteId, playerPlacements);

        //We want to handle this at this time before the game start process because certain game properties aren't
        // available until the player order has been determined.
        if (bidHandler != null) {
            bidHandler.accept(this);
        } else {
            BidAndSeatPlayers();
        }

        Cards.put(P1(), new HashMap<>());
        Cards.put(P2(), new HashMap<>());

        // Now that the game has been initialized, we reset any automatic drawing that was performed as part of startup
        for(var card : _gameState.getHand(P2()).stream().toList()) {
            MoveCardsToTopOfDeck((PhysicalCardImpl)card);
        }

        // Next we associate all the physically-instantiated cards with the human-readable names they were given by the
        // tester.  This will now permit us to nab the exact card from anywhere without searching or collisions.
        if(cardIDs != null) {
            for(var card : _game.getGameState().getHand(P1()).stream().toList()) {
                MoveCardsToTopOfDeck((PhysicalCardImpl)card);
            }

            for (var card : _game.getGameState().getDeck(P1())) {
                String name = cardIDs.entrySet()
                        .stream()
                        .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P1()).containsKey(x.getKey()))
                        .map(Map.Entry::getKey)
                        .findFirst().get();

                Cards.get(P1()).put(name, (PhysicalCardImpl) card);
            }

            for(var card : _game.getGameState().getHand(P2()).stream().toList()) {
                MoveCardsToTopOfDeck((PhysicalCardImpl)card);
            }

            for (var card : _game.getGameState().getDeck(P2())) {
                String name = cardIDs.entrySet()
                        .stream()
                        .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P2()).containsKey(x.getKey()))
                        .map(Map.Entry::getKey)
                        .findFirst().get();

                Cards.get(P2()).put(name, (PhysicalCardImpl) card);
            }
        }

        if(siteIDs != null) {
            for (var card : _game.getGameState().getAdventureDeck(P1())) {
                String name = null;

                if(cardIDs != null) {
                    name = cardIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P1()).containsKey(x.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                }

                if(name == null) {
                    // Each key names one site: a key already given is passed over, so two keys that hold one
                    // blueprint name its two copies.
                    name = siteKeys.stream()
                            .filter(key -> siteMap.get(key).equals(card.getBlueprintId()) && !Cards.get(P1()).containsKey(key))
                            .findFirst().get();
                }

                Cards.get(P1()).put(name, (PhysicalCardImpl) card);
            }

            for (var card : _game.getGameState().getAdventureDeck(P2())) {
                String name = null;

                if(cardIDs != null) {
                    name = cardIDs.entrySet()
                            .stream()
                            .filter(x -> x.getValue().equals(card.getBlueprintId()) && !Cards.get(P2()).containsKey(x.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                }

                if(name == null) {
                    // Each key names one site: a key already given is passed over, so two keys that hold one
                    // blueprint name its two copies.
                    name = siteKeys.stream()
                            .filter(key -> siteMap.get(key).equals(card.getBlueprintId()) && !Cards.get(P2()).containsKey(key))
                            .findFirst().get();
                }

                Cards.get(P2()).put(name, (PhysicalCardImpl) card);
            }
        }

        // Create and place meta-site modifiers in SUPPORT zone if provided
        if (freepsMetaSiteId != null) {
            var freepsMod = _gameState.getMetaSites(P1()).getFirst();
            Cards.get(P1()).put("mod", (PhysicalCardImpl)freepsMod);
        }
        if (shadowMetaSiteId != null) {
            var shadowMod = _gameState.getMetaSites(P2()).getFirst();
            Cards.get(P2()).put("mod", (PhysicalCardImpl)shadowMod);
        }
    }


    /**
     * The keys of a site map in the order their sites go into each adventure deck: the sites printed with a site
     * number first, then the sites printed with none (the Shadows-block sites), each group in the order the map
     * gives its keys.  The game takes a player's sites as unordered, to be laid by choice, whenever the FIRST card
     * of the adventure deck has no printed number (GameState.usesUnorderedSites, which is asked again at every
     * move), and a HashMap gives its keys in an order no tester chose: a Shadows-block site standing on a numbered
     * path under a key that happened to come out first made the whole path unordered, and the game then rested on
     * "Choose site to play" where a site 1 was expected.  With the numbered sites first a path that has any is laid
     * by number for as long as one is left in the deck; a path of Shadows-block sites alone is as it was.
     * @param siteIDs The site map the tester gave.
     * @return Its keys, numbered sites first.
     */
    private static List<String> SiteKeysInDeckOrder(HashMap<String, String> siteIDs) throws CardNotFoundException {
        List<String> numbered = new java.util.ArrayList<>();
        List<String> unnumbered = new java.util.ArrayList<>();
        for(String name : siteIDs.keySet()) {
            if(_cardLibrary.getLotroCardBlueprint(siteIDs.get(name)).getSiteNumber() != 0) {
                numbered.add(name);
            }
            else {
                unnumbered.add(name);
            }
        }
        numbered.addAll(unnumbered);
        return numbered;
    }

    /**
     * Refuses a set of sites of which the game could lay none as site 1.  The game lays each site by its printed
     * site number unless the first card of the adventure deck has none (GameState.usesUnorderedSites, which goes
     * by the deck and not by the format); deck validation, which a test table does not go through, is what keeps a
     * player's deck to one site path.  With no site printed as site 1 the game's set-up lays nothing and falls
     * over two steps later (a NullPointerException out of CheckForCorruptionGameProcess), with nothing to say why.
     * @param adventureDeck The blueprint IDs of the sites, in the order the game will have them in each
     *                      adventure deck.
     * @param siteIDs The site map the tester gave, for naming what is under the key "site1".
     */
    private static void RequireASiteToStartAt(List<String> adventureDeck, HashMap<String, String> siteIDs) throws CardNotFoundException {
        if(adventureDeck.isEmpty())
            return;
        var first = _cardLibrary.getLotroCardBlueprint(adventureDeck.getFirst());
        if(first.getSiteNumber() == 0)
            return;
        for(String id : adventureDeck) {
            if(_cardLibrary.getLotroCardBlueprint(id).getSiteNumber() == 1)
                return;
        }

        String message = "These sites cannot be set up: the game would lay no site 1.  The first card of the adventure deck ("
                + first.getTitle() + ", " + adventureDeck.getFirst() + ") has a printed site number, so the game lays "
                + "each site by its printed number, and none of these sites is printed as site 1.  (The table puts "
                + "the sites that have a printed number into the adventure deck ahead of those that have none.)";
        String atSite1 = siteIDs.get("site1");
        if(atSite1 != null) {
            var blueprint = _cardLibrary.getLotroCardBlueprint(atSite1);
            message += "  The key \"site1\" holds " + blueprint.getTitle() + " (" + atSite1 + "), which "
                    + (blueprint.getSiteNumber() != 0 ? "is printed as site " + blueprint.getSiteNumber() + "."
                    : "has no printed site number: such a site cannot stand as site 1 of a numbered path (see "
                            + "\"A Shadows-block site path\" in the gemp-testing pitfalls).");
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * The Map of the site path that a set of sites is on, for a deck whose Map the tester did not name.
     * @param siteIDs The sites going into the deck.
     * @return The blueprint ID of that path's Map.
     */
    private static String MapOfSitePath(HashMap<String, String> siteIDs) throws CardNotFoundException {
        var paths = new java.util.HashSet<SitesBlock>();
        for(String id : siteIDs.values()) {
            paths.add(_cardLibrary.getLotroCardBlueprint(id).getSiteBlock());
        }

        if(paths.size() == 1) {
            switch (paths.iterator().next()) {
                case FELLOWSHIP: return FellowshipMap;
                case TWO_TOWERS: return TowersMap;
                case KING: return KingMap;
                default: break;
            }
        }

        throw new IllegalArgumentException("This table's decks need a Map and no Map goes with their sites (" + paths
                + "): there is one for each of the Fellowship, Towers and King paths.  Name each player's Map in the constructor.");
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
     * @param playerPlacements Race to Mount Doom league placements per player (keyed by P1() / P2()), or else null
     */
    public void InitializeGameWithDecks(Map<String, LotroDeck> decks, String formatName, String freepsMetaSiteId, String shadowMetaSiteId,
            Map<String, RTMDGameInfo.LeaguePlacement> playerPlacements) {
        var format = _formatLibrary.getFormat(formatName);

        GameExtraInfo info = null;
        if(freepsMetaSiteId != null || shadowMetaSiteId != null) {
            var playerMetaSites = new HashMap<String, List<RTMDGameInfo.MetaSitePair>>();
            if(freepsMetaSiteId != null) {
                playerMetaSites.put(P1(), Collections.singletonList(new RTMDGameInfo.MetaSitePair("90_1", freepsMetaSiteId)));
            }
            if(shadowMetaSiteId != null) {
                playerMetaSites.put(P2(), Collections.singletonList(new RTMDGameInfo.MetaSitePair("90_1", shadowMetaSiteId)));
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

    /**
     * Has the game's pack opener offer one pack, "Test - Booster", whose contents are exactly the cards given, in
     * the order given, every time it is opened: the pack for a test that needs a card the fixed test pack of
     * UseTestBoosters() does not hold.  The pack is offered and opened as any other (the owner chooses it by its
     * name, the other player is shown its cards, the owner chooses among them); the same blueprint may be given
     * more than once.  A later call, or UseTestBoosters(), takes this pack's place.  Fails if no card is given, or
     * if one of the ids is no card.
     * @param blueprintIds The blueprint ids of the pack's cards, first to last.
     */
    public void UseBoosterOf(String... blueprintIds) {
        Verify(blueprintIds != null && blueprintIds.length > 0, "UseBoosterOf was given no cards: a pack holds at least one.");
        for (String blueprintId : blueprintIds) {
            boolean known;
            try {
                known = blueprintId != null && _cardLibrary.getLotroCardBlueprint(blueprintId) != null;
            } catch (CardNotFoundException notFound) {
                known = false;
            }
            Verify(known, "UseBoosterOf was given '" + blueprintId + "', which is not the blueprint id of any card.");
        }
        _game.setPackOpener(new NamedPackOpener(List.of(blueprintIds)));
    }

}
