package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.hall.HallServer;
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

            var today = ZonedDateTime.now(ZoneOffset.UTC);
            var yesterday = today.minusDays(1);
            var lastWeek = today.minusDays(7);

            String sb = "Tables count: " + _hallServer.getTablesCount() + ", players in hall: " + _chatServer.getChatRoom("Game Hall").getUsersInRoom(false).size() +
                    ", games played in last 24 hours: " + _gameHistoryService.getGamesPlayedCount(yesterday, today) +
                    ", active players in last week: " + _gameHistoryService.getActivePlayersCount(lastWeek, today);

            responseWriter.writeHtmlResponse(sb);
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
