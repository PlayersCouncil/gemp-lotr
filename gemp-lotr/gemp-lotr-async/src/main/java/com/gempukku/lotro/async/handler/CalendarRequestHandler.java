package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueScheduleService;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.TournamentParams;
import com.gempukku.lotro.tournament.TournamentQueue;
import com.gempukku.lotro.tournament.TournamentService;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /calendar?from=yyyy-MM-dd&to=yyyy-MM-dd
 * <p>
 * Every event that touches the requested range, as JSON: leagues (with their series), scheduled tournaments and,
 * for event admins only, the leagues that active schedules are projected to create.  The client renders the grid.
 */
public class CalendarRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private static final Logger _log = LogManager.getLogger(CalendarRequestHandler.class);

    /**
     * A calendar request may span at most this many days, to keep projections bounded.
     */
    public static final int MAX_RANGE_DAYS = 400;

    private final LeagueDAO _leagueDao;
    private final TournamentService _tournamentService;
    private final LeagueScheduleService _leagueScheduleService;
    private final ProductLibrary _productLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final MarkdownParser _markdownParser;

    public CalendarRequestHandler(Map<Type, Object> context) {
        super(context);
        _leagueDao = extractObject(context, LeagueDAO.class);
        _tournamentService = extractObject(context, TournamentService.class);
        _leagueScheduleService = extractObject(context, LeagueScheduleService.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _markdownParser = extractObject(context, MarkdownParser.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if ((uri.isEmpty() || uri.equals("/")) && request.method() == HttpMethod.GET) {
            getCalendar(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getCalendar(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        Player viewer = getResourceOwnerSafely(request, participantId);
        boolean eventAdmin = viewer.hasType(Player.Type.ADMIN) || viewer.hasType(Player.Type.LEAGUE_ADMIN);

        LocalDate from = parseDate(getQueryParameterSafely(queryDecoder, "from"), "from");
        LocalDate to = parseDate(getQueryParameterSafely(queryDecoder, "to"), "to");
        if (to.isBefore(from))
            throw new HttpProcessingException(400, "'to' must not be before 'from'.");
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to))
            throw new HttpProcessingException(400, "A calendar request may span at most " + MAX_RANGE_DAYS + " days.");

        var events = new ArrayList<Map<String, Object>>();
        addLeagues(events, from, to, viewer);
        addTournaments(events, from, to, viewer);
        if (eventAdmin)
            addProjectedLeagues(events, from, to);

        var result = new LinkedHashMap<String, Object>();
        result.put("from", from.toString());
        result.put("to", to.toString());
        result.put("eventAdmin", eventAdmin);
        result.put("events", events);
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    private static LocalDate parseDate(String value, String name) throws HttpProcessingException {
        Throw400IfBlank(value, name);
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exp) {
            throw new HttpProcessingException(400, "Parameter '" + name + "' must be a date in yyyy-MM-dd form.");
        }
    }

    private static void Throw400IfBlank(String value, String name) throws HttpProcessingException {
        if (value == null || value.isBlank())
            throw new HttpProcessingException(400, "Parameter '" + name + "' is required.");
    }

    /**
     * Leagues whose overall span overlaps the range.  The row-level start/end are used for the overlap test; the
     * series come from the league definition.  {@code joined} tells the viewer which leagues they are already in.
     */
    private void addLeagues(List<Map<String, Object>> events, LocalDate from, LocalDate to, Player viewer) throws java.sql.SQLException {
        List<League> leagues = _leagueDao.loadActiveLeagues(DateUtils.ParseDate(from));
        for (League league : leagues) {
            List<LeagueSerieInfo> series;
            try {
                series = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getSeries();
            } catch (Exception exp) {
                _log.warn("Skipping league " + league.getName() + " on the calendar: " + exp.getMessage());
                continue;
            }
            if (series == null || series.isEmpty())
                continue;

            LocalDate start = series.getFirst().getStart().toLocalDate();
            LocalDate end = series.getLast().getEnd().toLocalDate();
            if (start.isAfter(to) || end.isBefore(from))
                continue;

            var event = new LinkedHashMap<String, Object>();
            event.put("kind", "league");
            event.put("id", league.getCodeStr());
            event.put("name", league.getName());
            event.put("leagueType", league.getType() == null ? null : league.getType().toString());
            event.put("start", start.toString());
            event.put("end", end.toString());
            event.put("inviteOnly", league.inviteOnly());
            event.put("cost", league.getCost());
            event.put("description", _markdownParser.renderDescription(league.getDescription()));
            event.put("scheduleId", league.getScheduleId());
            event.put("joined", _leagueService.isPlayerInLeague(league, viewer));

            var serieList = new ArrayList<Map<String, Object>>();
            for (LeagueSerieInfo serie : series) {
                var s = new LinkedHashMap<String, Object>();
                s.put("name", serie.getName());
                s.put("start", serie.getStart().toLocalDate().toString());
                s.put("end", serie.getEnd().toLocalDate().toString());
                s.put("format", serie.getFormat() == null ? null : serie.getFormat().getName());
                s.put("limited", serie.isLimited());
                serieList.add(s);
            }
            event.put("series", serieList);
            events.add(event);
        }
    }

    /**
     * Scheduled tournaments starting in the range.  {@code joined} is true when the viewer is signed up: in the
     * tournament's queue while it is waiting to start, or among its recorded players once it has.
     */
    private void addTournaments(List<Map<String, Object>> events, LocalDate from, LocalDate to, Player viewer) {
        var tournaments = _tournamentService.getScheduledTournamentsBetween(
                DateUtils.ParseDate(from), DateUtils.ParseDate(to.plusDays(1)).minusSeconds(1));
        for (var tournament : tournaments) {
            var event = new LinkedHashMap<String, Object>();
            event.put("kind", "tournament");
            event.put("id", tournament.tournament_id);
            event.put("name", tournament.name);
            event.put("tournamentType", tournament.type);
            event.put("format", tournament.format);
            event.put("start", tournament.start_date.toLocalDate().toString());
            event.put("startTime", tournament.start_date.toString());
            event.put("end", tournament.start_date.toLocalDate().toString());
            event.put("started", tournament.started);
            event.put("cost", tournamentCost(tournament));
            event.put("joined", isSignedUp(tournament, viewer));
            events.add(event);
        }
    }

    private static Integer tournamentCost(com.gempukku.lotro.common.DBDefs.ScheduledTournament tournament) {
        try {
            TournamentParams params = JsonUtils.Convert(tournament.parameters, TournamentParams.class);
            return params == null ? null : params.cost;
        } catch (Exception exp) {
            return null;
        }
    }

    private boolean isSignedUp(com.gempukku.lotro.common.DBDefs.ScheduledTournament tournament, Player viewer) {
        try {
            TournamentQueue queue = _tournamentService.getTournamentQueue(tournament.tournament_id);
            if (queue != null)
                return queue.isPlayerSignedUp(viewer.getName());
            if (tournament.started)
                return _tournamentService.retrieveTournamentPlayers(tournament.tournament_id).contains(viewer.getName());
        } catch (Exception exp) {
            _log.debug("Could not determine sign-up state for tournament " + tournament.tournament_id, exp);
        }
        return false;
    }

    private void addProjectedLeagues(List<Map<String, Object>> events, LocalDate from, LocalDate to) {
        for (var schedule : _leagueScheduleService.getSchedules()) {
            if (!schedule.isActive())
                continue;
            // A projected league that started before the range but is still running should show too
            LocalDate lookBack = from.minusDays(120);
            for (var projected : _leagueScheduleService.project(schedule, lookBack, to, 50)) {
                if (projected.end().isBefore(from))
                    continue;
                var event = new LinkedHashMap<String, Object>();
                event.put("kind", "projected");
                event.put("id", schedule.getId() + ":" + projected.eventIndex() + ":" + projected.start());
                event.put("scheduleId", schedule.getId());
                event.put("scheduleName", schedule.getName());
                event.put("eventIndex", projected.eventIndex());
                event.put("eventName", projected.eventName());
                event.put("name", projected.leagueName());
                event.put("leagueType", schedule.getType() == null ? null : schedule.getType().toString());
                event.put("start", projected.start().toString());
                event.put("end", projected.end().toString());
                event.put("createdOn", projected.createdOn().toString());
                event.put("error", projected.error());
                events.add(event);
            }
        }
    }
}
