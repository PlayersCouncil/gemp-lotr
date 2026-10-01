package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryFilter;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import com.gempukku.util.JsonUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameHistoryRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final GameHistoryService _gameHistoryService;

    private static final Logger _log = LogManager.getLogger(GameHistoryRequestHandler.class);

    public GameHistoryRequestHandler(Map<Type, Object> context) {
        super(context);

        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getGameHistory(request, responseWriter);
        } else if (uri.equals("/filters") && request.method() == HttpMethod.GET) {
            getFilterOptions(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    /**
     * GET /gameHistory?start=&count=[&format=][&opponent=][&opponentExact=true][&event=][&from=yyyy-MM-dd][&to=yyyy-MM-dd]
     * <p>
     * The logged-in player's games, newest first.  count is clamped to 1..100 (default 20).  The filters are optional:
     * format is an exact format name, opponent and event match the start of the name ignoring case (event=casual
     * means casual games), from/to are inclusive days in server time (UTC) on which the game ended.
     * <p>
     * The root carries count (games matching the filters), total (all the player's games), start and pageSize.  Each
     * historyEntry has the original attributes plus result (W/L), opponent, deckName (the player's own deck, whether
     * or not a replay was kept), startMs/endMs (epoch milliseconds) next to the startTime/endTime strings.
     */
    private void getGameHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        int start = parseInt(getQueryParameterSafely(queryDecoder, "start"), 0);
        int count = parseInt(getQueryParameterSafely(queryDecoder, "count"), GameHistoryService.DEFAULT_HISTORY_PAGE_SIZE);
        if (start < 0 || count < 1)
            throw new HttpProcessingException(400);

        GameHistoryFilter filter = new GameHistoryFilter(
                getQueryParameterSafely(queryDecoder, "format"),
                getQueryParameterSafely(queryDecoder, "opponent"),
                "true".equalsIgnoreCase(getQueryParameterSafely(queryDecoder, "opponentExact")),
                getQueryParameterSafely(queryDecoder, "event"),
                parseDay(getQueryParameterSafely(queryDecoder, "from")),
                parseDay(getQueryParameterSafely(queryDecoder, "to")));
        String problem = filter.validate();
        if (problem != null)
            throw new HttpProcessingException(400, problem);

        Player resourceOwner = getResourceOwnerSafely(request, participantId);
        String me = resourceOwner.getName();

        GameHistoryService.HistoryPage page = _gameHistoryService.getGameHistoryPage(resourceOwner, filter, start, count);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element gameHistory = doc.createElement("gameHistory");
        gameHistory.setAttribute("count", String.valueOf(page.matching()));
        gameHistory.setAttribute("total", String.valueOf(page.total()));
        gameHistory.setAttribute("start", String.valueOf(page.start()));
        gameHistory.setAttribute("pageSize", String.valueOf(page.count()));
        gameHistory.setAttribute("playerId", me);

        for (DBDefs.GameHistory game : page.entries()) {
            Element historyEntry = doc.createElement("historyEntry");
            historyEntry.setAttribute("winner", game.winner);
            historyEntry.setAttribute("loser", game.loser);

            historyEntry.setAttribute("winReason", game.win_reason);
            historyEntry.setAttribute("loseReason", game.lose_reason);

            if (game.format_name != null)
                historyEntry.setAttribute("formatName", game.format_name);
            String tournament = game.tournament;
            if (tournament != null)
                historyEntry.setAttribute("tournament", tournament);

            boolean won = game.winner.equals(me);
            historyEntry.setAttribute("result", won ? "W" : "L");
            historyEntry.setAttribute("opponent", won ? game.loser : game.winner);
            String deckName = won ? game.winner_deck_name : game.loser_deck_name;
            if (deckName != null)
                historyEntry.setAttribute("deckName", deckName);
            // the replay link stays as before: only the player's own recording of their own game
            String recordingId = won ? game.win_recording_id : game.lose_recording_id;
            if (recordingId != null)
                historyEntry.setAttribute("gameRecordingId", recordingId);

            if (game.start_date != null) {
                historyEntry.setAttribute("startTime", game.start_date.format(DateUtils.DateTimeFormat));
                historyEntry.setAttribute("startMs", String.valueOf(game.GetUTCStartDate().toInstant().toEpochMilli()));
            }
            if (game.end_date != null) {
                historyEntry.setAttribute("endTime", game.end_date.format(DateUtils.DateTimeFormat));
                historyEntry.setAttribute("endMs", String.valueOf(game.GetUTCEndDate().toInstant().toEpochMilli()));
            }

            gameHistory.appendChild(historyEntry);
        }

        doc.appendChild(gameHistory);

        responseWriter.writeXmlResponse(doc);
    }

    /** GET /gameHistory/filters -> {"formats": [...most played first], "events": [...most recent first, max 100]} */
    private void getFilterOptions(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Player resourceOwner = getResourceOwnerSafely(request, getQueryParameterSafely(queryDecoder, "participantId"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formats", _gameHistoryService.getGameHistoryFormats(resourceOwner));
        result.put("events", _gameHistoryService.getGameHistoryEvents(resourceOwner));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    private static int parseInt(String value, int fallback) throws HttpProcessingException {
        if (value == null || value.isBlank())
            return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exp) {
            throw new HttpProcessingException(400);
        }
    }

    private static LocalDate parseDay(String value) throws HttpProcessingException {
        if (value == null || value.isBlank())
            return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exp) {
            throw new HttpProcessingException(400, "Dates must be given as yyyy-MM-dd.");
        }
    }
}
