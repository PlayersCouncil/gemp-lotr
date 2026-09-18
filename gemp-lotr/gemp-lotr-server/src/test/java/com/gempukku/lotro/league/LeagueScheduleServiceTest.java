package com.gempukku.lotro.league;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueScheduleDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LeagueScheduleServiceTest extends AbstractAtTest {
    private LeagueDAO _leagueDao;
    private LeagueService _leagueService;
    private LeagueScheduleDAO _scheduleDao;
    private LeagueScheduleService _service;

    @Before
    public void setUp() {
        var collectionsManager = new CollectionsManager(null, null, null, _cardLibrary, _productLibrary);
        var soloDraftDefinitions = new SoloDraftDefinitions(collectionsManager, _cardLibrary, _formatLibrary);
        _leagueDao = Mockito.mock(LeagueDAO.class);
        _leagueService = Mockito.mock(LeagueService.class);
        Mockito.when(_leagueService.getActiveLeagues()).thenReturn(List.of());
        Mockito.when(_leagueDao.addLeague(Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any())).thenReturn(100, 101, 102, 103);
        var factory = new LeagueFactory(_cardLibrary, _productLibrary, _formatLibrary, soloDraftDefinitions, _leagueDao, _leagueService);
        _scheduleDao = Mockito.mock(LeagueScheduleDAO.class);
        _service = new LeagueScheduleService(_scheduleDao, factory, _leagueService);
    }

    // ------------------------------------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------------------------------------

    private static String constructedTemplate() {
        var template = new JSONObject();
        template.put("cost", 50);
        template.put("maxRepeatMatches", 2);
        template.put("description", "Rotating constructed league");
        template.put("collectionName", "default");
        template.put("series", List.of(serie("fotr_block", 28, 10)));
        return template.toJSONString();
    }

    private static JSONObject serie(String format, int duration, int matches) {
        var obj = new JSONObject();
        obj.put("format", format);
        obj.put("duration", duration);
        obj.put("matches", matches);
        return obj;
    }

    private static String events(Object... nameAndOverridePairs) {
        var events = new ArrayList<LeagueSchedule.Event>();
        for (int i = 0; i < nameAndOverridePairs.length; i += 2) {
            events.add(new LeagueSchedule.Event((String) nameAndOverridePairs[i], (JSONObject) nameAndOverridePairs[i + 1]));
        }
        return LeagueSchedule.serializeEvents(events);
    }

    private static JSONObject override(String key, Object value) {
        var obj = new JSONObject();
        obj.put(key, value);
        return obj;
    }

    /**
     * A two-event rotating constructed schedule: Fellowship Block then Towers Block (shorter), monthly on the 1st.
     */
    private static DBDefs.LeagueSchedule rotatingSchedule() {
        var row = new DBDefs.LeagueSchedule();
        row.id = 7;
        row.name = "Constructed";
        row.league_type = "CONSTRUCTED";
        row.template = constructedTemplate();
        row.events = events(
                "Fellowship Block", new JSONObject(),
                "Towers Block", override("series", List.of(serie("movie", 14, 8))));
        row.name_pattern = "{series} - {event}";
        row.next_event_date = LocalDate.of(2026, 10, 1);
        row.next_event_index = 0;
        row.interval_months = 1;
        row.lead_days = 7;
        row.active = true;
        return row;
    }

    private static ZonedDateTime at(int year, int month, int day) {
        return DateUtils.DateOf(year, month, day);
    }

    // ------------------------------------------------------------------------------------------------
    // Name rendering and date arithmetic
    // ------------------------------------------------------------------------------------------------

    @Test
    public void namePatternTokensAreFilledIn() {
        var date = LocalDate.of(2026, 9, 1);
        assertEquals("Sealed - King Block", LeagueScheduleService.renderName("{series} - {event}", "Sealed", "King Block", date));
        assertEquals("Draft September 2026", LeagueScheduleService.renderName("{series} {month} {year}", "Draft", "x", date));
        assertEquals("Sep 26 / 09 / 2026", LeagueScheduleService.renderName("{mon} {yy} / {mm} / {yyyy}", "", "", date));
        assertEquals("Sealed - King Block", LeagueScheduleService.renderName(null, "Sealed", "King Block", date));
        assertEquals("{unknown} kept", LeagueScheduleService.renderName("{unknown} kept", "", "", date));
    }

    @Test
    public void wholeMonthsAdvanceByCalendarMonth() {
        assertEquals(LocalDate.of(2026, 10, 1), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 1));
        assertEquals(LocalDate.of(2026, 12, 1), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 3));
        // end-of-month clamping is java.time's: Jan 31 + 1 month = Feb 28
        assertEquals(LocalDate.of(2027, 2, 28), LeagueScheduleService.advance(LocalDate.of(2027, 1, 31), 1));
    }

    @Test
    public void fractionalMonthsAdvanceByWeeks() {
        assertEquals(LocalDate.of(2026, 9, 8), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 0.25));
        assertEquals(LocalDate.of(2026, 9, 15), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 0.5));
        assertEquals(LocalDate.of(2026, 10, 15), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 1.5));
    }

    @Test
    public void aTinyIntervalStillMovesForward() {
        assertEquals(LocalDate.of(2026, 9, 2), LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 0.001));
        try {
            LeagueScheduleService.advance(LocalDate.of(2026, 9, 1), 0);
            fail("zero interval must be rejected");
        } catch (IllegalArgumentException expected) {
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Event parameter assembly
    // ------------------------------------------------------------------------------------------------

    @Test
    public void eventOverridesReplaceTemplateKeysOnly() {
        var schedule = new LeagueSchedule(rotatingSchedule());

        var first = LeagueScheduleService.buildEventParams(schedule, 0, LocalDate.of(2026, 10, 1));
        assertEquals("Constructed - Fellowship Block", first.name);
        assertEquals(LocalDate.of(2026, 10, 1).atStartOfDay(), first.start);
        assertEquals(50, first.cost);
        assertEquals(2, first.maxRepeatMatches);
        assertEquals(1, first.series.size());
        assertEquals("fotr_block", first.series.getFirst().format());
        assertEquals(28, first.series.getFirst().duration());
        assertEquals(0, first.code);

        var second = LeagueScheduleService.buildEventParams(schedule, 1, LocalDate.of(2026, 11, 1));
        assertEquals("Constructed - Towers Block", second.name);
        assertEquals(50, second.cost);                         // untouched by the override
        assertEquals("movie", second.series.getFirst().format());
        assertEquals(14, second.series.getFirst().duration()); // overridden
        assertEquals(8, second.series.getFirst().matches());
    }

    @Test
    public void templateNameStartAndCodeAreIgnored() {
        var row = rotatingSchedule();
        var template = JSONObject.parseObject(row.template);
        template.put("name", "should not leak");
        template.put("code", 999);
        template.put("start", "2020-01-01T00:00:00");
        row.template = template.toJSONString();

        var params = LeagueScheduleService.buildEventParams(new LeagueSchedule(row), 0, LocalDate.of(2026, 10, 1));
        assertEquals("Constructed - Fellowship Block", params.name);
        assertEquals(0, params.code);
        assertEquals(LocalDate.of(2026, 10, 1).atStartOfDay(), params.start);
    }

    // ------------------------------------------------------------------------------------------------
    // Due detection and materialisation
    // ------------------------------------------------------------------------------------------------

    @Test
    public void notDueBeforeTheLeadWindow() {
        var row = rotatingSchedule();
        assertFalse(LeagueScheduleService.isDue(row, at(2026, 9, 23)));
        assertTrue(LeagueScheduleService.isDue(row, at(2026, 9, 24)));
        assertTrue(LeagueScheduleService.isDue(row, at(2026, 10, 1)));
        assertTrue(LeagueScheduleService.isDue(row, at(2026, 10, 15)));
    }

    @Test
    public void nothingHappensBeforeTheLeadWindow() {
        var row = rotatingSchedule();
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 20));

        assertTrue(created.isEmpty());
        Mockito.verify(_leagueDao, Mockito.never()).addLeague(Mockito.anyString(), Mockito.anyLong(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any());
        Mockito.verify(_scheduleDao, Mockito.never()).updateProgress(Mockito.any());
    }

    @Test
    public void dueScheduleCreatesTheLeagueAndAdvances() {
        var row = rotatingSchedule();
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 25));

        assertEquals(1, created.size());
        var prepared = created.getFirst();
        assertEquals("Constructed - Fellowship Block", prepared.params().name);
        assertEquals(at(2026, 10, 1), prepared.start());
        assertEquals(League.LeagueType.CONSTRUCTED, prepared.type());
        assertTrue(prepared.params().code > 0);

        var paramsCaptor = ArgumentCaptor.forClass(LeagueParams.class);
        Mockito.verify(_leagueDao).addLeague(Mockito.eq("Constructed - Fellowship Block"), Mockito.anyLong(),
                Mockito.eq(League.LeagueType.CONSTRUCTED), paramsCaptor.capture(), Mockito.eq(at(2026, 10, 1)),
                Mockito.eq(at(2026, 10, 28).plusDays(LeagueFactory.DISPLAY_END_GRACE_DAYS)), Mockito.eq(50), Mockito.eq(7));
        assertSame(prepared.params(), paramsCaptor.getValue());
        Mockito.verify(_leagueService).clearCache();

        // The row moved on to the next event, one month later, and recorded what it made
        assertEquals(1, row.next_event_index);
        assertEquals(LocalDate.of(2026, 11, 1), row.next_event_date);
        assertEquals(Integer.valueOf(100), row.last_created_league_id);
        assertNull(row.last_error);
        assertNotNull(row.last_run);
        Mockito.verify(_scheduleDao).updateProgress(row);
    }

    @Test
    public void rotationWrapsAroundToTheFirstEvent() {
        var row = rotatingSchedule();
        row.next_event_index = 1;
        row.next_event_date = LocalDate.of(2026, 11, 1);
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 10, 28));

        assertEquals(1, created.size());
        assertEquals("Constructed - Towers Block", created.getFirst().params().name);
        assertEquals(0, row.next_event_index);
        assertEquals(LocalDate.of(2026, 12, 1), row.next_event_date);
    }

    @Test
    public void aScheduleThatFellBehindCatchesUpWithACap() {
        var row = rotatingSchedule();
        row.next_event_date = LocalDate.of(2026, 3, 1);
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 25));

        assertEquals(LeagueScheduleService.MAX_CATCH_UP_PER_RUN, created.size());
        assertEquals("Constructed - Fellowship Block", created.get(0).params().name);
        assertEquals("Constructed - Towers Block", created.get(1).params().name);
        assertEquals("Constructed - Fellowship Block", created.get(2).params().name);
        assertEquals(LocalDate.of(2026, 6, 1), row.next_event_date);
        assertEquals(1, row.next_event_index);
    }

    @Test
    public void inactiveSchedulesAreSkipped() {
        var row = rotatingSchedule();
        row.active = false;
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        assertTrue(_service.processDueSchedules(at(2026, 10, 1)).isEmpty());
        Mockito.verifyNoInteractions(_leagueDao);
    }

    @Test
    public void anActiveLeagueWithTheSameNameBlocksCreation() {
        var existing = new League("Constructed - Fellowship Block", 50, 1, League.LeagueType.CONSTRUCTED, "{}", 0);
        Mockito.when(_leagueService.getActiveLeagues()).thenReturn(List.of(existing));
        var row = rotatingSchedule();
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 25));

        assertTrue(created.isEmpty());
        Mockito.verifyNoInteractions(_leagueDao);
        // Position is kept so the admin can sort it out; the reason is recorded
        assertEquals(0, row.next_event_index);
        assertEquals(LocalDate.of(2026, 10, 1), row.next_event_date);
        assertNotNull(row.last_error);
        assertTrue(row.last_error, row.last_error.contains("already active"));
        Mockito.verify(_scheduleDao).updateProgress(row);
    }

    @Test
    public void anInvalidEventDefinitionIsRecordedNotThrown() {
        var row = rotatingSchedule();
        row.events = events("Broken", override("series", List.of(serie("no_such_format", 7, 5))));
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 25));

        assertTrue(created.isEmpty());
        Mockito.verifyNoInteractions(_leagueDao);
        assertNotNull(row.last_error);
        assertTrue(row.last_error, row.last_error.contains("no_such_format"));
        assertEquals(LocalDate.of(2026, 10, 1), row.next_event_date);
    }

    @Test
    public void malformedTemplateJsonIsRecordedNotThrown() {
        var row = rotatingSchedule();
        row.template = "{ this is not json";
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        var created = _service.processDueSchedules(at(2026, 9, 25));

        assertTrue(created.isEmpty());
        assertNotNull(row.last_error);
        Mockito.verifyNoInteractions(_leagueDao);
    }

    @Test
    public void runNowIgnoresTheLeadTime() {
        var row = rotatingSchedule();
        Mockito.when(_scheduleDao.getSchedule(7)).thenReturn(row);

        var created = _service.runNow(7);

        assertNotNull(created);
        assertEquals("Constructed - Fellowship Block", created.params().name);
        assertEquals(1, row.next_event_index);
        assertEquals(LocalDate.of(2026, 11, 1), row.next_event_date);
    }

    @Test
    public void dailyEntryPointOnlyRunsOncePerDay() {
        var row = rotatingSchedule();
        row.next_event_date = DateUtils.Today().toLocalDate();
        Mockito.when(_scheduleDao.getAllSchedules()).thenReturn(List.of(row));

        _service.processDueSchedulesDaily();
        _service.processDueSchedulesDaily();

        Mockito.verify(_scheduleDao, Mockito.times(1)).getAllSchedules();
    }

    // ------------------------------------------------------------------------------------------------
    // Definition validation (admin save)
    // ------------------------------------------------------------------------------------------------

    private void assertDefinitionRejected(DBDefs.LeagueSchedule row, String expectedParameter) {
        try {
            _service.validateDefinition(row);
            fail("Expected rejection on " + expectedParameter);
        } catch (LeagueDefinitionException exp) {
            assertEquals(exp.getMessage(), expectedParameter, exp.getParameter());
        }
    }

    @Test
    public void aValidDefinitionIsAccepted() throws LeagueDefinitionException {
        _service.validateDefinition(rotatingSchedule());
        Mockito.verifyNoInteractions(_leagueDao);
    }

    @Test
    public void definitionRules() {
        var row = rotatingSchedule();
        row.name = "";
        assertDefinitionRejected(row, "name");

        row = rotatingSchedule();
        row.league_type = "BOGUS";
        assertDefinitionRejected(row, "leagueType");

        row = rotatingSchedule();
        row.next_event_date = null;
        assertDefinitionRejected(row, "nextEventDate");

        row = rotatingSchedule();
        row.interval_months = 0;
        assertDefinitionRejected(row, "intervalMonths");

        row = rotatingSchedule();
        row.lead_days = -1;
        assertDefinitionRejected(row, "leadDays");

        row = rotatingSchedule();
        row.events = "[]";
        assertDefinitionRejected(row, "events");

        row = rotatingSchedule();
        row.next_event_index = 2;
        assertDefinitionRejected(row, "nextEventIndex");

        row = rotatingSchedule();
        row.template = "not json";
        assertDefinitionRejected(row, "template");
    }

    @Test
    public void everyEventMustProduceAValidLeague() {
        var row = rotatingSchedule();
        row.events = events(
                "Fellowship Block", new JSONObject(),
                "Broken", override("series", List.of(serie("no_such_format", 7, 5))));
        assertDefinitionRejected(row, "events[1].series[0].format");
    }

    @Test
    public void saveStoresAndReloads() throws LeagueDefinitionException {
        var row = rotatingSchedule();
        row.id = 0;
        Mockito.when(_scheduleDao.saveSchedule(row)).thenReturn(55);
        var stored = rotatingSchedule();
        stored.id = 55;
        Mockito.when(_scheduleDao.getSchedule(55)).thenReturn(stored);

        var schedule = _service.saveSchedule(row);

        assertEquals(55, schedule.getId());
        assertEquals("Constructed", schedule.getName());
        assertEquals(2, schedule.getEvents().size());
    }

    // ------------------------------------------------------------------------------------------------
    // Projection
    // ------------------------------------------------------------------------------------------------

    @Test
    public void projectionWalksTheRotationWithoutCreatingAnything() {
        var schedule = new LeagueSchedule(rotatingSchedule());

        var events = _service.project(schedule, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31), 10);

        assertEquals(3, events.size());
        assertEquals("Constructed - Towers Block", events.get(0).leagueName());
        assertEquals(LocalDate.of(2026, 11, 1), events.get(0).start());
        assertEquals(LocalDate.of(2026, 11, 14), events.get(0).end());
        assertEquals(LocalDate.of(2026, 10, 25), events.get(0).createdOn());
        assertNull(events.get(0).error());
        assertEquals("Constructed - Fellowship Block", events.get(1).leagueName());
        assertEquals(LocalDate.of(2026, 12, 28), events.get(1).end());
        assertEquals("Constructed - Towers Block", events.get(2).leagueName());
        assertEquals(LocalDate.of(2027, 1, 1), events.get(2).start());
        Mockito.verifyNoInteractions(_leagueDao);
    }

    @Test
    public void projectionReportsBrokenEventsInsteadOfFailing() {
        var row = rotatingSchedule();
        row.events = events(
                "Fellowship Block", new JSONObject(),
                "Broken", override("series", List.of(serie("no_such_format", 7, 5))));
        var events = _service.projectNext(new LeagueSchedule(row), 2);

        assertEquals(2, events.size());
        assertNull(events.get(0).error());
        assertNotNull(events.get(1).error());
        assertEquals("Constructed - Broken", events.get(1).leagueName());
        assertEquals(LocalDate.of(2026, 11, 1), events.get(1).start());
    }
}
