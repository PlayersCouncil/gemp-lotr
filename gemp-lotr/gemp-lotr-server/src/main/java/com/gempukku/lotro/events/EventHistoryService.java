package com.gempukku.lotro.events;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.FormatNames;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueParams;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.TournamentDAO;
import com.gempukku.lotro.tournament.TournamentParams;
import com.gempukku.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One month of completed events at a time, for the event browser (GET /eventHistory).
 * <p>
 * <b>The bucketing is deliberately asymmetric.</b>  {@code league} has a real {@code end_date}, so leagues are
 * bucketed by the month they <i>ended</i> in.  {@code tournament} has no finish column at all - the only temporal
 * column is {@code start_date}, and "finished" is the string {@code stage = 'FINISHED'} - so tournaments are
 * bucketed by the month they <i>started</i> in.  In practice a tournament starts and finishes the same day, so the
 * two agree; a league spanning March into April, however, appears only under April.  Adding a finish column is a
 * separate decision for the maintainer, so nothing here assumes one.
 * <p>
 * Everything is read from raw rows.  Hydrating a {@code Tournament} costs about six queries and deserialises every
 * stored decklist, which a month listing must never pay, and it would also disturb the live tournament cache.
 * <p>
 * A single bad row must never take out a whole month: parameter JSON that no longer parses, a league whose stored
 * definition can no longer be built and a format code that has since been retired are all caught per event and
 * either degraded (the raw format code stands in for its name) or skipped with a warning.
 */
public class EventHistoryService implements Cached {
    private static final Logger _log = LogManager.getLogger(EventHistoryService.class);

    public static final String KIND_LEAGUE = "league";
    public static final String KIND_TOURNAMENT = "tournament";

    private final LeagueDAO _leagueDao;
    private final TournamentDAO _tournamentDao;
    private final ProductLibrary _productLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;

    /**
     * Keyed by {@link #cacheKey} - "yyyy-MM/kind" - so that a tournament panel never pays for the league queries and
     * vice versa.  Values are immutable lists of plain value objects, never live domain objects.
     */
    private final Map<String, List<EventSummary>> _months = new ConcurrentHashMap<>();
    /** Keyed by kind: the months holding at least one completed event of that kind, newest first. */
    private final Map<String, List<String>> _availableMonths = new ConcurrentHashMap<>();
    /**
     * When each "live" entry was cached: the month lists, and the current month's events.  Those change as events
     * finish, so they are re-read after {@link #LIVE_TTL_MS}; a month that is over never changes and is kept.
     */
    private final Map<String, Long> _liveCachedAt = new ConcurrentHashMap<>();
    static final long LIVE_TTL_MS = 5 * 60 * 1000L;

    public EventHistoryService(LeagueDAO leagueDao, TournamentDAO tournamentDao, ProductLibrary productLibrary,
                               LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        _leagueDao = leagueDao;
        _tournamentDao = tournamentDao;
        _productLibrary = productLibrary;
        _formatLibrary = formatLibrary;
        _soloDraftDefinitions = soloDraftDefinitions;
    }

    @Override
    public void clearCache() {
        _months.clear();
        _availableMonths.clear();
        _liveCachedAt.clear();
    }

    private boolean isFresh(String liveKey) {
        Long at = _liveCachedAt.get(liveKey);
        return at != null && System.currentTimeMillis() - at < LIVE_TTL_MS;
    }

    @Override
    public int getItemCount() {
        return _months.size() + _availableMonths.size();
    }

    /**
     * The canonical kind ({@link #KIND_LEAGUE} or {@link #KIND_TOURNAMENT}), or null when the text is missing or
     * names neither - which the handler turns into the contract's 400.
     */
    public static String parseKind(String value) {
        if (value == null)
            return null;
        String kind = value.trim().toLowerCase(Locale.ROOT);
        return KIND_LEAGUE.equals(kind) || KIND_TOURNAMENT.equals(kind) ? kind : null;
    }

