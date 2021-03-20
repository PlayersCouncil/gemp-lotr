package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.hall.HallServer;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.reflect.Type;
import java.util.Map;

public class StatusRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private HallServer _hallServer;
    private GameHistoryService _gameHistoryService;
    private ChatServer _chatServer;

    public StatusRequestHandler(Map<Type, Object> context) {
        super(context);
        _hallServer = extractObject(context, HallServer.class);
        _gameHistoryService = extractObject(context, GameHistoryService.class);
        _chatServer = extractObject(context, ChatServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.getMethod() == HttpMethod.GET) {
            StringBuilder sb = new StringBuilder();

            int day = 1000 * 60 * 60 * 24;
            int week = 1000 * 60 * 60 * 24 * 7;
            sb.append("Tables count: ").append(_hallServer.getTablesCount()).append(", players in hall: ").append(_chatServer.getChatRoom("Game Hall").getUsersInRoom(false).size())
                    .append(", games played in last 24 hours: ").append(_gameHistoryService.getGamesPlayedCount(System.currentTimeMillis() - day, day))
                    .append(", active players in last week: ").append(_gameHistoryService.getActivePlayersCount(System.currentTimeMillis() - week, week));

            responseWriter.writeHtmlResponse(sb.toString());
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
