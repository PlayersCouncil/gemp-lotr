package com.gempukku.lotro.prizes;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.*;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.league.LeagueParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Exercises {@link PrizeService} against in-memory prize tables and a mocked collections manager.
 */
public class PrizeServiceTest extends AbstractAtTest {
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 3, 1, 0, 0, 0, 0, DateUtils.UTC);

    private CollectionsManager _collectionsManager;
    private PlayerDAO _playerDAO;
    private CollectionDAO _collectionDAO;
    private LeagueDAO _leagueDAO;
    private LeagueMatchDAO _leagueMatchDAO;
    private InMemoryPlaceholderDAO _placeholderDAO;
    private InMemoryAwardDAO _awardDAO;
    private PrizeService _service;

    @Before
    public void setUp() throws Exception {
        _collectionsManager = Mockito.mock(CollectionsManager.class);
        _playerDAO = Mockito.mock(PlayerDAO.class);
        _collectionDAO = Mockito.mock(CollectionDAO.class);
        _leagueDAO = Mockito.mock(LeagueDAO.class);
        _leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);
        _placeholderDAO = new InMemoryPlaceholderDAO();
        _awardDAO = new InMemoryAwardDAO();
        Mockito.when(_leagueDAO.loadActiveLeagues(Mockito.any())).thenReturn(List.of());
        _service = newService();
    }

    @After
    public void tearDown() {
        // registrations live in the shared library; do not leak them into other tests
        for (String id : new ArrayList<>(_cardLibrary.getRegisteredPlaceholders().keySet()))
            _cardLibrary.unregisterPlaceholder(id);
    }

    private PrizeService newService() {
        return new PrizeService(_collectionsManager, _playerDAO, _collectionDAO, _placeholderDAO, _awardDAO,
                _leagueDAO, _leagueMatchDAO, _cardLibrary, _productLibrary, _formatLibrary, null);
    }

    // ------------------------------------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------------------------------------

    private static PlayerStanding standing(String name, int standing, int points, int games) {
        return new PlayerStanding(name, points, games, games, 0, 0, 0.5f, standing);
    }

    private static PrizeService.EventRef league(String code, String name) {
        return new PrizeService.EventRef(PrizeService.KIND_LEAGUE, code, name, null);
    }

    private static PrizeService.EventRef campaignLeague(String code, String name, String campaign) {
        return new PrizeService.EventRef(PrizeService.KIND_LEAGUE, code, name, campaign);
    }

    /**
     * Standings deliberately handed over unsorted: the service must order them itself.
     */
    private static List<PlayerStanding> field() {
        var standings = new ArrayList<>(List.of(
                standing("p1", 1, 20, 10),
                standing("p2", 2, 18, 10),
                standing("p3", 3, 16, 9),
                standing("p4", 4, 16, 8),   // tied on points with 3rd, lost the tie-break
                standing("p5", 5, 16, 7),   // tied on points with 3rd too
                standing("p6", 6, 14, 3),
                standing("p7", 7, 2, 1),
                standing("p8", 8, 0, 0)));
        Collections.shuffle(standings, new Random(42));
        return standings;
    }

    /**
     * Who got what: player -> every item list delivered to their "My cards" collection.
     */
    private Map<String, List<List<CardCollection.Item>>> deliveries() {
        var players = ArgumentCaptor.forClass(String.class);
        var types = ArgumentCaptor.forClass(CollectionType.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<CardCollection.Item>> items = ArgumentCaptor.forClass(Iterable.class);
        Mockito.verify(_collectionsManager, Mockito.atLeast(0)).addItemsToPlayerCollection(Mockito.eq(true),
                Mockito.anyString(), players.capture(), types.capture(), items.capture());

        Map<String, List<List<CardCollection.Item>>> result = new TreeMap<>();
        for (int i = 0; i < players.getAllValues().size(); i++) {
            assertEquals(CollectionType.MY_CARDS, types.getAllValues().get(i));
            List<CardCollection.Item> list = new ArrayList<>();
            items.getAllValues().get(i).forEach(list::add);
            result.computeIfAbsent(players.getAllValues().get(i), k -> new ArrayList<>()).add(list);
        }
        return result;
    }

    private List<String> reasons() {
        var reasons = ArgumentCaptor.forClass(String.class);
        Mockito.verify(_collectionsManager, Mockito.atLeast(0)).addItemsToPlayerCollection(Mockito.eq(true),
                reasons.capture(), Mockito.anyString(), Mockito.any(CollectionType.class), Mockito.any());
        return reasons.getAllValues();
    }

    private static void assertDelivered(Map<String, List<List<CardCollection.Item>>> deliveries, String player, String... expectedItems) {
        assertTrue(player + " received nothing", deliveries.containsKey(player));
        List<String> got = new ArrayList<>();
        for (var delivery : deliveries.get(player))
            for (var item : delivery)
                got.add(item.getCount() + "x" + item.getBlueprintId());
        assertEquals("items for " + player, List.of(expectedItems), got);
    }

    // ------------------------------------------------------------------------------------------------
    // Placement tiers
    // ------------------------------------------------------------------------------------------------

    @Test
    public void placementAwardsTheRangeAndAnyoneTiedWithTheLastQualifyingPlace() {
        var tiers = List.of(PrizeTier.placement(1, 3, PrizeItem.card("1_1", 2)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);

        var deliveries = deliveries();
        assertEquals(Set.of("p1", "p2", "p3", "p4", "p5"), deliveries.keySet());
        assertDelivered(deliveries, "p4", "2x1_1");

        assertEquals(5, _awardDAO.rows.size());
        var row = _awardDAO.rows.getFirst();
        assertEquals("league", row.event_kind);
        assertEquals("100", row.event_id);
        assertEquals("Spring League", row.event_name);
        assertEquals(Integer.valueOf(0), row.tier_index);
        assertEquals("2x 1_1", row.items);

        for (String reason : reasons())
            assertEquals("Spring League prize: Places 1-3: 2x The One Ring, Isildur's Bane (1_1)", reason);
    }

    @Test
    public void placementTiersBelowTheTopDoNotReachUpwards() {
        var tiers = List.of(PrizeTier.placement(4, 6, PrizeItem.card("1_2", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);
        // p3 is tied with p4/p5 but sits above the tier; p6 is within it
        assertEquals(Set.of("p4", "p5", "p6"), deliveries().keySet());
    }

    @Test
    public void placementBeyondTheFieldAwardsNobody() {
        var tiers = List.of(PrizeTier.placement(9, 12, PrizeItem.card("1_1", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);
        Mockito.verifyNoInteractions(_collectionsManager);
        assertTrue(_awardDAO.rows.isEmpty());

        // a range that runs past the field still awards the places that exist
        var wide = List.of(PrizeTier.placement(7, 12, PrizeItem.card("1_1", 1)));
        _service.awardTiers(league("100", "Spring League"), wide, field(), null);
        assertEquals(Set.of("p7", "p8"), deliveries().keySet());
    }

    @Test
    public void severalTiersAreAwardedIndependently() {
        var tiers = List.of(
                PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1)),
                PrizeTier.placement(2, 3, PrizeItem.card("1_2", 1)),
                PrizeTier.participation(8, PrizeTier.Scope.EVENT, PrizeItem.card("Event Chase Booster", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);

        var deliveries = deliveries();
        assertDelivered(deliveries, "p1", "1x1_1", "1xEvent Chase Booster");
        assertDelivered(deliveries, "p2", "1x1_2", "1xEvent Chase Booster");
        assertDelivered(deliveries, "p3", "1x1_2", "1xEvent Chase Booster");
        assertDelivered(deliveries, "p4", "1x1_2", "1xEvent Chase Booster");
        assertDelivered(deliveries, "p5", "1x1_2");
        assertFalse(deliveries.containsKey("p6"));
    }

    // ------------------------------------------------------------------------------------------------
    // Participation tiers
    // ------------------------------------------------------------------------------------------------

    @Test
    public void participationInTheEventCountsGamesPlayed() {
        var tiers = List.of(PrizeTier.participation(3, PrizeTier.Scope.EVENT, PrizeItem.card("1_2", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);
        assertEquals(Set.of("p1", "p2", "p3", "p4", "p5", "p6"), deliveries().keySet());

        for (String reason : reasons())
            assertEquals("Spring League prize: Participation (3+ games in this event): 1x The One Ring, The Ruling Ring (1_2)", reason);
    }

    @Test
    public void campaignParticipationSumsGamesAcrossTheCampaignAndAwardsOncePerCampaign() throws Exception {
        String campaign = "Yuletide 2026";
        League leagueA = storedLeague("Yule A", 1001, campaign);
        League leagueB = storedLeague("Yule B", 1002, campaign);
        League other = storedLeague("Unrelated", 1003, null);
        Mockito.when(_leagueDAO.loadActiveLeagues(Mockito.any())).thenReturn(List.of(leagueA, leagueB, other));

        // A: alice 2 games, bob 2 games, carol 2 games.  B: alice 1 game, bob 1 game.  Unrelated: carol 5 games.
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1001")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "alice", "bob"),
                new LeagueMatchResult("Serie 1", "carol", "alice"),
                new LeagueMatchResult("Serie 1", "bob", "carol")));
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1002")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "alice", "bob")));
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1003")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "carol", "dave"),
                new LeagueMatchResult("Serie 1", "carol", "dave"),
                new LeagueMatchResult("Serie 1", "carol", "dave"),
                new LeagueMatchResult("Serie 1", "carol", "dave"),
                new LeagueMatchResult("Serie 1", "carol", "dave")));

        var tier = PrizeTier.participation(3, PrizeTier.Scope.CAMPAIGN, PrizeItem.card("1_3", 1));
        tier.label = "Yule regular";

        // League A ends first: alice and bob have 3 campaign games, carol only 2 (her unrelated games do not count)
        var standingsA = List.of(standing("alice", 1, 4, 2), standing("bob", 2, 3, 2), standing("carol", 3, 2, 2));
        _service.awardTiers(campaignLeague("1001", "Yule A", campaign), List.of(tier), standingsA, null);
        assertEquals(Set.of("alice", "bob"), deliveries().keySet());

        var awards = _awardDAO.getAwards(PrizeService.KIND_CAMPAIGN, campaign);
        assertEquals(2, awards.size());
        assertEquals("Yule regular", awards.getFirst().tier_label);
        assertEquals("Yule A", awards.getFirst().event_name);
        assertTrue(_awardDAO.getAwards(PrizeService.KIND_LEAGUE, "1001").isEmpty());

        // carol plays one more game in B, then B ends: only carol is new; alice and bob are not awarded again
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1002")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "alice", "bob"),
                new LeagueMatchResult("Serie 1", "carol", "bob")));
        var standingsB = List.of(standing("alice", 1, 2, 1), standing("carol", 1, 2, 1), standing("bob", 3, 2, 2));
        _service.awardTiers(campaignLeague("1002", "Yule B", campaign), List.of(tier), standingsB, null);

        assertEquals(Set.of("alice", "bob", "carol"), deliveries().keySet());
        assertEquals(1, deliveries().get("alice").size());
        assertEquals(1, deliveries().get("bob").size());
        assertEquals(1, deliveries().get("carol").size());
        assertEquals(3, _awardDAO.getAwards(PrizeService.KIND_CAMPAIGN, campaign).size());
    }

    @Test
    public void campaignTierOnAnEventWithoutACampaignFallsBackToTheEvent() {
        var tier = PrizeTier.participation(8, PrizeTier.Scope.CAMPAIGN, PrizeItem.card("1_3", 1));
        _service.awardTiers(league("100", "Spring League"), List.of(tier), field(), null);
        assertEquals(Set.of("p1", "p2", "p3", "p4"), deliveries().keySet());
        assertEquals(4, _awardDAO.getAwards(PrizeService.KIND_LEAGUE, "100").size());
        Mockito.verifyNoInteractions(_leagueMatchDAO);
    }

    private League storedLeague(String name, long code, String campaign) {
        var params = new LeagueParams();
        params.name = name;
        params.code = code;
        params.start = START.toLocalDateTime();
        params.campaign = campaign;
        params.series.add(new LeagueParams.SerieData("fotr_block", 7, 5));
        return new League(name, 0, code, League.LeagueType.CONSTRUCTED, params.toString(), 0);
    }

    // ------------------------------------------------------------------------------------------------
    // Dedup
    // ------------------------------------------------------------------------------------------------

    @Test
    public void theSameEventTierIsNeverAwardedTwiceToAPlayer() {
        var tiers = List.of(PrizeTier.placement(1, 2, PrizeItem.card("1_1", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);

        var deliveries = deliveries();
        assertEquals(Set.of("p1", "p2"), deliveries.keySet());
        assertEquals(1, deliveries.get("p1").size());
        assertEquals(1, deliveries.get("p2").size());
        assertEquals(2, _awardDAO.rows.size());

        // a fresh service instance reads the log from the table, so a restart does not re-award either
        newService().awardTiers(league("100", "Spring League"), tiers, field(), null);
        assertEquals(2, _awardDAO.rows.size());

        // but the same tier of another event is awarded
        newService().awardTiers(league("200", "Summer League"), tiers, field(), null);
        assertEquals(4, _awardDAO.rows.size());
        assertEquals(2, deliveries().get("p1").size());
    }

    @Test
    public void aFailedDeliveryDoesNotStopTheOtherAwardsOrGetLogged() {
        Mockito.doThrow(new RuntimeException("db down")).when(_collectionsManager)
                .addItemsToPlayerCollection(Mockito.eq(true), Mockito.anyString(), Mockito.eq("p1"), Mockito.any(CollectionType.class), Mockito.any());
        var tiers = List.of(PrizeTier.placement(1, 2, PrizeItem.card("1_1", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, field(), null);

        assertEquals(1, _awardDAO.rows.size());
        assertEquals("p2", _awardDAO.rows.getFirst().player);
    }

    // ------------------------------------------------------------------------------------------------
    // Promises and placeholders
    // ------------------------------------------------------------------------------------------------

    @Test
    public void promisesAreAwardedAsPlaceholdersAndThePlaceholderIsReusedForTheSameKey() throws Exception {
        var tiers = List.of(PrizeTier.placement(1, 1, PrizeItem.promise("2026 WC Champion promo", 2), PrizeItem.card("1_1", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, List.of(standing("p1", 1, 10, 5), standing("p2", 2, 8, 5)), null);

        assertEquals(1, _placeholderDAO.rows.size());
        var placeholder = _placeholderDAO.rows.getFirst();
        int id = placeholder.id;
        assertTrue(id > 0);
        assertEquals("2026 WC Champion promo", placeholder.label);
        assertEquals(2, placeholder.count);
        assertEquals("league", placeholder.event_kind);
        assertEquals("100", placeholder.event_id);
        assertEquals("Spring League", placeholder.event_name);
        assertEquals(Integer.valueOf(0), placeholder.tier_index);
        assertNotNull(placeholder.created);
        assertFalse(placeholder.isResolved());

        String blueprint = PrizeService.placeholderBlueprint(id);
        assertEquals("404_" + id, blueprint);
        assertTrue(PrizeService.isPlaceholder(blueprint));
        assertEquals(id, PrizeService.placeholderId(blueprint));
        assertDelivered(deliveries(), "p1", "2x" + blueprint, "1x1_1");
        assertEquals("2x " + blueprint + "\n1x 1_1", _awardDAO.rows.getFirst().items);

        // the library now renders the placeholder under the promise's name
        var bp = _cardLibrary.getLotroCardBlueprint(blueprint);
        assertEquals("2026 WC Champion promo", bp.getTitle());
        assertEquals("2026 WC Champion promo", bp.getFullName());
        assertEquals(blueprint, bp.getId());
        assertEquals(_cardLibrary.getLotroCardBlueprint(LotroCardBlueprintLibrary.PLACEHOLDER_BASE_ID).getCardType(), bp.getCardType());

        // p1 was tied by p3 in a re-run with a wider field: the same placeholder is handed out, no second row
        _service.awardTiers(league("100", "Spring League"), tiers,
                List.of(standing("p1", 1, 10, 5), standing("p3", 2, 10, 5), standing("p2", 3, 8, 5)), null);
        assertEquals(1, _placeholderDAO.rows.size());
        assertDelivered(deliveries(), "p3", "2x" + blueprint, "1x1_1");

        // the same label in another tier of the same event is the same promise (tiers can be reordered by an edit);
        // a different label is a different one
        var moreTiers = List.of(tiers.getFirst(),
                PrizeTier.placement(2, 2, PrizeItem.promise("2026 WC Champion promo", 1)),
                PrizeTier.placement(3, 3, PrizeItem.promise("2026 WC Finalist promo", 1)));
        _service.awardTiers(league("100", "Spring League"), moreTiers,
                List.of(standing("p1", 1, 10, 5), standing("p2", 2, 8, 5), standing("p4", 3, 6, 5)), null);
        assertEquals(2, _placeholderDAO.rows.size());
        assertEquals("2026 WC Finalist promo", _placeholderDAO.rows.get(1).label);
        assertEquals(Integer.valueOf(2), _placeholderDAO.rows.get(1).tier_index);
    }

    @Test
    public void campaignPromisesShareOnePlaceholderAcrossTheCampaign() throws Exception {
        String campaign = "Yuletide 2026";
        League leagueA = storedLeague("Yule A", 1001, campaign);
        League leagueB = storedLeague("Yule B", 1002, campaign);
        Mockito.when(_leagueDAO.loadActiveLeagues(Mockito.any())).thenReturn(List.of(leagueA, leagueB));
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1001")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "alice", "bob")));
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1002")).thenReturn(List.of());

        var tier = PrizeTier.participation(1, PrizeTier.Scope.CAMPAIGN, PrizeItem.promise("Yule promo", 1));
        _service.awardTiers(campaignLeague("1001", "Yule A", campaign), List.of(tier), List.of(standing("alice", 1, 2, 1)), null);
        assertEquals(Set.of("alice", "bob"), deliveries().keySet());

        // league B ends later with its own games on record
        Mockito.when(_leagueMatchDAO.getLeagueMatches("1002")).thenReturn(List.of(
                new LeagueMatchResult("Serie 1", "carol", "dave")));
        _service.awardTiers(campaignLeague("1002", "Yule B", campaign), List.of(tier), List.of(standing("carol", 1, 2, 1)), null);

        assertEquals(1, _placeholderDAO.rows.size());
        var placeholder = _placeholderDAO.rows.getFirst();
        assertEquals("campaign", placeholder.event_kind);
        assertEquals(campaign, placeholder.event_id);
        assertNull(placeholder.tier_index);
        var deliveries = deliveries();
        assertEquals(Set.of("alice", "bob", "carol", "dave"), deliveries.keySet());
        String blueprint = PrizeService.placeholderBlueprint(placeholder.id);
        assertDelivered(deliveries, "alice", "1x" + blueprint);
        assertDelivered(deliveries, "dave", "1x" + blueprint);
    }

    @Test
    public void unresolvedPlaceholdersAreRegisteredWithTheLibraryAtConstruction() throws Exception {
        var row = new DBDefs.PrizePlaceholder();
        row.label = "Old promise";
        row.event_kind = "manual";
        int id = _placeholderDAO.createPlaceholder(row);
        var resolved = new DBDefs.PrizePlaceholder();
        resolved.label = "Done promise";
        resolved.event_kind = "manual";
        int resolvedId = _placeholderDAO.createPlaceholder(resolved);
        _placeholderDAO.markResolved(resolvedId, "1_1", "admin");

        newService();
        assertEquals("Old promise", _cardLibrary.getLotroCardBlueprint("404_" + id).getTitle());
        assertEquals(LotroCardBlueprintLibrary.PLACEHOLDER_TITLE, _cardLibrary.getLotroCardBlueprint("404_" + resolvedId).getTitle());
        assertEquals(1, _service.getUnresolved().size());
        assertEquals(1, _service.getResolved(10).size());
        assertEquals("Done promise", _service.getResolved(10).getFirst().label);
    }

    @Test
    public void promisesAreRecordedWhenTheEventIsCreatedAndReusedWhenItIsAwarded() throws Exception {
        var tiers = List.of(
                PrizeTier.placement(1, 1, PrizeItem.promise("Champion promo", 1)),
                PrizeTier.placement(2, 4, PrizeItem.promise("Runner-up promo", 1), PrizeItem.card("1_1", 1)));
        var event = league("100", "Spring League");

        _service.registerPromises(event, tiers);
        assertEquals(2, _placeholderDAO.rows.size());
        assertEquals("Champion promo", _placeholderDAO.rows.get(0).label);
        assertEquals("Runner-up promo", _placeholderDAO.rows.get(1).label);
        assertEquals("Champion promo", _cardLibrary.getLotroCardBlueprint("404_" + _placeholderDAO.rows.get(0).id).getTitle());
        assertEquals(2, _service.getUnresolved().size());

        // editing the league again (same promises, one new) adds only the new one
        var edited = new ArrayList<>(tiers);
        edited.add(PrizeTier.participation(3, PrizeTier.Scope.EVENT, PrizeItem.promise("Sticker", 1)));
        _service.registerPromises(event, edited);
        assertEquals(3, _placeholderDAO.rows.size());

        // and the award at the end hands out the rows created up front, not new ones
        _service.awardTiers(event, edited, List.of(standing("p1", 1, 10, 5), standing("p2", 2, 8, 5)), null);
        assertEquals(3, _placeholderDAO.rows.size());
        var deliveries = deliveries();
        assertDelivered(deliveries, "p1", "1x404_" + _placeholderDAO.rows.get(0).id, "1x404_" + _placeholderDAO.rows.get(2).id);
        assertDelivered(deliveries, "p2", "1x404_" + _placeholderDAO.rows.get(1).id, "1x1_1", "1x404_" + _placeholderDAO.rows.get(2).id);
    }

    @Test
    public void aPromiseDroppedFromTheEventIsWithdrawnUnlessHeldOrResolved() throws Exception {
        var event = league("100", "Spring League");
        _service.registerPromises(event, List.of(
                PrizeTier.placement(1, 1, PrizeItem.promise("Kept", 1)),
                PrizeTier.placement(2, 2, PrizeItem.promise("Dropped", 1)),
                PrizeTier.placement(3, 3, PrizeItem.promise("Held", 1)),
                PrizeTier.placement(4, 4, PrizeItem.promise("Resolved", 1))));
        assertEquals(4, _placeholderDAO.rows.size());
        int heldId = _placeholderDAO.rows.get(2).id;
        int resolvedId = _placeholderDAO.rows.get(3).id;
        Mockito.when(_collectionDAO.findHolders(PrizeService.placeholderBlueprint(heldId))).thenReturn(List.of(holder(1, "p1", "permanent", 1)));
        Mockito.when(_collectionDAO.findHolders(PrizeService.placeholderBlueprint(resolvedId))).thenReturn(List.of());
        _service.resolve(resolvedId, "1_1", "admin");

        _service.registerPromises(event, List.of(PrizeTier.placement(1, 1, PrizeItem.promise("Kept", 1))));

        var labels = _placeholderDAO.rows.stream().map(r -> r.label).toList();
        assertEquals(List.of("Kept", "Held", "Resolved"), labels);
        assertEquals(0, _service.getUnresolved().stream().filter(r -> r.label.equals("Dropped")).count());
        assertEquals(2, _service.getUnresolved().size());
    }

    @Test
    public void aPromiseResolvedBeforeTheEventEndsIsAwardedAsTheRealCard() throws Exception {
        var tiers = List.of(PrizeTier.placement(1, 1, PrizeItem.promise("Champion promo", 2)));
        var event = league("100", "Spring League");
        _service.registerPromises(event, tiers);
        int id = _placeholderDAO.rows.getFirst().id;

        Mockito.when(_collectionDAO.findHolders(PrizeService.placeholderBlueprint(id))).thenReturn(List.of());
        var result = _service.resolve(id, "9_1*", "admin");
        assertEquals(0, result.playerCount());
        assertTrue(_placeholderDAO.getPlaceholder(id).isResolved());

        Mockito.clearInvocations(_collectionsManager);
        _service.awardTiers(event, tiers, List.of(standing("p1", 1, 10, 5)), null);
        assertEquals(1, _placeholderDAO.rows.size());
        assertDelivered(deliveries(), "p1", "2x9_1*");
    }

    // ------------------------------------------------------------------------------------------------
    // Resolution
    // ------------------------------------------------------------------------------------------------

    @Test
    public void resolvingSwapsThePlaceholderForEveryHolderAndMarksTheRow() throws Exception {
        var tiers = List.of(PrizeTier.placement(1, 2, PrizeItem.promise("WC promo", 1)));
        _service.awardTiers(league("100", "Spring League"), tiers, List.of(standing("p1", 1, 10, 5), standing("p2", 2, 8, 5)), null);
        int id = _placeholderDAO.rows.getFirst().id;
        String placeholder = PrizeService.placeholderBlueprint(id);

        Mockito.when(_collectionDAO.findHolders(placeholder)).thenReturn(List.of(
                holder(1, "p1", "permanent", 1),
                holder(2, "p2", "permanent", 3),
                holder(2, "p2", "1700000000000", 1)));
        assertEquals(2, _service.holderCount(id));

        Mockito.clearInvocations(_collectionsManager);
        var result = _service.resolve(id, "9_1*", "admin");

        assertEquals(id, result.placeholderId());
        assertEquals("WC promo", result.label());
        assertEquals("9_1*", result.blueprintId());
        assertEquals(List.of("p1", "p2"), result.players());
        assertEquals(2, result.playerCount());
        assertEquals(5, result.cardsSwapped());

        var removedPlayers = ArgumentCaptor.forClass(String.class);
        var removedTypes = ArgumentCaptor.forClass(String.class);
        var removed = ArgumentCaptor.forClass(CardCollection.class);
        Mockito.verify(_collectionsManager, Mockito.times(3)).removeFromPlayerCollection(removedPlayers.capture(),
                removedTypes.capture(), removed.capture(), Mockito.eq("Prize resolved: WC promo"));
        assertEquals(List.of("p1", "p2", "p2"), removedPlayers.getAllValues());
        assertEquals(List.of("permanent", "permanent", "1700000000000"), removedTypes.getAllValues());
        assertEquals(1, removed.getAllValues().get(0).getItemCount(placeholder));
        assertEquals(3, removed.getAllValues().get(1).getItemCount(placeholder));
        assertEquals(1, removed.getAllValues().get(2).getItemCount(placeholder));

        var addedPlayers = ArgumentCaptor.forClass(String.class);
        var addedTypes = ArgumentCaptor.forClass(CollectionType.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<CardCollection.Item>> added = ArgumentCaptor.forClass(Iterable.class);
        Mockito.verify(_collectionsManager, Mockito.times(3)).addItemsToPlayerCollection(Mockito.eq(true),
                Mockito.eq("Prize resolved: WC promo"), addedPlayers.capture(), addedTypes.capture(), added.capture());
        assertEquals(List.of("p1", "p2", "p2"), addedPlayers.getAllValues());
        assertEquals(CollectionType.MY_CARDS, addedTypes.getAllValues().get(0));
        assertEquals("1700000000000", addedTypes.getAllValues().get(2).getCode());
        assertEquals(List.of(CardCollection.Item.createItem("9_1*", 3)), toList(added.getAllValues().get(1)));

        var row = _placeholderDAO.getPlaceholder(id);
        assertTrue(row.isResolved());
        assertEquals("9_1*", row.resolved_blueprint);
        assertEquals("admin", row.resolved_by);
        assertNotNull(row.resolved_on);
        assertTrue(_service.getUnresolved().isEmpty());
        assertEquals(1, _service.getResolved(10).size());

        // the id still renders, as a plain Future Prize
        assertFalse(_cardLibrary.getRegisteredPlaceholders().containsKey(placeholder));
        assertEquals(LotroCardBlueprintLibrary.PLACEHOLDER_TITLE, _cardLibrary.getLotroCardBlueprint(placeholder).getTitle());

        // and cannot be resolved again
        try {
            _service.resolve(id, "1_1", "admin");
            fail("Expected a second resolution to be rejected");
        } catch (PrizeDefinitionException exp) {
            assertEquals("placeholder", exp.getField());
        }
    }

    @Test
    public void resolvingRejectsUnknownPlaceholdersAndUnknownCards() throws Exception {
        try {
            _service.resolve(999, "1_1", "admin");
            fail("Expected an unknown placeholder to be rejected");
        } catch (PrizeDefinitionException exp) {
            assertEquals("placeholder", exp.getField());
        }

        var row = new DBDefs.PrizePlaceholder();
        row.label = "Promise";
        row.event_kind = "manual";
        int id = _placeholderDAO.createPlaceholder(row);
        for (String bad : new String[]{"999_999", "No Such Pack", "404_7", "", null}) {
            try {
                _service.resolve(id, bad, "admin");
                fail("Expected '" + bad + "' to be rejected");
            } catch (PrizeDefinitionException exp) {
                assertEquals("blueprintId", exp.getField());
            }
        }
        assertFalse(_placeholderDAO.getPlaceholder(id).isResolved());
        Mockito.verifyNoInteractions(_collectionsManager);

        // a pack is an acceptable resolution
        Mockito.when(_collectionDAO.findHolders(Mockito.anyString())).thenReturn(List.of());
        var result = _service.resolve(id, "Event Chase Booster", "admin");
        assertEquals(0, result.playerCount());
        assertTrue(_placeholderDAO.getPlaceholder(id).isResolved());
    }

    // ------------------------------------------------------------------------------------------------
    // Manual promises
    // ------------------------------------------------------------------------------------------------

    @Test
    public void manualPromisesAreAwardedToNamedPlayersOnly() throws Exception {
        Mockito.when(_playerDAO.getPlayer("alice")).thenReturn(player(1, "alice"));
        Mockito.when(_playerDAO.getPlayer("bob")).thenReturn(player(2, "bob"));

        try {
            _service.createManualPromise("Judge promo", 1, List.of("alice", "nobody", "bob", "ghost"), "admin");
            fail("Expected unknown players to be rejected");
        } catch (PrizeDefinitionException exp) {
            assertEquals("players", exp.getField());
            assertTrue(exp.getMessage(), exp.getMessage().contains("nobody"));
            assertTrue(exp.getMessage(), exp.getMessage().contains("ghost"));
        }
        Mockito.verifyNoInteractions(_collectionsManager);
        assertTrue(_placeholderDAO.rows.isEmpty());

        var row = _service.createManualPromise("Judge promo", 2, List.of("alice", "bob", "alice"), "admin");
        assertTrue(row.id > 0);
        assertEquals("manual", row.event_kind);
        assertEquals("admin", row.created_by);
        assertEquals("Judge promo", _cardLibrary.getLotroCardBlueprint("404_" + row.id).getTitle());

        var deliveries = deliveries();
        assertDelivered(deliveries, "alice", "2x404_" + row.id);
        assertDelivered(deliveries, "bob", "2x404_" + row.id);
        assertEquals(1, deliveries.get("alice").size());
        assertEquals(2, _awardDAO.getAwards("manual", String.valueOf(row.id)).size());
        for (String reason : reasons())
            assertEquals("Prize: Judge promo", reason);

        try {
            _service.createManualPromise("  ", 1, List.of("alice"), "admin");
            fail("Expected a blank label to be rejected");
        } catch (PrizeDefinitionException exp) {
            assertEquals("label", exp.getField());
        }
        try {
            _service.createManualPromise("x", 0, List.of("alice"), "admin");
            fail("Expected a zero count to be rejected");
        } catch (PrizeDefinitionException exp) {
            assertEquals("count", exp.getField());
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Validation and descriptions
    // ------------------------------------------------------------------------------------------------

    private static void assertInvalid(String expectedField, PrizeTier... tiers) {
        try {
            PrizeService.validateTiers(List.of(tiers), _cardLibrary, _productLibrary);
            fail("Expected the tiers to be rejected on " + expectedField);
        } catch (PrizeDefinitionException exp) {
            assertEquals(exp.getMessage(), expectedField, exp.getField());
        }
    }

    @Test
    public void validateTiersAcceptsSensibleDefinitions() throws Exception {
        PrizeService.validateTiers(null, _cardLibrary, _productLibrary);
        PrizeService.validateTiers(List.of(), _cardLibrary, _productLibrary);
        PrizeService.validateTiers(List.of(
                PrizeTier.placement(1, 1, PrizeItem.card("9_1*", 1), PrizeItem.card("1_2T", 1)),
                PrizeTier.placement(2, 8, PrizeItem.card("Event Chase Booster", 2)),
                PrizeTier.participation(0, PrizeTier.Scope.EVENT, PrizeItem.promise("Thanks for playing", 1)),
                PrizeTier.participation(10, PrizeTier.Scope.CAMPAIGN, PrizeItem.promise("Campaign promo", 1))),
                _cardLibrary, _productLibrary);
    }

    @Test
    public void validateTiersRejectsBadRangesCountsAndItems() {
        assertInvalid("prizeTiers[0].from", PrizeTier.placement(0, 1, PrizeItem.card("1_1", 1)));
        assertInvalid("prizeTiers[0].to", PrizeTier.placement(3, 2, PrizeItem.card("1_1", 1)));
        assertInvalid("prizeTiers[1].games", PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1)),
                PrizeTier.participation(-1, PrizeTier.Scope.EVENT, PrizeItem.card("1_1", 1)));
        assertInvalid("prizeTiers[0].items", PrizeTier.placement(1, 1));
        assertInvalid("prizeTiers[0].items[0].count", PrizeTier.placement(1, 1, PrizeItem.card("1_1", 0)));
        assertInvalid("prizeTiers[0].items[1].blueprintId", PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1), PrizeItem.card("999_999", 1)));
        assertInvalid("prizeTiers[0].items[0].blueprintId", PrizeTier.placement(1, 1, PrizeItem.card("No Such Booster", 1)));
        assertInvalid("prizeTiers[0].items[0].blueprintId", PrizeTier.placement(1, 1, PrizeItem.card("404_3", 1)));
        assertInvalid("prizeTiers[0].items[0]", PrizeTier.placement(1, 1, PrizeItem.promise("   ", 1)));
        assertInvalid("prizeTiers[0].items[0].promise", PrizeTier.placement(1, 1, PrizeItem.promise("x".repeat(256), 1)));

        var both = PrizeItem.card("1_1", 1);
        both.promise = "also a promise";
        assertInvalid("prizeTiers[0].items[0]", PrizeTier.placement(1, 1, both));

        var noKind = PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1));
        noKind.kind = null;
        assertInvalid("prizeTiers[0].kind", noKind);

        var noScope = PrizeTier.participation(1, null, PrizeItem.card("1_1", 1));
        assertInvalid("prizeTiers[0].scope", noScope);

        var longLabel = PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1));
        longLabel.label = "x".repeat(256);
        assertInvalid("prizeTiers[0].label", longLabel);
    }

    @Test
    public void describeTiersIsReadable() {
        var labelled = PrizeTier.participation(10, PrizeTier.Scope.CAMPAIGN, PrizeItem.promise("Campaign promo", 1));
        labelled.label = "Iron man";
        var described = _service.describeTiers(List.of(
                PrizeTier.placement(1, 1, PrizeItem.card("1_1", 1), PrizeItem.card("Event Chase Booster", 3)),
                PrizeTier.placement(2, 4, PrizeItem.promise("WC promo", 2)),
                PrizeTier.participation(5, PrizeTier.Scope.EVENT, PrizeItem.card("1_2*", 1)),
                labelled));
        assertEquals(List.of(
                "Place 1: 1x The One Ring, Isildur's Bane (1_1), 3x Event Chase Booster",
                "Places 2-4: 2x \"WC promo\" (promised)",
                "Participation (5+ games in this event): 1x The One Ring, The Ruling Ring (1_2*)",
                "Iron man - Participation (10+ games across the campaign): 1x \"Campaign promo\" (promised)"), described);
        assertTrue(_service.describeTiers(null).isEmpty());
    }

    // ------------------------------------------------------------------------------------------------
    // Library placeholder support
    // ------------------------------------------------------------------------------------------------

    @Test
    public void placeholderIdsResolveInTheLibraryAndAreNeverPlayable() throws Exception {
        assertTrue(LotroCardBlueprintLibrary.isPlaceholderId("404_12"));
        assertTrue(LotroCardBlueprintLibrary.isPlaceholderId("404_12*"));
        assertFalse(LotroCardBlueprintLibrary.isPlaceholderId("40_12"));
        assertFalse(LotroCardBlueprintLibrary.isPlaceholderId("404_"));
        assertFalse(LotroCardBlueprintLibrary.isPlaceholderId("Event Chase Booster"));
        assertFalse(LotroCardBlueprintLibrary.isPlaceholderId(null));

        var base = _cardLibrary.getLotroCardBlueprint("404_0");
        assertEquals("Future Prize", base.getTitle());
        assertEquals("404_0", base.getId());
        assertTrue(_cardLibrary.getSetDefinitions().containsKey("404"));
        assertFalse(_cardLibrary.getSetDefinitions().get("404").Merchantable());

        var unregistered = _cardLibrary.getLotroCardBlueprint("404_777*");
        assertEquals("Future Prize", unregistered.getTitle());
        assertEquals("404_777", unregistered.getId());
        assertEquals(base.getCardType(), unregistered.getCardType());
        assertSame(base, unregistered.getParent());

        _cardLibrary.registerPlaceholder("404_777", "Shiny promo");
        var registered = _cardLibrary.getLotroCardBlueprint("404_777");
        assertEquals("Shiny promo", registered.getTitle());
        assertEquals("Shiny promo", registered.getFullName());
        assertEquals("shinypromo", registered.getSanitizedTitle());
        assertSame(registered, _cardLibrary.getLotroCardBlueprint("404_777"));
        assertEquals(Map.of("404_777", "Shiny promo"), _cardLibrary.getRegisteredPlaceholders());

        _cardLibrary.unregisterPlaceholder("404_777");
        assertEquals("Future Prize", _cardLibrary.getLotroCardBlueprint("404_777").getTitle());

        var format = _formatLibrary.getFormat("fotr_block");
        assertEquals("Future prize placeholders cannot be played", format.validateCard("404_0"));
        assertEquals("Future prize placeholders cannot be played", format.validateCard("404_777*"));
        assertNull(format.validateCard("1_1"));
    }

    @Test
    public void collectionFiltersNeverHideAPlaceholderAPlayerHolds() {
        var filter = new com.gempukku.lotro.game.SortAndFilterCards();
        var items = List.of(CardCollection.Item.createItem("1_1", 1), CardCollection.Item.createItem("404_5", 1),
                CardCollection.Item.createItem("Event Chase Booster", 1));
        var ids = (java.util.function.Function<List<CardCollection.Item>, List<String>>) list -> list.stream().map(CardCollection.Item::getBlueprintId).sorted().toList();

        // a format, a block-style set range and a plain set range all keep the placeholder
        assertEquals(List.of("1_1", "404_5", "Event Chase Booster"), ids.apply(filter.process("format:fotr_block", items, _cardLibrary, _formatLibrary)));
        assertEquals(List.of("1_1", "404_5", "Event Chase Booster"), ids.apply(filter.process("set:0-34,50-200", items, _cardLibrary, _formatLibrary)));
        assertEquals(List.of("404_5", "Event Chase Booster"), ids.apply(filter.process("set:404", items, _cardLibrary, _formatLibrary)));
        // but a product filter for packs still excludes it, like any card
        assertEquals(List.of("Event Chase Booster"), ids.apply(filter.process("product:pack", items, _cardLibrary, _formatLibrary)));
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------------

    private static DBDefs.CollectionHolder holder(int playerId, String name, String type, int quantity) {
        var holder = new DBDefs.CollectionHolder();
        holder.player_id = playerId;
        holder.player_name = name;
        holder.collection_type = type;
        holder.quantity = quantity;
        return holder;
    }

    private static Player player(int id, String name) {
        return new Player(id, name, "", "", null, null, null, null, false);
    }

    private static List<CardCollection.Item> toList(Iterable<CardCollection.Item> items) {
        List<CardCollection.Item> list = new ArrayList<>();
        items.forEach(list::add);
        return list;
    }

    /**
     * prize_placeholder in memory, with the same reuse semantics as the real table.
     */
    private static class InMemoryPlaceholderDAO implements PrizePlaceholderDAO {
        final List<DBDefs.PrizePlaceholder> rows = new ArrayList<>();
        private int nextId = 1;

        @Override
        public DBDefs.PrizePlaceholder getPlaceholder(int id) {
            return rows.stream().filter(r -> r.id == id).findFirst().orElse(null);
        }

        @Override
        public List<DBDefs.PrizePlaceholder> getUnresolved() {
            return rows.stream().filter(r -> !r.isResolved()).toList();
        }

        @Override
        public List<DBDefs.PrizePlaceholder> getResolved(int limit) {
            return rows.stream().filter(DBDefs.PrizePlaceholder::isResolved).limit(limit).toList();
        }

        @Override
        public DBDefs.PrizePlaceholder findPromise(String eventKind, String eventId, String label) {
            var matching = rows.stream().filter(r -> Objects.equals(r.event_kind, eventKind)
                    && Objects.equals(r.event_id, eventId) && Objects.equals(r.label, label)).toList();
            var unresolved = matching.stream().filter(r -> !r.isResolved()).findFirst();
            if (unresolved.isPresent())
                return unresolved.get();
            return matching.isEmpty() ? null : matching.get(matching.size() - 1);
        }

        @Override
        public int createPlaceholder(DBDefs.PrizePlaceholder placeholder) {
            var copy = new DBDefs.PrizePlaceholder();
            copy.id = nextId++;
            copy.label = placeholder.label;
            copy.count = placeholder.count;
            copy.event_kind = placeholder.event_kind;
            copy.event_id = placeholder.event_id;
            copy.event_name = placeholder.event_name;
            copy.tier_index = placeholder.tier_index;
            copy.created = placeholder.created != null ? placeholder.created : DateUtils.Now().toLocalDateTime();
            copy.created_by = placeholder.created_by;
            copy.notes = placeholder.notes;
            rows.add(copy);
            return copy.id;
        }

        @Override
        public void markResolved(int id, String blueprintId, String resolvedBy) {
            var row = getPlaceholder(id);
            row.resolved_blueprint = blueprintId;
            row.resolved_on = DateUtils.Now().toLocalDateTime();
            row.resolved_by = resolvedBy;
        }

        @Override
        public List<DBDefs.PrizePlaceholder> findByEvent(String eventKind, String eventId) {
            return rows.stream().filter(r -> Objects.equals(r.event_kind, eventKind) && Objects.equals(r.event_id, eventId)).toList();
        }

        @Override
        public void deletePlaceholder(int id) {
            rows.removeIf(r -> r.id == id && !r.isResolved());
        }
    }

    private static class InMemoryAwardDAO implements PrizeAwardDAO {
        final List<DBDefs.PrizeAward> rows = new ArrayList<>();

        @Override
        public List<DBDefs.PrizeAward> getAwards(String eventKind, String eventId) {
            return rows.stream().filter(r -> Objects.equals(r.event_kind, eventKind) && Objects.equals(r.event_id, eventId)).toList();
        }

        @Override
        public int addAward(DBDefs.PrizeAward award) {
            award.id = rows.size() + 1;
            rows.add(award);
            return award.id;
        }
    }
}