    /**
     * A month, or null when the text is missing or is not a {@code yyyy-MM} month - which the handler turns into
     * the contract's 400.  Here rather than in the handler so that it can be tested: the async module has no test
     * tree at all.
     */
    public static YearMonth parseMonth(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException exp) {
            return null;
        }
    }

    /**
     * Every month holding at least one completed event of {@code kind}, newest first.  Cached per kind for
     * {@link #LIVE_TTL_MS}, so a new month appears once its first event completes.  A failing
     * query propagates (the endpoint then fails) and nothing is cached, rather than caching an empty list that would
     * hide every month until the next cache clear.
     */
    public List<String> getAvailableMonths(String kind) {
        requireKind(kind);
        List<String> cached = _availableMonths.get(kind);
        if (cached != null && isFresh("months/" + kind))
            return cached;

        List<String> rows = KIND_TOURNAMENT.equals(kind)
                ? _tournamentDao.getFinishedTournamentMonths()
                : _leagueDao.getLeagueEndMonths(today());

        var months = new TreeSet<String>(Comparator.reverseOrder());
        if (rows != null) {
            // A DATE_FORMAT over a nullable column can hand back a null; a reverse-ordered set will not take one.
            for (String month : rows) {
                if (month != null && !month.isBlank())
                    months.add(month);
            }
        }

        List<String> result = List.copyOf(months);
        _availableMonths.put(kind, result);
        _liveCachedAt.put("months/" + kind, System.currentTimeMillis());
        return result;
    }

    /**
     * Every completed event of {@code kind} in the given month, sorted by start date descending and then by name.
     * Cached per (month, kind): the second call does not query.  A finished month is cached for good; the current
     * month is re-read every {@link #LIVE_TTL_MS}, so events finishing later in it appear within minutes.
     * <p>
     * If the month's main query fails, the exception propagates and nothing is cached.  If only the player counts
     * cannot be read, the events are returned with null counts but not cached, so the next request tries again.
     */
    public List<EventSummary> getMonth(YearMonth month, String kind) {
        requireKind(kind);
        String key = cacheKey(month, kind);
        YearMonth current = YearMonth.from(today());
        boolean over = month.isBefore(current);
        List<EventSummary> cached = _months.get(key);
        if (cached != null && (over || isFresh(key)))
            return cached;

        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        var events = new ArrayList<EventSummary>();
        boolean complete = KIND_TOURNAMENT.equals(kind)
                ? addTournaments(events, from, to)
                : addLeagues(events, from, to);

        events.sort(Comparator.comparing((EventSummary event) -> event.startDate).reversed()
                .thenComparing(event -> event.name == null ? "" : event.name));

        List<EventSummary> result = Collections.unmodifiableList(events);
        // a finished month is kept; the current one is re-read after LIVE_TTL_MS; a future one is never cached (the
        // month comes from the request, so caching those would let the map grow without limit)
        if (complete && !month.isAfter(current)) {
            _months.put(key, result);
            if (!over)
                _liveCachedAt.put(key, System.currentTimeMillis());
        }
        return result;
    }

    /**
     * One month of completed events of one kind in the shape the endpoint returns them (contract v2: no per-event
     * kind, no detailUrl).  The admin extras are added on the way out rather than cached, so that one cached month
     * serves admins and everyone else alike.
     */
    public List<Map<String, Object>> getMonthEvents(YearMonth month, String kind, boolean isAdmin) {
        var events = new ArrayList<Map<String, Object>>();
        for (EventSummary summary : getMonth(month, kind)) {
            var event = new LinkedHashMap<String, Object>();
            event.put("id", summary.id);
            event.put("name", summary.name);
            event.put("startDate", summary.startDate);
            event.put("endDate", summary.endDate);
            event.put("format", summary.format);
            event.put("playerCount", summary.playerCount);
            event.put("rounds", summary.rounds);
            if (isAdmin)
                event.put("adminLinks", adminLinks(summary));
            events.add(event);
        }
        return events;
    }

    static String cacheKey(YearMonth month, String kind) {
        return month + "/" + kind;
    }

    private static void requireKind(String kind) {
        if (!KIND_LEAGUE.equals(kind) && !KIND_TOURNAMENT.equals(kind))
            throw new IllegalArgumentException("Unknown event kind '" + kind + "'");
    }

    /**
     * The admin-only extras of the two displays this browser replaces.  Every entry carries {@code text} (what is
     * displayed) and {@code url} (null when it is not a link): a tournament's forum-postable HTML report, shown as
     * the tournament id as the old display did, and a league's code, which is plain text because leagues have no
     * report page.  Both go to anyone who is ADMIN or LEAGUE_ADMIN.
     */
    private static List<Map<String, Object>> adminLinks(EventSummary summary) {
        var link = new LinkedHashMap<String, Object>();
        if (KIND_TOURNAMENT.equals(summary.kind)) {
            link.put("label", "Report");
            link.put("text", summary.id);
            link.put("url", "/gemp-lotr-server/tournament/" + summary.id + "/report/html");
        } else {
            link.put("label", "Code");
            link.put("text", summary.id);
            link.put("url", null);
        }
        return List.of(link);
    }

    /** @return false when the player counts could not be read (the rows are then not to be cached) */
    private boolean addTournaments(List<EventSummary> events, LocalDate from, LocalDate to) {
        // Half-open range: start_date is a DATETIME, so an inclusive upper bound would drop every tournament
        // that started after midnight on the last day of the month.
        ZonedDateTime fromTime = DateUtils.ParseDate(from);
        ZonedDateTime toTime = DateUtils.ParseDate(to);

        List<DBDefs.Tournament> rows = _tournamentDao.getFinishedTournamentsBetween(fromTime, toTime);
        Map<String, Integer> playerCounts;
        boolean complete = true;
        try {
            playerCounts = _tournamentDao.getFinishedTournamentPlayerCountsBetween(fromTime, toTime);
        } catch (Exception exp) {
            _log.warn("Unable to count tournament players for " + from + " - " + to + "; counts will be null", exp);
            playerCounts = Map.of();
            complete = false;
        }
        if (rows == null)
            return complete;
        if (playerCounts == null)
            playerCounts = Map.of();

        for (DBDefs.Tournament row : rows) {
            try {
                // A finished tournament with no round played is not a real event (a solo draft against bots,
                // for instance).  The old "load finished tournaments" list excluded these too.
                if (row.round == 0)
                    continue;

                events.add(new EventSummary(
                        KIND_TOURNAMENT,
                        row.tournament_id,
                        row.name,
                        row.start_date.toLocalDate().toString(),
                        null,   // tournament has no finish column; see the class comment
                        tournamentFormat(row),
                        playerCounts.get(row.tournament_id),
                        row.round));
            } catch (Exception exp) {
                _log.warn("Skipping tournament " + row.tournament_id + " in the event history: " + exp.getMessage(), exp);
            }
        }
        return complete;
    }

    /** @return false when the participant counts could not be read (the rows are then not to be cached) */
    private boolean addLeagues(List<EventSummary> events, LocalDate from, LocalDate to) {
        LocalDate today = today();

        List<DBDefs.League> rows = _leagueDao.loadLeaguesEndingBetween(from, to);
        Map<String, Integer> playerCounts;
        boolean complete = true;
        try {
            playerCounts = _leagueDao.getParticipantCountsForLeaguesEndingBetween(from, to);
        } catch (Exception exp) {
            _log.warn("Unable to count league participants for " + from + " - " + to + "; counts will be null", exp);
            playerCounts = Map.of();
            complete = false;
        }
        if (rows == null)
            return complete;
        if (playerCounts == null)
            playerCounts = Map.of();

        for (DBDefs.League row : rows) {
            try {
                // A league is complete once its end date has passed.  The status column is only ever advanced
                // while the league is still active, so it cannot be used to answer this.
                if (row.end_date == null || !row.end_date.isBefore(today))
                    continue;

                String code = String.valueOf(row.code);
                events.add(new EventSummary(
                        KIND_LEAGUE,
                        code,
                        row.name,
                        row.start_date == null ? row.end_date.toString() : row.start_date.toString(),
                        row.end_date.toString(),
                        leagueFormat(row),
                        playerCounts.get(code),
                        null));  // leagues have series, not rounds
            } catch (Exception exp) {
                _log.warn("Skipping league " + row.code + " in the event history: " + exp.getMessage(), exp);
            }
        }
        return complete;
    }

    private String tournamentFormat(DBDefs.Tournament row) {
        String code = null;
        try {
            TournamentParams params = JsonUtils.Convert(row.parameters, TournamentParams.class);
            if (params != null)
                code = params.format;
        } catch (Exception exp) {
            _log.debug("Unable to parse the parameters of tournament " + row.tournament_id, exp);
        }
        return formatName(code, row.type);
    }

    private String leagueFormat(DBDefs.League row) {
        try {
            List<LeagueSerieInfo> series = new League(row)
                    .getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getSeries();
            if (series != null && !series.isEmpty()) {
                LeagueSerieInfo first = series.getFirst();
                LotroFormat format = first.getFormat();
                if (format != null && format.getName() != null && !format.getName().isBlank())
                    return format.getName();
                if (first.getFormatCode() != null && !first.getFormatCode().isBlank())
                    return first.getFormatCode();
            }
        } catch (Exception exp) {
            // An old league whose stored definition references a product or format that no longer exists.
            _log.debug("Unable to build the definition of league " + row.code + " for its format name", exp);
        }
        return formatName(rawLeagueFormatCode(row.parameters), row.type);
    }

    /**
     * The format code of a league's first series, straight out of the stored parameters, for the case where the
     * league definition itself will no longer build.  Handles both the JSON parameters and the legacy
     * comma-separated ones ({@code 20240726,default,0.7,1,1,pc_movie,3,6}, format code at index 5).
     */
    private static String rawLeagueFormatCode(String parameters) {
        if (parameters == null || parameters.isBlank())
            return null;
        try {
            if (parameters.contains("{")) {
                LeagueParams params = JsonUtils.Convert(parameters, LeagueParams.class);
                if (params != null && params.series != null && !params.series.isEmpty())
                    return params.series.getFirst().format();
                return null;
            }
            String[] parts = parameters.split(",");
            return parts.length > 5 ? parts[5].trim() : null;
        } catch (Exception exp) {
            return null;
        }
    }

    /**
     * The human-readable name of a format code, the raw code when the format has since been retired from the
     * library, and {@code fallback} (the event's type) when there is no code at all.  Never null, never throws.
     */
    private String formatName(String code, String fallback) {
        if (code != null && !code.isBlank())
            return FormatNames.nameOrCode(_formatLibrary, code);
        return fallback == null || fallback.isBlank() ? FormatNames.UNKNOWN : fallback;
    }

    private static LocalDate today() {
        return DateUtils.Today().toLocalDate();
    }
}
