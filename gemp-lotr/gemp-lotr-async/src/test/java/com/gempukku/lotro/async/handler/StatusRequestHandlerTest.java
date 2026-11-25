package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.hall.HallServer;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * The server status, which needs no login: the sentence the legacy login page shows ({@code GET /gemp-lotr-server/})
 * and the same four numbers as JSON for the new login page ({@code GET /gemp-lotr-server/status}).
 */
public class StatusRequestHandlerTest {
    private Map<Type, Object> context;

    @Before
    public void setUp() {
        HallServer hallServer = mock(HallServer.class);
        when(hallServer.getTablesCount()).thenReturn(3);

        ChatRoomMediator gameHall = mock(ChatRoomMediator.class);
        when(gameHall.getUsersInRoom(anyBoolean())).thenReturn(List.of("Ketura", "Bob"));
        ChatServer chatServer = mock(ChatServer.class);
        when(chatServer.getChatRoom("Game Hall")).thenReturn(gameHall);

        GameHistoryService gameHistoryService = mock(GameHistoryService.class);
        when(gameHistoryService.getGamesPlayedCount(any(), any())).thenReturn(41);
        when(gameHistoryService.getActivePlayersCount(any(), any())).thenReturn(57);

        context = new HashMap<>();
        context.put(HallServer.class, hallServer);
        context.put(ChatServer.class, chatServer);
        context.put(GameHistoryService.class, gameHistoryService);
    }

    private ResponseWriter get(String path) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        // anonymous: no loggedUser cookie
        new StatusRequestHandler(context).handleRequest(path,
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/gemp-lotr-server/" + path),
                context, writer, "127.0.0.1");
        return writer;
    }

    @Test
    public void legacySentenceIsUnchanged() throws Exception {
        ResponseWriter writer = get("");
        verify(writer).writeHtmlResponse("Tables count: 3, players in hall: 2, games played in last 24 hours: 41, "
                + "active players in last week: 57");
    }

    @Test
    public void statusAsJson() throws Exception {
        ResponseWriter writer = get("status");
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(writer).writeJsonResponse(json.capture());

        JSONObject status = JSON.parseObject(json.getValue());
        assertEquals(4, status.size());
        assertEquals(3, status.getIntValue("tables"));
        assertEquals(2, status.getIntValue("playersInHall"));
        assertEquals(41, status.getIntValue("gamesLast24Hours"));
        assertEquals(57, status.getIntValue("activePlayersLastWeek"));
    }

    @Test
    public void anythingElseIsNotFound() throws Exception {
        try {
            get("statuses");
            fail("expected a 404");
        } catch (HttpProcessingException e) {
            assertEquals(404, e.getStatus());
        }
    }
}
