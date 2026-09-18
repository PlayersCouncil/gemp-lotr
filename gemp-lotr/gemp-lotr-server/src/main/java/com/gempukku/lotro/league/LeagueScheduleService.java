package com.gempukku.lotro.league;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueScheduleDAO;
import com.gempukku.lotro.db.vo.League;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns {@link LeagueSchedule} definitions into leagues.  Once a day (or whenever asked) each active schedule whose
 * next event is within its lead time is materialised through the {@link LeagueFactory}; the schedule then advances
 * to its next event and date.  A schedule that cannot be materialised (invalid definition, or an active league with
 * the same name already exists) is left where it is with the reason recorded on it, and is retried on the next run.
 */
public class LeagueScheduleService {
    private static final Logger _log = LogManager.getLogger(LeagueScheduleService.class);

    /**
     * Upper bound on leagues created for one schedule in one run, so a schedule that fell far behind (or has a
     * tiny interval by mistake) cannot flood the league table.
     */
    public static final int MAX_CATCH_UP_PER_RUN = 3;

    private final LeagueScheduleDAO _scheduleDao;
    private final LeagueFactory _leagueFactory;
    private final LeagueService _leagueService;

    private LocalDate _lastProcessedDay;

    public LeagueScheduleService(LeagueScheduleDAO scheduleDao, LeagueFactory leagueFactory, LeagueService leagueService) {
        _scheduleDao = scheduleDao;
        _leagueFactory = leagueFactory;
        _leagueService = leagueService;
    }

    // ------------------------------------------------------------------------------------------
    // Definitions
    // ------------------------------------------------------------------------------------------

    public synchronized List<LeagueSchedule> getSchedules() {
        var result = new ArrayList<LeagueSchedule>();
        for (var row : _scheduleDao.getAllSchedules())
            result.add(new LeagueSchedule(row));
        return result;
    }

    public synchronized LeagueSchedule getSchedule(int id) {
        var row = _scheduleDao.getSchedule(id);
        return row == null ? null : new LeagueSchedule(row);
    }

    /**
     * Validates and stores a definition.  The next event is dry-run through the factory so that a template that
     * can never produce a league is rejected up front.
     * @return the stored schedule
     */
    public synchronized LeagueSchedule saveSchedule(DBDefs.LeagueSchedule row) throws LeagueDefinitionException {
        validateDefinition(row);
        int id = _scheduleDao.saveSchedule(row);
        return getSchedule(id);
    }

    public synchronized void deleteSchedule(int id) {
        _scheduleDao.deleteSchedule(id);
    }

