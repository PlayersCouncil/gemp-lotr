package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoggedUserHolder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Who is acting: the player the session cookie's session plays as, and nobody else (PC-ACCOUNT.md, "The player comes
 * from the session cookie and nothing else").  A participantId parameter used to let an admin act as any player,
 * and with -Dtest=true let anyone act as anyone without logging in.  Driven through GET /playerStats, whose answer
 * is the acting player's own statistics.
 */
public class ResourceOwnerTest {
    private static final String STATS = "/gemp-lotr-server/playerStats";

    private String previousTestProperty;
    private Map<Type, Object> context;
    private LoggedUserHolder sessions;
    private PlayerDAO playerDao;
    private GameHistoryService history;
    private Player frodo;
    private Player admin;

    @Before
    public void setUp() {
        previousTestProperty = System.getProperty("test");
        playerDao = mock(PlayerDAO.class);
        frodo = new Player(1, "Frodo", "hash", "u", null, null, null, null, false);
        admin = new Player(2, "Gandalf", "hash", "ua", null, null, null, null, false);
        when(playerDao.getPlayer("Frodo")).thenReturn(frodo);
        when(playerDao.getPlayer("Gandalf")).thenReturn(admin);
        when(playerDao.getPlayer("Sam")).thenReturn(new Player(3, "Sam", "hash", "u", null, null, null, null, false));
        history = mock(GameHistoryService.class);
        when(history.getCasualPlayerStatistics(any())).thenReturn(List.of());
        when(history.getCompetitivePlayerStatistics(any())).thenReturn(List.of());
        sessions = new LoggedUserHolder();
        context = new HashMap<>();
        context.put(PlayerDAO.class, playerDao);
        context.put(LoggedUserHolder.class, sessions);
        context.put(GameHistoryService.class, history);
    }

    @After
    public void tearDown() {
        if (previousTestProperty != null)
            System.setProperty("test", previousTestProperty);
        else
            System.clearProperty("test");
    }

    private static HttpRequest get(String uri, String sessionId) {
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        if (sessionId != null)
            request.headers().set(HttpHeaderNames.COOKIE, "loggedUser=" + sessionId);
        return request;
    }

    private void send(HttpRequest request) throws Exception {
        new PlayerStatsRequestHandler(context).handleRequest("", request, context, mock(ResponseWriter.class), "127.0.0.1");
    }

    private void assert401(HttpRequest request) throws Exception {
        try {
            send(request);
            fail("expected 401");
        } catch (HttpProcessingException e) {
            assertEquals(401, e.getStatus());
        }
        verify(history, never()).getCasualPlayerStatistics(any());
    }

    @Test
    public void theSessionsPlayerActs() throws Exception {
        send(get(STATS, sessions.logUser("Frodo")));
        verify(history).getCasualPlayerStatistics(frodo);
    }

    @Test
    public void aParticipantIdNamesNobody() throws Exception {
        send(get(STATS + "?participantId=Sam", sessions.logUser("Frodo")));
        verify(history).getCasualPlayerStatistics(frodo);
        verify(playerDao, never()).getPlayer("Sam");
    }

    @Test
    public void anAdminCannotActAsAnotherPlayer() throws Exception {
        send(get(STATS + "?participantId=Sam", sessions.logUser("Gandalf")));
        verify(history).getCasualPlayerStatistics(admin);
        verify(playerDao, never()).getPlayer("Sam");
    }

    @Test
    public void noLoginIs401() throws Exception {
        assert401(get(STATS, null));
        assert401(get(STATS, "no-such-session"));
    }

    @Test
    public void noLoginIs401InTestModeToo() throws Exception {
        System.setProperty("test", "true");
        assert401(get(STATS + "?participantId=Frodo", null));
        verify(playerDao, never()).getPlayer("Frodo");
    }

    @Test
    public void aSessionPlayingAsNobodyIs401() throws Exception {
        // a PC account signed in, no alias entered yet
        assert401(get(STATS + "?participantId=Frodo", sessions.newSession().getId()));
    }
}
