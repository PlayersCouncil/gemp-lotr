package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.prizes.PrizeItem;
import com.gempukku.lotro.prizes.PrizeTier;
import com.gempukku.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Exercises {@link LeagueFactory}: defaults, validation for each league type, instantiation and persistence.
 */
public class LeagueFactoryTest extends AbstractAtTest {
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 3, 1, 0, 0, 0, 0, DateUtils.UTC);

    private LeagueDAO _leagueDao;
    private LeagueService _leagueService;
    private LeagueFactory _factory;

    @Before
    public void setUp() {
        var collectionsManager = new CollectionsManager(null, null, null, _cardLibrary, _productLibrary);
        var soloDraftDefinitions = new SoloDraftDefinitions(collectionsManager, _cardLibrary, _formatLibrary);
        _leagueDao = Mockito.mock(LeagueDAO.class);
        _leagueService = Mockito.mock(LeagueService.class);
        _factory = new LeagueFactory(_cardLibrary, _productLibrary, _formatLibrary, soloDraftDefinitions, _leagueDao, _leagueService);
    }

    // ------------------------------------------------------------------------------------------------
    // Definitions used across the tests
    // ------------------------------------------------------------------------------------------------

    private static LeagueParams base(String name) {
        var params = new LeagueParams();
        params.name = name;
        params.start = START.toLocalDateTime();
        params.cost = 50;
        params.maxRepeatMatches = 1;
        params.description = "desc";
        return params;
    }

    private static LeagueParams constructed() {
        var params = base("Constructed - Fellowship Block");
        params.collectionName = "default";
        params.series.add(new LeagueParams.SerieData("fotr_block", 7, 5));
        params.series.add(new LeagueParams.SerieData("movie", 10, 8));
        params.prizeTiers.add(PrizeTier.placement(1, 3, PrizeItem.card("1_1", 1), PrizeItem.promise("Season promo", 2)));
        params.prizeTiers.add(PrizeTier.participation(3, PrizeTier.Scope.EVENT, PrizeItem.card("1_2", 2)));
        return params;
    }

    private static LeagueParams sealed() {
        var params = base("Sealed - Fellowship Block");
        params.series.add(new LeagueParams.SerieData("fotr_block_sealed", 7, 5));
        return params;
    }

    private static LeagueParams soloDraft() {
        var params = base("Draft - Fellowship Block");
        params.series.add(new LeagueParams.SerieData("fotr_draft", 14, 10));
        return params;
    }

    private static LeagueParams rtmd() {
        var params = base("Race to Mount Doom");
        params.series.add(new LeagueParams.SerieData("fotr_block", 7, 10));
        params.racePath = new ArrayList<>(List.of("92_3", "92_24"));
        params.raceVisualPath = new ArrayList<>(List.of("90_11", "90_2"));
        params.raceIntensityFloor = -10;
        params.raceIntensityCeiling = 10;
        params.raceAdvancementMode = RTMDLeague.AdvanceType.WIN;
        params.raceAdvanceFactor = 1;
        return params;
    }

    private void assertRejected(League.LeagueType type, LeagueParams params, String expectedParameter) {
        try {
            _factory.prepare(type, params);
            fail("Expected the definition to be rejected on parameter '" + expectedParameter + "'");
        } catch (LeagueDefinitionException exp) {
            assertEquals(exp.getMessage(), expectedParameter, exp.getParameter());
        }
        Mockito.verifyNoInteractions(_leagueDao, _leagueService);
    }

    // ------------------------------------------------------------------------------------------------
    // Happy paths
    // ------------------------------------------------------------------------------------------------

    @Test
    public void constructedLeagueChainsItsSeriesAndAddsTheDisplayGrace() throws LeagueDefinitionException {
        var prepared = _factory.prepare(League.LeagueType.CONSTRUCTED, constructed());

        assertEquals(League.LeagueType.CONSTRUCTED, prepared.type());
        assertTrue(prepared.data() instanceof ConstructedLeague);
        assertEquals(2, prepared.series().size());

        var first = prepared.series().get(0);
        var second = prepared.series().get(1);
        assertEquals(START, first.getStart());
        assertEquals(START.plusDays(6), first.getEnd());
        assertEquals(5, first.getMaxMatches());
        assertEquals("fotr_block", first.getFormat().getCode());
        assertFalse(first.isLimited());

        assertEquals(START.plusDays(7), second.getStart());
        assertEquals(START.plusDays(16), second.getEnd());
        assertEquals(8, second.getMaxMatches());

        assertEquals(START, prepared.start());
        assertEquals(START.plusDays(16 + LeagueFactory.DISPLAY_END_GRACE_DAYS), prepared.displayEnd());
    }

    @Test
    public void sealedLeagueTakesItsSerieCountFromTheTemplate() throws LeagueDefinitionException {
        var prepared = _factory.prepare(League.LeagueType.SEALED, sealed());

        assertTrue(prepared.data() instanceof SealedLeague);
        // The Fellowship Block sealed template defines four series
        assertEquals(4, prepared.series().size());
        for (int i = 0; i < 4; i++) {
            var serie = prepared.series().get(i);
            assertEquals(START.plusDays(7L * i), serie.getStart());
            assertEquals(START.plusDays(7L * (i + 1) - 1), serie.getEnd());
            assertTrue(serie.isLimited());
            assertEquals(5, serie.getMaxMatches());
        }
        assertEquals(START.plusDays(27 + LeagueFactory.DISPLAY_END_GRACE_DAYS), prepared.displayEnd());
    }

    @Test
    public void soloDraftLeagueIsASingleLimitedSerie() throws LeagueDefinitionException {
        var prepared = _factory.prepare(League.LeagueType.SOLODRAFT, soloDraft());

        assertTrue(prepared.data() instanceof SoloDraftLeague);
        assertEquals(1, prepared.series().size());
        var serie = prepared.series().getFirst();
        assertEquals(START, serie.getStart());
        assertEquals(START.plusDays(13), serie.getEnd());
        assertTrue(serie.isLimited());
        assertEquals(10, serie.getMaxMatches());
    }

    @Test
    public void rtmdLeagueKeepsItsRacePath() throws LeagueDefinitionException {
        var prepared = _factory.prepare(League.LeagueType.RTMD, rtmd());

        assertTrue(prepared.data() instanceof RTMDLeague);
        var race = (RTMDLeague) prepared.data();
        assertEquals(List.of("92_3", "92_24"), race.getPath());
        assertEquals(List.of("90_11", "90_2"), race.getVisualPath());
        assertEquals(1, prepared.series().size());
    }

    @Test
    public void createPersistsThePreparedLeagueAndClearsTheCache() throws LeagueDefinitionException {
        Mockito.when(_leagueDao.addLeague(Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.isNull())).thenReturn(42);

        var params = constructed();
        var prepared = _factory.prepare(League.LeagueType.CONSTRUCTED, params);
        int id = _factory.create(prepared);

        assertEquals(42, id);
        var paramsCaptor = ArgumentCaptor.forClass(LeagueParams.class);
        Mockito.verify(_leagueDao).addLeague(Mockito.eq("Constructed - Fellowship Block"), Mockito.eq(params.code),
                Mockito.eq(League.LeagueType.CONSTRUCTED), paramsCaptor.capture(),
                Mockito.eq(prepared.start()), Mockito.eq(prepared.displayEnd()), Mockito.eq(50), Mockito.isNull());
        assertSame(params, paramsCaptor.getValue());
        Mockito.verify(_leagueService).clearCache();
        Mockito.verifyNoMoreInteractions(_leagueDao, _leagueService);
    }

    @Test
    public void prepareDoesNotTouchTheDatabase() throws LeagueDefinitionException {
        _factory.prepare(League.LeagueType.SEALED, sealed());
        Mockito.verifyNoInteractions(_leagueDao, _leagueService);
    }

    // ------------------------------------------------------------------------------------------------
    // Updating an existing league
    // ------------------------------------------------------------------------------------------------

    private static League existingLeague(League.LeagueType type, LeagueParams storedParams, long code) {
        storedParams.code = code;
        return new League(storedParams.name, storedParams.cost, code, type, storedParams.toString(), 0);
    }

    @Test
    public void updateKeepsTheStoredCodeEvenWhenTheNewDefinitionCarriesAnother() throws LeagueDefinitionException {
        var existing = existingLeague(League.LeagueType.CONSTRUCTED, constructed(), 1234);

        var params = constructed();
        params.code = 9999;
        params.name = "Renamed League";

        var prepared = _factory.update(existing, params);

        assertEquals(1234, prepared.params().code);
        assertEquals(1234, params.code);
        assertSame(params, prepared.params());
        Mockito.verify(_leagueDao).updateLeague(Mockito.eq(1234L), Mockito.eq("Renamed League"), Mockito.same(params),
                Mockito.any(), Mockito.any(), Mockito.eq(50));

        var fresh = constructed();
        assertEquals(0, fresh.code);
        var kept = _factory.update(existingLeague(League.LeagueType.CONSTRUCTED, constructed(), 5678), fresh);
        assertEquals(5678, kept.params().code);
    }

    @Test
    public void updateRewritesTheRowWithThePreparedDatesAndClearsTheCache() throws LeagueDefinitionException {
        var existing = existingLeague(League.LeagueType.CONSTRUCTED, constructed(), 1234);

        var params = constructed();
        params.cost = 75;
        params.start = START.plusDays(3).toLocalDateTime();
        params.series.set(1, new LeagueParams.SerieData("movie", 20, 8));

        var prepared = _factory.update(existing, params);

        assertEquals(League.LeagueType.CONSTRUCTED, prepared.type());
        assertEquals(START.plusDays(3), prepared.start());
        assertEquals(START.plusDays(3 + 26 + LeagueFactory.DISPLAY_END_GRACE_DAYS), prepared.displayEnd());

        var paramsCaptor = ArgumentCaptor.forClass(LeagueParams.class);
        Mockito.verify(_leagueDao).updateLeague(Mockito.eq(1234L), Mockito.eq("Constructed - Fellowship Block"),
                paramsCaptor.capture(), Mockito.eq(prepared.start()), Mockito.eq(prepared.displayEnd()), Mockito.eq(75));
        assertSame(params, paramsCaptor.getValue());
        Mockito.verify(_leagueService).clearCache();
        Mockito.verifyNoMoreInteractions(_leagueDao, _leagueService);
    }

    @Test
    public void updateUsesTheStoredTypeNotTheCallers() throws LeagueDefinitionException {
        // A sealed definition handed to a league stored as SEALED validates against sealed templates
        var existing = existingLeague(League.LeagueType.SEALED, sealed(), 1234);
        var prepared = _factory.update(existing, sealed());
        assertEquals(League.LeagueType.SEALED, prepared.type());
        assertTrue(prepared.data() instanceof SealedLeague);
        assertEquals(4, prepared.series().size());
    }

    @Test
    public void updateOfAnInvalidDefinitionThrowsAndLeavesTheRowAlone() {
        var existing = existingLeague(League.LeagueType.CONSTRUCTED, constructed(), 1234);

        var params = constructed();
        params.series.set(0, new LeagueParams.SerieData("no_such_format", 7, 5));
        try {
            _factory.update(existing, params);
            fail("Expected the definition to be rejected");
        } catch (LeagueDefinitionException exp) {
            assertEquals("series[0].format", exp.getParameter());
        }

        // The wrong shape for the stored type is rejected as well
        var sealedExisting = existingLeague(League.LeagueType.SEALED, sealed(), 4321);
        try {
            _factory.update(sealedExisting, constructed());
            fail("Expected a two-serie definition to be rejected for a sealed league");
        } catch (LeagueDefinitionException exp) {
            assertEquals("series", exp.getParameter());
        }

        try {
            _factory.update(null, constructed());
            fail("Expected a missing league to be rejected");
        } catch (LeagueDefinitionException exp) {
            assertEquals("code", exp.getParameter());
        }

        Mockito.verifyNoInteractions(_leagueDao, _leagueService);
    }

    // ------------------------------------------------------------------------------------------------
    // Defaults
    // ------------------------------------------------------------------------------------------------

    @Test
    public void codeIsAssignedWhenMissingAndKeptWhenPresent() throws LeagueDefinitionException {
        long before = System.currentTimeMillis();
        var fresh = _factory.prepare(League.LeagueType.CONSTRUCTED, constructed());
        assertTrue(fresh.params().code >= before);

        var params = constructed();
        params.code = 1234;
        var kept = _factory.prepare(League.LeagueType.CONSTRUCTED, params);
        assertEquals(1234, kept.params().code);
    }

    @Test
    public void limitedLeaguesDefaultTheirCollectionNameToTheLeagueName() throws LeagueDefinitionException {
        // LeagueParams initialises collectionName to "default", which only makes sense for constructed leagues
        var sealedPrepared = _factory.prepare(League.LeagueType.SEALED, sealed());
        assertEquals("Sealed - Fellowship Block", sealedPrepared.params().collectionName);
        assertEquals("Sealed - Fellowship Block", sealedPrepared.series().getFirst().getCollectionType().getFullName());

        var draftPrepared = _factory.prepare(League.LeagueType.SOLODRAFT, soloDraft());
        assertEquals("Draft - Fellowship Block", draftPrepared.params().collectionName);

        var explicit = sealed();
        explicit.collectionName = "Custom Name";
        assertEquals("Custom Name", _factory.prepare(League.LeagueType.SEALED, explicit).params().collectionName);
    }

    @Test
    public void constructedLeaguesDefaultToThePlayersOwnCollection() throws LeagueDefinitionException {
        var params = constructed();
        params.collectionName = null;
        assertEquals("default", _factory.prepare(League.LeagueType.CONSTRUCTED, params).params().collectionName);

        var race = rtmd();
        race.collectionName = "";
        assertEquals("default", _factory.prepare(League.LeagueType.RTMD, race).params().collectionName);
    }

    @Test
    public void anExplicitCollectionNameIsKept() throws LeagueDefinitionException {
        var params = constructed();
        params.collectionName = "permanent+trophy";
        assertEquals("permanent+trophy", _factory.prepare(League.LeagueType.CONSTRUCTED, params).params().collectionName);
    }

    // ------------------------------------------------------------------------------------------------
    // Validation - common fields
    // ------------------------------------------------------------------------------------------------

    @Test
    public void rejectsMissingTypeOrParameters() {
        assertRejected(null, constructed(), "type");
        assertRejected(League.LeagueType.CONSTRUCTED, null, "parameters");
    }

    @Test
    public void rejectsBlankName() {
        var params = constructed();
        params.name = "  ";
        assertRejected(League.LeagueType.CONSTRUCTED, params, "name");
    }

    @Test
    public void rejectsOverlongName() {
        var params = constructed();
        params.name = "x".repeat(LeagueFactory.MAX_NAME_LENGTH + 1);
        assertRejected(League.LeagueType.CONSTRUCTED, params, "name");

        params.name = "x".repeat(LeagueFactory.MAX_NAME_LENGTH);
        try {
            _factory.prepare(League.LeagueType.CONSTRUCTED, params);
        } catch (LeagueDefinitionException exp) {
            fail("A name of exactly the maximum length must be accepted");
        }
    }

    @Test
    public void rejectsMissingStart() {
        var params = constructed();
        params.start = null;
        assertRejected(League.LeagueType.CONSTRUCTED, params, "start");
    }

    @Test
    public void rejectsNegativeCost() {
        var params = constructed();
        params.cost = -1;
        assertRejected(League.LeagueType.CONSTRUCTED, params, "cost");
    }

    @Test
    public void rejectsZeroRepeatMatches() {
        var params = constructed();
        params.maxRepeatMatches = 0;
        assertRejected(League.LeagueType.CONSTRUCTED, params, "maxRepeatMatches");
    }

    // ------------------------------------------------------------------------------------------------
    // Validation - prize tiers and campaign
    // ------------------------------------------------------------------------------------------------

    @Test
    public void acceptsALeagueWithoutPrizeTiersAndFillsInTheEmptyList() throws LeagueDefinitionException {
        var params = constructed();
        params.prizeTiers = null;
        var prepared = _factory.prepare(League.LeagueType.CONSTRUCTED, params);
        assertNotNull(prepared.params().prizeTiers);
        assertTrue(prepared.params().prizeTiers.isEmpty());
    }

    @Test
    public void rejectsPrizeTiersWithBadRanges() {
        var params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(0, 3, PrizeItem.card("1_1", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(4, 3, PrizeItem.card("1_1", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(1, PrizeTier.participation(-1, PrizeTier.Scope.EVENT, PrizeItem.card("1_1", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");
    }

    @Test
    public void rejectsPrizeTiersWithUnknownCardsOrEmptyPromises() {
        var params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1, PrizeItem.card("999_999", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1, PrizeItem.card("No Such Booster", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1, PrizeItem.promise("  ", 1)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1, PrizeItem.card("1_1", 0)));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");

        params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "prizeTiers");
    }

    @Test
    public void acceptsFoilsTengwarsAndPacksAsPrizes() throws LeagueDefinitionException {
        var params = constructed();
        params.prizeTiers.set(0, PrizeTier.placement(1, 1, PrizeItem.card("1_1*", 1), PrizeItem.card("1_2T", 1),
                PrizeItem.card("Event Chase Booster", 3)));
        _factory.prepare(League.LeagueType.CONSTRUCTED, params);
    }

    @Test
    public void campaignTagIsTrimmedAndBlankMeansNone() throws LeagueDefinitionException {
        var params = constructed();
        params.campaign = "  Yuletide 2026 ";
        assertEquals("Yuletide 2026", _factory.prepare(League.LeagueType.CONSTRUCTED, params).params().campaign);

        params = constructed();
        params.campaign = "   ";
        assertNull(_factory.prepare(League.LeagueType.CONSTRUCTED, params).params().campaign);

        params = constructed();
        params.campaign = "x".repeat(LeagueFactory.MAX_NAME_LENGTH + 1);
        assertRejected(League.LeagueType.CONSTRUCTED, params, "campaign");
    }

    // ------------------------------------------------------------------------------------------------
    // Validation - series
    // ------------------------------------------------------------------------------------------------

    @Test
    public void rejectsMissingSeries() {
        var params = constructed();
        params.series = null;
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series");

        params.series = new ArrayList<>();
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series");
    }

    @Test
    public void rejectsMalformedSerie() {
        var params = constructed();
        params.series.set(1, new LeagueParams.SerieData("", 7, 5));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series[1].format");

        params = constructed();
        params.series.set(0, new LeagueParams.SerieData("fotr_block", 0, 5));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series[0].duration");

        params = constructed();
        params.series.set(0, new LeagueParams.SerieData("fotr_block", 7, 0));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series[0].matches");
    }

    @Test
    public void rejectsUnknownConstructedFormat() {
        var params = constructed();
        params.series.set(1, new LeagueParams.SerieData("no_such_format", 7, 5));
        assertRejected(League.LeagueType.CONSTRUCTED, params, "series[1].format");
    }

    @Test
    public void rejectsUnknownSealedTemplate() {
        var params = sealed();
        params.series.set(0, new LeagueParams.SerieData("no_such_template", 7, 5));
        assertRejected(League.LeagueType.SEALED, params, "format");
    }

    @Test
    public void sealedLeagueMustHaveExactlyOneSerieDefinition() {
        var params = sealed();
        params.series.add(new LeagueParams.SerieData("fotr_block_sealed", 7, 5));
        assertRejected(League.LeagueType.SEALED, params, "series");
    }

    @Test
    public void rejectsUnknownSoloDraft() {
        var params = soloDraft();
        params.series.set(0, new LeagueParams.SerieData("no_such_draft", 7, 5));
        assertRejected(League.LeagueType.SOLODRAFT, params, "format");
    }

    @Test
    public void soloDraftLeagueMustHaveExactlyOneSerieDefinition() {
        var params = soloDraft();
        params.series.add(new LeagueParams.SerieData("fotr_draft", 7, 5));
        assertRejected(League.LeagueType.SOLODRAFT, params, "series");
    }

    // ------------------------------------------------------------------------------------------------
    // Validation - RTMD
    // ------------------------------------------------------------------------------------------------

    @Test
    public void rtmdRequiresARacePath() {
        var params = rtmd();
        params.racePath = null;
        assertRejected(League.LeagueType.RTMD, params, "racePath");

        params.racePath = new ArrayList<>();
        assertRejected(League.LeagueType.RTMD, params, "racePath");
    }

    @Test
    public void rtmdRacePathIsCapped() {
        var params = rtmd();
        params.racePath = new ArrayList<>();
        params.raceVisualPath = new ArrayList<>();
        for (int i = 0; i <= LeagueFactory.MAX_RACE_PATH_LENGTH; i++) {
            params.racePath.add("92_3");
            params.raceVisualPath.add("90_11");
        }
        assertRejected(League.LeagueType.RTMD, params, "racePath");
    }

    @Test
    public void rtmdRejectsUnknownBlueprintsInEitherPath() {
        var params = rtmd();
        params.racePath.set(1, "999_999");
        assertRejected(League.LeagueType.RTMD, params, "racePath");

        params = rtmd();
        params.raceVisualPath.set(0, "999_999");
        assertRejected(League.LeagueType.RTMD, params, "raceVisualPath");
    }

    @Test
    public void rtmdVisualPathMustMatchTheModifierPath() {
        var params = rtmd();
        params.raceVisualPath.remove(1);
        assertRejected(League.LeagueType.RTMD, params, "raceVisualPath");
    }

    @Test
    public void rtmdRejectsInvertedIntensityRange() {
        var params = rtmd();
        params.raceIntensityFloor = 5;
        params.raceIntensityCeiling = 4;
        assertRejected(League.LeagueType.RTMD, params, "raceIntensity");
    }

    @Test
    public void rtmdRejectsZeroAdvanceFactor() {
        var params = rtmd();
        params.raceAdvanceFactor = 0;
        assertRejected(League.LeagueType.RTMD, params, "raceAdvanceFactor");
    }

    @Test
    public void rtmdRejectsUnknownSeriesFormatToo() {
        var params = rtmd();
        params.series.set(0, new LeagueParams.SerieData("no_such_format", 7, 10));
        assertRejected(League.LeagueType.RTMD, params, "series[0].format");
    }

    // ------------------------------------------------------------------------------------------------
    // Round trip through the persisted representation
    // ------------------------------------------------------------------------------------------------

    @Test
    public void aDefinitionSurvivesTheJsonRoundTrip() throws LeagueDefinitionException {
        var original = _factory.prepare(League.LeagueType.RTMD, rtmd());

        String json = original.params().toString();
        LeagueParams restored = JsonUtils.Convert(json, LeagueParams.class);
        assertNotNull(restored);

        var again = _factory.prepare(League.LeagueType.RTMD, restored);

        assertEquals(original.params().code, again.params().code);
        assertEquals(original.start(), again.start());
        assertEquals(original.displayEnd(), again.displayEnd());
        assertEquals(original.series().size(), again.series().size());
        for (int i = 0; i < original.series().size(); i++) {
            assertEquals(original.series().get(i).getStart(), again.series().get(i).getStart());
            assertEquals(original.series().get(i).getEnd(), again.series().get(i).getEnd());
            assertEquals(original.series().get(i).getMaxMatches(), again.series().get(i).getMaxMatches());
            assertEquals(original.series().get(i).getFormat().getCode(), again.series().get(i).getFormat().getCode());
        }
        assertEquals(((RTMDLeague) original.data()).getPath(), ((RTMDLeague) again.data()).getPath());
    }

    @Test
    public void prizeTiersAndCampaignSurviveTheJsonRoundTrip() throws LeagueDefinitionException {
        var params = constructed();
        params.campaign = "Yuletide 2026";
        params.prizeTiers.add(PrizeTier.participation(10, PrizeTier.Scope.CAMPAIGN, PrizeItem.promise("Campaign promo", 1)));
        params.prizeTiers.get(2).label = "Iron man";
        var original = _factory.prepare(League.LeagueType.CONSTRUCTED, params);

        LeagueParams restored = JsonUtils.Convert(original.params().toString(), LeagueParams.class);
        assertEquals("Yuletide 2026", restored.campaign);
        assertEquals(3, restored.prizeTiers.size());

        var placement = restored.prizeTiers.get(0);
        assertEquals(PrizeTier.Kind.PLACEMENT, placement.kind);
        assertEquals(1, placement.from);
        assertEquals(3, placement.to);
        assertEquals(2, placement.items.size());
        assertEquals("1_1", placement.items.get(0).blueprintId);
        assertNull(placement.items.get(0).promise);
        assertEquals("Season promo", placement.items.get(1).promise);
        assertEquals(2, placement.items.get(1).count);

        var campaign = restored.prizeTiers.get(2);
        assertEquals(PrizeTier.Kind.PARTICIPATION, campaign.kind);
        assertEquals(PrizeTier.Scope.CAMPAIGN, campaign.scope);
        assertEquals(10, campaign.games);
        assertEquals("Iron man", campaign.label);

        // and the restored definition is still valid
        _factory.prepare(League.LeagueType.CONSTRUCTED, restored);
    }

    /**
     * Leagues stored by the previous code carry an "extraPrizes" object that no longer has a field.  fastjson2
     * ignores unknown properties by default, so those rows must still load.
     */
    @Test
    public void definitionsStoredWithTheOldExtraPrizesFieldStillParse() throws LeagueDefinitionException {
        // exactly what the previous code persisted: today's serialisation plus the removed "extraPrizes" object
        var params = constructed();
        params.name = "Old League";
        params.code = 1700000000000L;
        params.prizeTiers.clear();
        String current = params.toString();
        String oldJson = "{\"extraPrizes\":{\"topPrize\":\"1x1_1\",\"topCutoff\":10,\"participationPrize\":\"2x1_2\",\"participationGames\":3},"
                + current.substring(1).replace("\"prizeTiers\":[],", "").replace(",\"prizeTiers\":[]", "");
        assertTrue(oldJson, oldJson.contains("\"extraPrizes\""));
        assertFalse(oldJson, oldJson.contains("prizeTiers"));

        LeagueParams restored = JsonUtils.Convert(oldJson, LeagueParams.class);
        assertNotNull(restored);
        assertEquals("Old League", restored.name);
        assertEquals(1700000000000L, restored.code);
        assertEquals(2, restored.series.size());
        assertNull(restored.campaign);
        assertNotNull(restored.prizeTiers);
        assertTrue(restored.prizeTiers.isEmpty());

        // the stored row can still be turned into a league
        var league = new League("Old League", 50, 1700000000000L, League.LeagueType.CONSTRUCTED, oldJson, 0);
        var data = league.getLeagueData(_productLibrary, _formatLibrary, null);
        assertEquals(2, data.getSeries().size());
        assertTrue(data.getParameters().prizeTiers.isEmpty());

        // and validates as it is
        _factory.prepare(League.LeagueType.CONSTRUCTED, restored);
    }
}
