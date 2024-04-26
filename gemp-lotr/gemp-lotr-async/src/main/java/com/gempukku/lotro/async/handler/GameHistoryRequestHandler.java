package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.DateUtils;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getGameHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        //HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        int start = Integer.parseInt(getQueryParameterSafely(queryDecoder, "start"));
        int count = Integer.parseInt(getQueryParameterSafely(queryDecoder, "count"));

        if (start < 0 || count < 1 || count > 100)
            throw new HttpProcessingException(400);

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        final List<DBDefs.GameHistory> playerGameHistory = _gameHistoryService.getGameHistoryForPlayer(resourceOwner, start, count);
        int recordCount = _gameHistoryService.getGameHistoryForPlayerCount(resourceOwner);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element gameHistory = doc.createElement("gameHistory");
        gameHistory.setAttribute("count", String.valueOf(recordCount));
        gameHistory.setAttribute("playerId", resourceOwner.getName());

        for (DBDefs.GameHistory game : playerGameHistory) {
            Element historyEntry = doc.createElement("historyEntry");
            historyEntry.setAttribute("winner", game.winner);
            historyEntry.setAttribute("loser", game.loser);

            historyEntry.setAttribute("winReason", game.win_reason);
            historyEntry.setAttribute("loseReason", game.lose_reason);

            historyEntry.setAttribute("formatName", game.format_name);
            String tournament = game.tournament;
            if (tournament != null)
                historyEntry.setAttribute("tournament", tournament);

            if (game.winner.equals(resourceOwner.getName()) && game.win_recording_id != null) {
                historyEntry.setAttribute("gameRecordingId", game.win_recording_id);
                historyEntry.setAttribute("deckName", game.winner_deck_name);
            } else if (game.loser.equals(resourceOwner.getName()) && game.lose_recording_id != null) {
                historyEntry.setAttribute("gameRecordingId", game.lose_recording_id);
                historyEntry.setAttribute("deckName", game.loser_deck_name);
            }

            historyEntry.setAttribute("startTime", game.start_date.format(DateUtils.DateHourMinuteFormatter));
            historyEntry.setAttribute("endTime", game.end_date.format(DateUtils.DateHourMinuteFormatter));

            gameHistory.appendChild(historyEntry);
        }

        doc.appendChild(gameHistory);

        responseWriter.writeXmlResponse(doc);
    }
}
