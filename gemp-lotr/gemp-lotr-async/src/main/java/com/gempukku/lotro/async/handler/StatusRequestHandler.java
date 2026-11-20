package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

public class StatusRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final HallServer _hallServer;
    private final GameHistoryService _gameHistoryService;
    private final ChatServer _chatServer;

    private static final Logger _log = LogManager.getLogger(StatusRequestHandler.class);

    public StatusRequestHandler(Map<Type, Object> context) {
        super(context);
        _hallServer = extractObject(context, HallServer.class);
        _gameHistoryService = extractObject(context, GameHistoryService.class);
        _chatServer = extractObject(context, ChatServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            // GET /gemp-lotr-server/ : the sentence the legacy login page shows
            var status = getStatus();
            String sb = "Tables count: " + status.tables() + ", players in hall: " + status.playersInHall() +
                    ", games played in last 24 hours: " + status.gamesLast24Hours() +
                    ", active players in last week: " + status.activePlayersLastWeek();

            responseWriter.writeHtmlResponse(sb);
        } else if (uri.equals("status") && request.method() == HttpMethod.GET) {
            // GET /gemp-lotr-server/status : the same four numbers as JSON, for the new login page to word itself
            responseWriter.writeJsonResponse(JsonUtils.Serialize(getStatus()));
        } else {
            throw new HttpProcessingException(404);
        }
    }

    /** The server's activity counts; serialized as {tables, playersInHall, gamesLast24Hours, activePlayersLastWeek}. */
    public record ServerStatus(int tables, int playersInHall, int gamesLast24Hours, int activePlayersLastWeek) {}

    private ServerStatus getStatus() {
        var today = ZonedDateTime.now(ZoneOffset.UTC);
        var yesterday = today.minusDays(1);
        var lastWeek = today.minusDays(7);

        return new ServerStatus(_hallServer.getTablesCount(),
                _chatServer.getChatRoom("Game Hall").getUsersInRoom(false).size(),
                _gameHistoryService.getGamesPlayedCount(yesterday, today),
                _gameHistoryService.getActivePlayersCount(lastWeek, today));
    }
}
