package com.gempukku.lotro.events;

import com.gempukku.lotro.cache.CacheManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.TournamentDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The completed-event browser's service layer.
 * <p>
 * The DAOs are mocked, as everywhere else in this suite, so the two new SQL statements themselves are not covered
 * here - only that they are called with the right half-open ranges and that their rows are assembled correctly.
 * The handler is not covered either: gemp-lotr-async has no test tree, which is why month and kind parsing (the
 * contract's 400s) and row assembly live on the service rather than in the handler.
 */
public class EventHistoryServiceTest {

    private LeagueDAO _leagueDao;
    private TournamentDAO _tournamentDao;
    private LotroFormatLibrary _formatLibrary;
    private EventHistoryService _service;

    @Before
    public void setUp() {
        _leagueDao = Mockito.mock(LeagueDAO.class);
        _tournamentDao = Mockito.mock(TournamentDAO.class);
        _formatLibrary = Mockito.mock(LotroFormatLibrary.class);

        Mockito.when(_tournamentDao.getFinishedTournamentsBetween(Mockito.any(), Mockito.any())).thenReturn(List.of());
        Mockito.when(_tournamentDao.getFinishedTournamentPlayerCountsBetween(Mockito.any(), Mockito.any())).thenReturn(Map.of());
        Mockito.when(_leagueDao.loadLeaguesEndingBetween(Mockito.any(), Mockito.any())).thenReturn(List.of());
        Mockito.when(_leagueDao.getParticipantCountsForLeaguesEndingBetween(Mockito.any(), Mockito.any())).thenReturn(Map.of());

        _service = new EventHistoryService(_leagueDao, _tournamentDao, Mockito.mock(ProductLibrary.class),
                _formatLibrary, Mockito.mock(SoloDraftDefinitions.class));
    }

    private static DBDefs.Tournament tournament(String id, String name, LocalDateTime start, String formatCode, int round) {
        var row = new DBDefs.Tournament();
        row.tournament_id = id;
        row.name = name;
        row.start_date = start;
        row.type = "CONSTRUCTED";
        row.parameters = "{\"tournamentId\":\"" + id + "\",\"name\":\"" + name + "\",\"format\":\"" + formatCode + "\"}";
        row.stage = "FINISHED";
        row.round = round;
        return row;
    }

    private static DBDefs.League league(long code, String name, LocalDate start, LocalDate end) {
        var row = new DBDefs.League();
        row.id = (int) code;
        row.code = code;
        row.name = name;
        row.type = "CONSTRUCTED";
        // The legacy comma-separated parameters: start, collection, multiplier, repeats, series, then per-serie
        // format / duration / max matches.
        row.parameters = "20120502,default,0.7,1,1,lotr_block,7,2";
        row.start_date = start;
        row.end_date = end;
        row.status = 1;
        row.cost = 0;
        return row;
    }

    private void stubFormat(String code, String name) {
        LotroFormat format = Mockito.mock(LotroFormat.class);
        Mockito.when(format.getName()).thenReturn(name);
        Mockito.when(_formatLibrary.getFormat(code)).thenReturn(format);
    }

    private static Map<String, Object> only(List<Map<String, Object>> events, String id) {
        return events.stream().filter(x -> id.equals(x.get("id"))).findFirst()
                .orElseThrow(() -> new AssertionError("No event with id " + id + " in " + events));
    }

    private static final String T = EventHistoryService.KIND_TOURNAMENT;
    private static final String L = EventHistoryService.KIND_LEAGUE;
    private static final YearMonth MARCH = YearMonth.of(2026, 3);

    private void stubTournaments(DBDefs.Tournament... rows) {
        Mockito.when(_tournamentDao.getFinishedTournamentsBetween(Mockito.any(), Mockito.any())).thenReturn(List.of(rows));
    }

    private void stubLeagues(DBDefs.League... rows) {
        Mockito.when(_leagueDao.loadLeaguesEndingBetween(Mockito.any(), Mockito.any())).thenReturn(List.of(rows));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> adminLinksOf(Map<String, Object> event) {
        return (List<Map<String, Object>>) event.get("adminLinks");
    }

    // --- request parsing (the handler's 400s) ---------------------------------------------------------------

    @Test
    public void parsesAWellFormedMonth() {
        assertEquals(YearMonth.of(2026, 3), EventHistoryService.parseMonth("2026-03"));
        assertEquals(YearMonth.of(2026, 12), EventHistoryService.parseMonth(" 2026-12 "));
    }

    @Test
    public void rejectsAMissingOrMalformedMonth() {
        // Each of these is what the handler turns into the contract's 400.
        assertNull(EventHistoryService.parseMonth(null));
        assertNull(EventHistoryService.parseMonth(""));
        assertNull(EventHistoryService.parseMonth("   "));
        assertNull(EventHistoryService.parseMonth("2026"));
        assertNull(EventHistoryService.parseMonth("2026-13"));
        assertNull(EventHistoryService.parseMonth("2026-03-04"));
        assertNull(EventHistoryService.parseMonth("March"));
    }

    @Test
    public void parsesBothKinds() {
        assertEquals("league", EventHistoryService.parseKind("league"));
        assertEquals("tournament", EventHistoryService.parseKind("tournament"));
        assertEquals("tournament", EventHistoryService.parseKind(" Tournament "));
    }

    @Test
    public void rejectsAMissingOrUnknownKind() {
        // Each of these is what the handler turns into the contract's 400, on both endpoints.
        assertNull(EventHistoryService.parseKind(null));
        assertNull(EventHistoryService.parseKind(""));
        assertNull(EventHistoryService.parseKind("  "));
        assertNull(EventHistoryService.parseKind("leagues"));
        assertNull(EventHistoryService.parseKind("all"));
        assertNull(EventHistoryService.parseKind("league,tournament"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theServiceRefusesAnUnparsedKindForAMonth() {
        _service.getMonth(MARCH, "bogus");
    }

    @Test(expected = IllegalArgumentException.class)
    public void theServiceRefusesAnUnparsedKindForTheMonthsList() {
        _service.getAvailableMonths(null);
    }

    // --- empty month ---------------------------------------------------------------------------------------

    @Test
    public void anEmptyMonthIsAnEmptyList() {
        assertTrue(_service.getMonth(YearMonth.of(2026, 5), T).isEmpty());
        assertTrue(_service.getMonthEvents(YearMonth.of(2026, 5), T, true).isEmpty());
        assertTrue(_service.getMonthEvents(YearMonth.of(2026, 5), L, true).isEmpty());
    }

    // --- kind filtering ------------------------------------------------------------------------------------

    @Test
    public void aTournamentRequestListsOnlyTournamentsAndNeverQueriesLeagues() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        var events = _service.getMonthEvents(MARCH, T, false);

        assertEquals(List.of("t1"), events.stream().map(x -> x.get("id")).toList());
        Mockito.verify(_leagueDao, Mockito.never()).loadLeaguesEndingBetween(Mockito.any(), Mockito.any());
        Mockito.verify(_leagueDao, Mockito.never()).getParticipantCountsForLeaguesEndingBetween(Mockito.any(), Mockito.any());
    }

    @Test
    public void aLeagueRequestListsOnlyLeaguesAndNeverQueriesTournaments() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        var events = _service.getMonthEvents(MARCH, L, false);

        assertEquals(List.of("456"), events.stream().map(x -> x.get("id")).toList());
        Mockito.verify(_tournamentDao, Mockito.never()).getFinishedTournamentsBetween(Mockito.any(), Mockito.any());
        Mockito.verify(_tournamentDao, Mockito.never()).getFinishedTournamentPlayerCountsBetween(Mockito.any(), Mockito.any());
    }

    // --- bucketing -----------------------------------------------------------------------------------------

    @Test
    public void tournamentsAreBucketedByStartDateOverAHalfOpenRange() {
        _service.getMonth(MARCH, T);

        var from = ArgumentCaptor.forClass(ZonedDateTime.class);
        var to = ArgumentCaptor.forClass(ZonedDateTime.class);
        Mockito.verify(_tournamentDao).getFinishedTournamentsBetween(from.capture(), to.capture());

        assertEquals("2026-03-01T00:00Z", from.getValue().toString());
        assertEquals("2026-04-01T00:00Z", to.getValue().toString());
    }

    @Test
    public void monthRangesRollOverTheYear() {
        _service.getMonth(YearMonth.of(2026, 12), T);

        var from = ArgumentCaptor.forClass(ZonedDateTime.class);
        var to = ArgumentCaptor.forClass(ZonedDateTime.class);
        Mockito.verify(_tournamentDao).getFinishedTournamentsBetween(from.capture(), to.capture());

        assertEquals("2026-12-01T00:00Z", from.getValue().toString());
        assertEquals("2027-01-01T00:00Z", to.getValue().toString());
    }

    @Test
    public void aTournamentStartingOnTheLastDayOfTheMonthIsIncluded() {
        // The upper bound must be exclusive-midnight-of-the-next-month, not the last day: start_date is a DATETIME.
        stubTournaments(tournament("t1", "Late Night", LocalDateTime.of(2026, 3, 31, 23, 59, 59), "lotr_block", 3));

        var events = _service.getMonthEvents(MARCH, T, false);

        assertEquals(1, events.size());
        assertEquals("2026-03-31", only(events, "t1").get("startDate"));
        var to = ArgumentCaptor.forClass(ZonedDateTime.class);
        Mockito.verify(_tournamentDao).getFinishedTournamentsBetween(Mockito.any(), to.capture());
        assertTrue(LocalDateTime.of(2026, 3, 31, 23, 59, 59).atZone(java.time.ZoneOffset.UTC).isBefore(to.getValue()));
    }

    @Test
    public void leaguesAreBucketedByEndDate() {
        _service.getMonth(MARCH, L);

        var from = ArgumentCaptor.forClass(LocalDate.class);
        var to = ArgumentCaptor.forClass(LocalDate.class);
        Mockito.verify(_leagueDao).loadLeaguesEndingBetween(from.capture(), to.capture());

        assertEquals(LocalDate.of(2026, 3, 1), from.getValue());
        assertEquals(LocalDate.of(2026, 4, 1), to.getValue());
    }

    @Test
    public void aLeagueIsListedUnderTheMonthItEndedInNotTheOneItStartedIn() {
        // Spans February into March; the DAO buckets it by end date, and the service does not second-guess that.
        stubLeagues(league(123L, "Spring League", LocalDate.of(2026, 2, 20), LocalDate.of(2026, 3, 14)));

        var events = _service.getMonthEvents(MARCH, L, false);

        assertEquals(1, events.size());
        var event = only(events, "123");
        assertEquals("2026-02-20", event.get("startDate"));
        assertEquals("2026-03-14", event.get("endDate"));
    }

    // --- only completed events ----------------------------------------------------------------------------

    @Test
    public void aLeagueThatHasNotEndedYetIsNotListed() {
        LocalDate today = LocalDate.now(java.time.ZoneOffset.UTC);
        stubLeagues(
                league(1L, "Still Running", today.minusDays(20), today.plusDays(3)),
                league(2L, "Ends Today", today.minusDays(20), today),
                league(3L, "Over", today.minusDays(20), today.minusDays(1)));

        var events = _service.getMonthEvents(YearMonth.from(today), L, false);

        assertEquals(1, events.size());
        assertEquals("3", events.getFirst().get("id"));
    }

    @Test
    public void aFinishedTournamentWithNoRoundPlayedIsNotListed() {
        stubTournaments(
                tournament("t0", "Bot Draft", LocalDateTime.of(2026, 3, 4, 18, 0), "lotr_block", 0),
                tournament("t1", "Real Event", LocalDateTime.of(2026, 3, 4, 19, 0), "lotr_block", 4));

        var events = _service.getMonthEvents(MARCH, T, false);

        assertEquals(1, events.size());
        assertEquals("t1", events.getFirst().get("id"));
    }

    // --- shape, counts and ordering -----------------------------------------------------------------------

    @Test
    public void tournamentRowsCarryExactlyTheContractedFields() {
        stubFormat("lotr_block", "LotR Block");
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        Mockito.when(_tournamentDao.getFinishedTournamentPlayerCountsBetween(Mockito.any(), Mockito.any()))
                .thenReturn(Map.of("t1", 14));

        var event = only(_service.getMonthEvents(MARCH, T, false), "t1");

        // v2: no per-event kind (echoed once at the top level) and no detailUrl.
        assertEquals(List.of("id", "name", "startDate", "endDate", "format", "playerCount", "rounds"),
                List.copyOf(event.keySet()));
        assertEquals("March Open", event.get("name"));
        assertEquals("2026-03-08", event.get("startDate"));
        assertNull(event.get("endDate"));                       // tournament has no finish column
        assertEquals("LotR Block", event.get("format"));
        assertEquals(14, event.get("playerCount"));
        assertEquals(5, event.get("rounds"));
    }

    @Test
    public void leagueRowsCarryExactlyTheContractedFields() {
        stubFormat("lotr_block", "LotR Block");
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));
        Mockito.when(_leagueDao.getParticipantCountsForLeaguesEndingBetween(Mockito.any(), Mockito.any()))
                .thenReturn(Map.of("456", 42));

        var event = only(_service.getMonthEvents(MARCH, L, false), "456");

        assertEquals(List.of("id", "name", "startDate", "endDate", "format", "playerCount", "rounds"),
                List.copyOf(event.keySet()));
        assertEquals("March Constructed", event.get("name"));
        assertEquals("2026-03-01", event.get("startDate"));
        assertEquals("2026-03-21", event.get("endDate"));
        assertEquals("LotR Block", event.get("format"));
        assertEquals(42, event.get("playerCount"));
        assertNull(event.get("rounds"));                        // leagues have series, not rounds
    }

    @Test
    public void detailUrlAndKindAreAbsentEvenForAnAdmin() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        for (String kind : List.of(T, L)) {
            for (var event : _service.getMonthEvents(MARCH, kind, true)) {
                assertFalse(event.containsKey("detailUrl"));
                assertFalse(event.containsKey("kind"));
            }
        }
    }

    @Test
    public void aPlayerCountThatIsNotAvailableIsNullRatherThanZero() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));

        var event = only(_service.getMonthEvents(MARCH, T, false), "t1");

        assertNull(event.get("playerCount"));
        assertTrue(event.containsKey("playerCount"));
    }

    @Test
    public void eventsAreSortedByStartDateDescendingThenName() {
        stubTournaments(
                tournament("a", "Zulu", LocalDateTime.of(2026, 3, 4, 19, 0), "lotr_block", 2),
                tournament("b", "Alpha", LocalDateTime.of(2026, 3, 4, 20, 0), "lotr_block", 2),
                tournament("c", "Middle", LocalDateTime.of(2026, 3, 9, 20, 0), "lotr_block", 2));
        stubLeagues(
                league(7L, "B League", LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 20)),
                league(8L, "A League", LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 22)),
                league(9L, "C League", LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 25)));

        assertEquals(List.of("c", "b", "a"),
                _service.getMonthEvents(MARCH, T, false).stream().map(x -> x.get("id")).toList());
        assertEquals(List.of("9", "8", "7"),
                _service.getMonthEvents(MARCH, L, false).stream().map(x -> x.get("id")).toList());
    }

    // --- admin fields --------------------------------------------------------------------------------------

    @Test
    public void adminLinksAreAbsentForANonAdmin() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        for (String kind : List.of(T, L)) {
            var events = _service.getMonthEvents(MARCH, kind, false);
            assertEquals(1, events.size());
            for (var event : events)
                assertFalse("A non-admin must not be sent adminLinks", event.containsKey("adminLinks"));
        }
    }

    @Test
    public void aTournamentsAdminLinkIsTheReportShownAsItsId() {
        stubTournaments(tournament("1712345678901", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));

        var links = adminLinksOf(only(_service.getMonthEvents(MARCH, T, true), "1712345678901"));

        assertEquals(1, links.size());
        assertEquals(List.of("label", "text", "url"), List.copyOf(links.getFirst().keySet()));
        assertEquals("Report", links.getFirst().get("label"));
        assertEquals("1712345678901", links.getFirst().get("text"));
        assertEquals("/gemp-lotr-server/tournament/1712345678901/report/html", links.getFirst().get("url"));
    }

    @Test
    public void aLeaguesAdminLinkIsItsCodeAsPlainText() {
        stubLeagues(league(456L, "March Constructed", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        var links = adminLinksOf(only(_service.getMonthEvents(MARCH, L, true), "456"));

        assertEquals(1, links.size());
        assertEquals(List.of("label", "text", "url"), List.copyOf(links.getFirst().keySet()));
        assertEquals("Code", links.getFirst().get("label"));
        assertEquals("456", links.getFirst().get("text"));
        assertTrue(links.getFirst().containsKey("url"));        // explicit null, serialized as such
        assertNull(links.getFirst().get("url"));
    }

    @Test
    public void oneCachedMonthServesAdminsAndNonAdminsAlike() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));

        _service.getMonthEvents(MARCH, T, true);
        var asPlayer = _service.getMonthEvents(MARCH, T, false);

        assertFalse(only(asPlayer, "t1").containsKey("adminLinks"));
        Mockito.verify(_tournamentDao, Mockito.times(1)).getFinishedTournamentsBetween(Mockito.any(), Mockito.any());
    }

    // --- robustness ----------------------------------------------------------------------------------------

    @Test
    public void aRetiredTournamentFormatDegradesToItsRawCode() {
        // getFormat returns null for a code the library no longer knows; the old code called getName() on it.
        Mockito.when(_formatLibrary.getFormat("ancient_block")).thenReturn(null);
        stubTournaments(tournament("t1", "Old Event", LocalDateTime.of(2026, 3, 8, 19, 0), "ancient_block", 3));

        assertEquals("ancient_block", only(_service.getMonthEvents(MARCH, T, false), "t1").get("format"));
    }

    @Test
    public void aRetiredLeagueFormatDegradesToItsRawCode() {
        Mockito.when(_formatLibrary.getFormat(Mockito.anyString())).thenReturn(null);
        stubLeagues(league(456L, "Old League", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        assertEquals("lotr_block", only(_service.getMonthEvents(MARCH, L, false), "456").get("format"));
    }

    @Test
    public void aTournamentWhoseParametersNoLongerParseStillLists() {
        var broken = tournament("t1", "Ancient", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 3);
        broken.parameters = "this is not json at all }{";
        stubFormat("lotr_block", "LotR Block");
        stubTournaments(broken, tournament("t2", "Fine", LocalDateTime.of(2026, 3, 9, 19, 0), "lotr_block", 3));

        var events = _service.getMonthEvents(MARCH, T, false);

        assertEquals(2, events.size());
        assertEquals("CONSTRUCTED", only(events, "t1").get("format"));   // falls all the way back to the type
        assertEquals("LotR Block", only(events, "t2").get("format"));
    }

    @Test
    public void aLeagueWhoseDefinitionNoLongerBuildsStillLists() {
        var broken = league(1L, "Unbuildable", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21));
        broken.parameters = "{ not valid league parameters";
        stubLeagues(broken, league(2L, "Fine", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 21)));

        var events = _service.getMonthEvents(MARCH, L, false);

        assertEquals(2, events.size());
        assertEquals("CONSTRUCTED", only(events, "1").get("format"));
        assertNotNull(only(events, "2").get("format"));
    }

    @Test
    public void aFailingMonthQueryPropagatesAndIsNotCached() {
        // v1 swallowed this and cached an empty month until the next "Clear Server Cache".
        Mockito.when(_tournamentDao.getFinishedTournamentsBetween(Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("database is down"))
                .thenReturn(List.of(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5)));

        assertThrows(RuntimeException.class, () -> _service.getMonthEvents(MARCH, T, false));
        assertEquals(0, _service.getItemCount());

        assertEquals(1, _service.getMonthEvents(MARCH, T, false).size());
    }

    @Test
    public void failingPlayerCountsDegradeToNullAndAreNotCached() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));
        Mockito.when(_tournamentDao.getFinishedTournamentPlayerCountsBetween(Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("timeout"))
                .thenReturn(Map.of("t1", 8));

        assertNull(only(_service.getMonthEvents(MARCH, T, false), "t1").get("playerCount"));
        assertEquals(0, _service.getItemCount());
        assertEquals(8, only(_service.getMonthEvents(MARCH, T, false), "t1").get("playerCount"));
        assertEquals(1, _service.getItemCount());
    }

    // --- caching -------------------------------------------------------------------------------------------

    @Test
    public void aMonthIsOnlyQueriedOncePerKind() {
        stubTournaments(tournament("t1", "March Open", LocalDateTime.of(2026, 3, 8, 19, 0), "lotr_block", 5));

        var first = _service.getMonth(MARCH, T);
        var second = _service.getMonth(MARCH, T);
        _service.getMonth(MARCH, L);
        _service.getMonth(MARCH, L);

        assertSame(first, second);
        Mockito.verify(_tournamentDao, Mockito.times(1)).getFinishedTournamentsBetween(Mockito.any(), Mockito.any());
        Mockito.verify(_leagueDao, Mockito.times(1)).loadLeaguesEndingBetween(Mockito.any(), Mockito.any());
        assertEquals(2, _service.getItemCount());               // one entry per (month, kind)
    }

    @Test
    public void clearingTheCacheMakesTheNextRequestQueryAgain() {
        _service.getMonth(MARCH, T);
        assertEquals(1, _service.getItemCount());

        _service.clearCache();
        assertEquals(0, _service.getItemCount());

        _service.getMonth(MARCH, T);
        Mockito.verify(_tournamentDao, Mockito.times(2)).getFinishedTournamentsBetween(Mockito.any(), Mockito.any());
    }

    @Test
    public void theCacheManagerEmptiesIt() {
        // The admin panel's "Clear Server Cache" only calls CacheManager.clearCaches(); registration must be enough.
        CacheManager cacheManager = new CacheManager();
        cacheManager.addCache(_service);

        _service.getMonth(MARCH, T);
        _service.getMonth(YearMonth.of(2026, 4), L);
        _service.getAvailableMonths(T);
        _service.getAvailableMonths(L);
        assertEquals(4, cacheManager.getTotalCount());

        cacheManager.clearCaches();

        assertEquals(0, cacheManager.getTotalCount());
        assertEquals(0, _service.getItemCount());
    }

    // --- the months list -----------------------------------------------------------------------------------

    @Test
    public void theMonthsListIsPerKind() {
        // 2026-02 holds only leagues, 2026-01 only tournaments.
        Mockito.when(_tournamentDao.getFinishedTournamentMonths()).thenReturn(List.of("2026-03", "2026-01", "2025-11"));
        Mockito.when(_leagueDao.getLeagueEndMonths(Mockito.any())).thenReturn(List.of("2026-02", "2026-03"));

        assertEquals(List.of("2026-03", "2026-01", "2025-11"), _service.getAvailableMonths(T));
        assertEquals(List.of("2026-03", "2026-02"), _service.getAvailableMonths(L));
        assertFalse("A league-only month must not be offered on the tournament panel",
                _service.getAvailableMonths(T).contains("2026-02"));
        assertFalse(_service.getAvailableMonths(L).contains("2026-01"));
    }

    @Test
    public void theTournamentMonthsListNeverQueriesLeaguesAndViceVersa() {
        Mockito.when(_tournamentDao.getFinishedTournamentMonths()).thenReturn(List.of("2026-03"));

        _service.getAvailableMonths(T);
        Mockito.verify(_leagueDao, Mockito.never()).getLeagueEndMonths(Mockito.any());

        _service.getAvailableMonths(L);
        Mockito.verify(_tournamentDao, Mockito.times(1)).getFinishedTournamentMonths();
    }

    @Test
    public void theLeagueMonthsListOnlyCountsLeaguesThatHaveEnded() {
        _service.getAvailableMonths(L);

        var before = ArgumentCaptor.forClass(LocalDate.class);
        Mockito.verify(_leagueDao).getLeagueEndMonths(before.capture());
        assertEquals(LocalDate.now(java.time.ZoneOffset.UTC), before.getValue());
    }

    @Test
    public void theMonthsListIsCachedPerKindAndSkipsNullRows() {
        Mockito.when(_tournamentDao.getFinishedTournamentMonths()).thenReturn(java.util.Arrays.asList("2026-03", null, "2026-01", "2026-03"));

        assertEquals(List.of("2026-03", "2026-01"), _service.getAvailableMonths(T));
        _service.getAvailableMonths(T);
        Mockito.verify(_tournamentDao, Mockito.times(1)).getFinishedTournamentMonths();

        _service.clearCache();
        _service.getAvailableMonths(T);
        Mockito.verify(_tournamentDao, Mockito.times(2)).getFinishedTournamentMonths();
    }

    @Test
    public void aFailingMonthsQueryPropagatesAndIsNotCached() {
        Mockito.when(_tournamentDao.getFinishedTournamentMonths())
                .thenThrow(new RuntimeException("database is down"))
                .thenReturn(List.of("2026-02"));

        assertThrows(RuntimeException.class, () -> _service.getAvailableMonths(T));
        assertEquals(List.of("2026-02"), _service.getAvailableMonths(T));
    }
}
