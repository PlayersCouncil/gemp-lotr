package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.events.EventHistoryService;
import com.gempukku.lotro.game.Player;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.reflect.Type;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The browser of completed leagues and tournaments, one kind and one month at a time (event-browser contract v2).
 * <p>
 * GET /eventHistory/months?kind=league|tournament - the months holding at least one completed event of that kind,
 * newest first.<br>
 * GET /eventHistory?month=yyyy-MM&amp;kind=league|tournament - that month's completed events of that kind.
 * <p>
 * {@code kind} is required on both; a missing or unknown kind, or a missing or malformed month, is a 400 with a
 * {@code {"error": ...}} body.  Both are called on demand: the client shows nothing until a month is picked, so
 * neither runs on tab load.  Whether the viewer is an event admin is decided here, from their player record, and
 * reported as {@code isAdmin}; the client must gate the admin links on that and never on its own copy of the
 * player's roles.
 * <p>
 * Assembling the rows lives in {@link EventHistoryService} rather than here, because this module has no test tree.
 */
public class EventHistoryRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final EventHistoryService _eventHistoryService;

    public EventHistoryRequestHandler(Map<Type, Object> context) {
        super(context);
        _eventHistoryService = extractObject(context, EventHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if ((uri.isEmpty() || uri.equals("/")) && request.method() == HttpMethod.GET) {
            getEventHistory(request, responseWriter);
        } else if (uri.equals("/months") && request.method() == HttpMethod.GET) {
            getEventHistoryMonths(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getEventHistoryMonths(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        getResourceOwnerSafely(request, participantId);

        String kind = EventHistoryService.parseKind(getQueryParameterSafely(queryDecoder, "kind"));
        if (kind == null) {
            writeBadRequest(responseWriter, KIND_ERROR);
            return;
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("kind", kind);
        result.put("months", _eventHistoryService.getAvailableMonths(kind));
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private void getEventHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        Player viewer = getResourceOwnerSafely(request, participantId);
        boolean isAdmin = viewer.hasType(Player.Type.ADMIN) || viewer.hasType(Player.Type.LEAGUE_ADMIN);

        YearMonth month = EventHistoryService.parseMonth(getQueryParameterSafely(queryDecoder, "month"));
        if (month == null) {
            writeBadRequest(responseWriter, "Parameter 'month' must be a month in yyyy-MM form.");
            return;
        }
        String kind = EventHistoryService.parseKind(getQueryParameterSafely(queryDecoder, "kind"));
        if (kind == null) {
            writeBadRequest(responseWriter, KIND_ERROR);
            return;
        }

        var result = new LinkedHashMap<String, Object>();
        result.put("month", month.toString());
        result.put("kind", kind);
        result.put("isAdmin", isAdmin);
        result.put("events", _eventHistoryService.getMonthEvents(month, kind, isAdmin));
        responseWriter.writeJsonResponse(JsonUtils.SerializeWithNulls(result));
    }

    private static final String KIND_ERROR = "Parameter 'kind' must be '" + EventHistoryService.KIND_LEAGUE
            + "' or '" + EventHistoryService.KIND_TOURNAMENT + "'.";

    /** The contract gives this endpoint's errors a readable JSON body, which writeError cannot send. */
    private static void writeBadRequest(ResponseWriter responseWriter, String message) {
        var error = new LinkedHashMap<String, Object>();
        error.put("error", message);
        responseWriter.writeJsonResponse(400, JsonUtils.SerializeWithNulls(error));
    }
}