    /**
     * Checks a definition without storing it: the row must parse, and the next event must produce a valid league.
     */
    public void validateDefinition(DBDefs.LeagueSchedule row) throws LeagueDefinitionException {
        if (StringUtils.isBlank(row.name))
            throw new LeagueDefinitionException("name", "Schedule name cannot be blank.");
        if (row.name.length() > LeagueFactory.MAX_NAME_LENGTH)
            throw new LeagueDefinitionException("name", "Schedule name must be " + LeagueFactory.MAX_NAME_LENGTH + " characters or less.");
        if (League.LeagueType.parse(row.league_type == null ? "" : row.league_type) == null)
            throw new LeagueDefinitionException("leagueType", "Unknown league type '" + row.league_type + "'.");
        if (row.next_event_date == null)
            throw new LeagueDefinitionException("nextEventDate", "The next event date must be provided.");
        if (row.interval_months <= 0)
            throw new LeagueDefinitionException("intervalMonths", "The interval between events must be positive.");
        if (row.lead_days < 0)
            throw new LeagueDefinitionException("leadDays", "Lead days cannot be negative.");

        LeagueSchedule schedule;
        try {
            schedule = new LeagueSchedule(row);
        } catch (IllegalArgumentException exp) {
            throw new LeagueDefinitionException("template", exp.getMessage());
        }
        if (schedule.getEvents().isEmpty())
            throw new LeagueDefinitionException("events", "At least one event must be defined.");
        if (row.next_event_index < 0 || row.next_event_index >= schedule.getEvents().size())
            throw new LeagueDefinitionException("nextEventIndex", "The next event index must point at one of the " + schedule.getEvents().size() + " events.");

        // Every event must be a valid league, not just the next one, otherwise the rotation breaks mid-way
        for (int i = 0; i < schedule.getEvents().size(); i++) {
            try {
                _leagueFactory.prepare(schedule.getType(), buildEventParams(schedule, i, schedule.getNextEventDate()));
            } catch (IllegalArgumentException exp) {
                throw new LeagueDefinitionException("events[" + i + "]", "Event " + (i + 1) + ": " + exp.getMessage());
            } catch (LeagueDefinitionException exp) {
                throw new LeagueDefinitionException("events[" + i + "]." + exp.getParameter(), "Event " + (i + 1) + ": " + exp.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Materialisation
    // ------------------------------------------------------------------------------------------

    /**
     * Cheap entry point for the hall's cleanup tick: does the real work at most once per (UTC) day.
     */
    public void processDueSchedulesDaily() {
        LocalDate today = DateUtils.Today().toLocalDate();
        synchronized (this) {
            if (today.equals(_lastProcessedDay))
                return;
            _lastProcessedDay = today;
        }
        try {
            processDueSchedules(DateUtils.Today());
        } catch (Exception exp) {
            _log.error("League schedule processing failed", exp);
        }
    }

    /**
     * Materialises every schedule whose next event falls within its lead time of {@code now}.
     * @return the leagues created, in order
     */
    public synchronized List<LeagueFactory.PreparedLeague> processDueSchedules(ZonedDateTime now) {
        var created = new ArrayList<LeagueFactory.PreparedLeague>();
        for (var row : _scheduleDao.getAllSchedules()) {
            if (!row.active)
                continue;
            for (int i = 0; i < MAX_CATCH_UP_PER_RUN; i++) {
                if (!isDue(row, now))
                    break;
                var result = materialize(row, now);
                if (result == null)
                    break;
                created.add(result);
            }
        }
        return created;
    }

    /**
     * Creates the next event of one schedule right now, regardless of lead time (the admin "run now" button).
     * @return the created league, or null if it could not be created (the reason is recorded on the schedule)
     */
    public synchronized LeagueFactory.PreparedLeague runNow(int scheduleId) {
        var row = _scheduleDao.getSchedule(scheduleId);
        if (row == null)
            return null;
        return materialize(row, DateUtils.Now());
    }

    public static boolean isDue(DBDefs.LeagueSchedule row, ZonedDateTime now) {
        LocalDate today = now.toLocalDate();
        return !row.next_event_date.minusDays(row.lead_days).isAfter(today);
    }

    /**
     * Creates the league for the row's next event and advances the row.  On failure the row keeps its position and
     * the error is stored; the caller decides whether to keep going.
     */
    private LeagueFactory.PreparedLeague materialize(DBDefs.LeagueSchedule row, ZonedDateTime now) {
        LeagueSchedule schedule;
        try {
            schedule = new LeagueSchedule(row);
        } catch (IllegalArgumentException exp) {
            return recordFailure(row, now, exp.getMessage());
        }
        if (schedule.getEvents().isEmpty())
            return recordFailure(row, now, "The schedule has no events.");

        int index = Math.floorMod(row.next_event_index, schedule.getEvents().size());
        LeagueParams params;
        try {
            params = buildEventParams(schedule, index, row.next_event_date);
        } catch (IllegalArgumentException exp) {
            return recordFailure(row, now, exp.getMessage());
        }

        if (isLeagueNameActive(params.name))
            return recordFailure(row, now, "A league named '" + params.name + "' is already active; not creating another. "
                    + "Rename or end that league, or adjust this schedule.");

        LeagueFactory.PreparedLeague prepared;
        int leagueId;
        try {
            prepared = _leagueFactory.prepare(schedule.getType(), params);
            leagueId = _leagueFactory.create(prepared, row.id);
        } catch (LeagueDefinitionException exp) {
            return recordFailure(row, now, "Event " + (index + 1) + " (" + params.name + "): " + exp.getMessage());
        } catch (RuntimeException exp) {
            _log.error("Unable to create league for schedule " + row.id, exp);
            return recordFailure(row, now, "Event " + (index + 1) + " (" + params.name + "): " + exp.getMessage());
        }

        row.next_event_index = (index + 1) % schedule.getEvents().size();
        row.next_event_date = advance(row.next_event_date, row.interval_months);
        row.last_created_league_id = leagueId;
        row.last_run = now.toLocalDateTime();
        row.last_error = null;
        _scheduleDao.updateProgress(row);

        _log.info("League schedule " + row.id + " (" + row.name + ") created league '" + params.name + "' (id " + leagueId
                + "); next event " + row.next_event_date + " index " + row.next_event_index);
        return prepared;
    }

    private LeagueFactory.PreparedLeague recordFailure(DBDefs.LeagueSchedule row, ZonedDateTime now, String message) {
        _log.warn("League schedule " + row.id + " (" + row.name + ") not materialised: " + message);
        row.last_run = now.toLocalDateTime();
        row.last_error = StringUtils.abbreviate(message, 1000);
        _scheduleDao.updateProgress(row);
        return null;
    }

    private boolean isLeagueNameActive(String name) {
        for (League league : _leagueService.getActiveLeagues()) {
            if (league.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------
    // Projection (for calendars and previews - nothing is created)
    // ------------------------------------------------------------------------------------------

    /**
     * One future occurrence of a schedule.  {@code error} is set when the event's definition does not produce a
     * valid league; {@code start}/{@code end} are then just the scheduled date.
     */
    public record ProjectedEvent(int scheduleId, String scheduleName, int eventIndex, String eventName, String leagueName,
                                 LocalDate start, LocalDate end, LocalDate createdOn, String error) {
    }

    /**
     * Lists the events a schedule would produce with a start date in [from, to], up to {@code limit} of them.
     */
    public List<ProjectedEvent> project(LeagueSchedule schedule, LocalDate from, LocalDate to, int limit) {
        var result = new ArrayList<ProjectedEvent>();
        if (schedule.getEvents().isEmpty() || schedule.getIntervalMonths() <= 0)
            return result;

        LocalDate date = schedule.getNextEventDate();
        int index = Math.floorMod(schedule.getNextEventIndex(), schedule.getEvents().size());
        int guard = 0;
        while (!date.isAfter(to) && result.size() < limit && guard++ < 1000) {
            if (!date.isBefore(from)) {
                result.add(projectOne(schedule, index, date));
            }
            date = advance(date, schedule.getIntervalMonths());
            index = (index + 1) % schedule.getEvents().size();
        }
        return result;
    }

    /**
     * The next {@code count} occurrences of a schedule starting from its current position.
     */
    public List<ProjectedEvent> projectNext(LeagueSchedule schedule, int count) {
        var result = new ArrayList<ProjectedEvent>();
        if (schedule.getEvents().isEmpty() || schedule.getIntervalMonths() <= 0)
            return result;
        LocalDate date = schedule.getNextEventDate();
        int index = Math.floorMod(schedule.getNextEventIndex(), schedule.getEvents().size());
        for (int i = 0; i < count; i++) {
            result.add(projectOne(schedule, index, date));
            date = advance(date, schedule.getIntervalMonths());
            index = (index + 1) % schedule.getEvents().size();
        }
        return result;
    }

    private ProjectedEvent projectOne(LeagueSchedule schedule, int index, LocalDate date) {
        String eventName = schedule.getEvents().get(index).name();
        LocalDate createdOn = date.minusDays(schedule.getLeadDays());
        try {
            var params = buildEventParams(schedule, index, date);
            var prepared = _leagueFactory.prepare(schedule.getType(), params);
            return new ProjectedEvent(schedule.getId(), schedule.getName(), index, eventName, params.name,
                    prepared.start().toLocalDate(), prepared.series().getLast().getEnd().toLocalDate(), createdOn, null);
        } catch (LeagueDefinitionException | IllegalArgumentException exp) {
            String leagueName = renderName(schedule.getNamePattern(), schedule.getName(), eventName, date);
            return new ProjectedEvent(schedule.getId(), schedule.getName(), index, eventName, leagueName, date, date,
                    createdOn, exp.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    /**
     * Assembles the LeagueParams for one occurrence: template + event overrides, then the generated name and the
     * start date.
     */
    public static LeagueParams buildEventParams(LeagueSchedule schedule, int eventIndex, LocalDate start) {
        LeagueParams params = schedule.buildParams(eventIndex);
        params.name = renderName(schedule.getNamePattern(), schedule.getName(), schedule.getEvents().get(eventIndex).name(), start);
        params.start = start.atStartOfDay();
        return params;
    }

    /**
     * Fills the name pattern's tokens: {series}, {event}, {month} (September), {mon} (Sep), {mm} (09), {year} / {yyyy}
     * (2026), {yy} (26).  Unknown tokens are left as they are.
     */
    public static String renderName(String pattern, String seriesName, String eventName, LocalDate date) {
        String result = StringUtils.isBlank(pattern) ? LeagueSchedule.DEFAULT_NAME_PATTERN : pattern;
        result = result.replace("{series}", seriesName == null ? "" : seriesName);
        result = result.replace("{event}", eventName == null ? "" : eventName);
        result = result.replace("{month}", date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        result = result.replace("{mon}", date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        result = result.replace("{mm}", String.format("%02d", date.getMonthValue()));
        result = result.replace("{yyyy}", String.valueOf(date.getYear()));
        result = result.replace("{year}", String.valueOf(date.getYear()));
        result = result.replace("{yy}", String.format("%02d", date.getYear() % 100));
        return result.trim();
    }

    /**
     * Adds an interval to a date.  The whole part is calendar months (so the 1st stays the 1st); the fractional
     * part is interpreted in weeks of a four-week month, so 0.25 = one week and 0.5 = two weeks.
     */
    public static LocalDate advance(LocalDate date, double intervalMonths) {
        if (intervalMonths <= 0)
            throw new IllegalArgumentException("Interval must be positive");
        int wholeMonths = (int) Math.floor(intervalMonths);
        double fraction = intervalMonths - wholeMonths;
        int days = (int) Math.round(fraction * 28);
        LocalDate result = date.plusMonths(wholeMonths).plusDays(days);
        if (result.equals(date))
            result = date.plusDays(1);
        return result;
    }
}
