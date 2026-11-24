package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.argonauth.ArgonauthConfig;
import com.gempukku.lotro.argonauth.ArgonauthException;
import com.gempukku.lotro.argonauth.ArgonauthMode;
import com.gempukku.lotro.argonauth.PcAccountService;
import com.gempukku.lotro.argonauth.PcAccountService.Landing;
import com.gempukku.lotro.argonauth.PcAccountService.Refusal;
import com.gempukku.lotro.argonauth.PcAccountService.View;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.service.LoggedUserHolder;
import com.gempukku.lotro.service.LoggedUserHolder.Session;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * `/gemp-lotr-server/argonauth/...` as HTTP: which address reaches which of {@link PcAccountService}'s calls, with
 * what, and how each answer is written. What the calls do is PcAccountServiceTest's.
 */
public class ArgonauthRequestHandlerTest {
    private static final String IP = "203.0.113.7";
    private static final View VIEW = new View("optional", "Frodo", "Frodo Baggins", "https://auth.example/", true, true,
            List.of(new PcAccountService.Alias(101, "Frodo", true, false)), null);

    private final Map<Type, Object> _context = new HashMap<>();
    private final LoggedUserHolder _sessions = new LoggedUserHolder();
    private final PcAccountService _service = mock(PcAccountService.class);
    private final List<Runnable> _deferred = new ArrayList<>();
    private ResponseWriter _writer;

    @Before
    public void setUp() throws Exception {
        _context.put(PlayerDAO.class, mock(PlayerDAO.class));
        _context.put(LoggedUserHolder.class, _sessions);
        _context.put(PcAccountService.class, _service);
        mode(ArgonauthMode.OPTIONAL);
        when(_service.view(any())).thenReturn(VIEW);
        _writer = mock(ResponseWriter.class);
    }

    private void mode(ArgonauthMode mode) {
        when(_service.getConfig()).thenReturn(mode == ArgonauthMode.TEST ? ArgonauthConfig.TEST
                : new ArgonauthConfig(mode, "https://auth.example", "gemp", "secret", "https://play.example/cb", "https://play.example/out", "https://auth.example"));
    }

    private static HttpRequest request(HttpMethod method, String uri, String body, Session session) {
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/gemp-lotr-server/argonauth" + uri,
                Unpooled.copiedBuffer(body == null ? "" : body, StandardCharsets.UTF_8));
        if (method == HttpMethod.POST) {
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        }
        if (session != null)
            request.headers().set(HttpHeaderNames.COOKIE, "other=1; loggedUser=" + session.getId());
        return request;
    }

    /** The request answered at once, as if the deferred part ran straight away. */
    private void handle(HttpMethod method, String uri, String body, Session session) throws Exception {
        defer(method, uri, body, session);
        new ArrayList<>(_deferred).forEach(Runnable::run);
        _deferred.clear();
    }

    /** The request handled as far as the IO thread takes it. */
    private void defer(HttpMethod method, String uri, String body, Session session) throws Exception {
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
        new ArgonauthRequestHandler(_context, _deferred::add).handleRequest(path, request(method, uri, body, session), _context, _writer, IP);
    }

    private int statusOf(HttpMethod method, String uri, String body) throws Exception {
        try {
            handle(method, uri, body, null);
            return 200;
        } catch (HttpProcessingException exp) {
            return exp.getStatus();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> redirect() {
        ArgumentCaptor<Map<String, String>> headers = ArgumentCaptor.forClass(Map.class);
        verify(_writer).writeError(eq(302), headers.capture());
        assertEquals("no-store", headers.getValue().get("cache-control"));
        return headers.getValue();
    }

    // ---- who this browser is

    @Test
    public void theSessionIsAnsweredForWhoeverTheCookieSays() throws Exception {
        Session session = _sessions.newSession();
        handle(HttpMethod.GET, "/session", null, session);
        verify(_service).view(same(session));
        verify(_writer).writeJsonResponse(VIEW.toJson());

        handle(HttpMethod.GET, "/session", null, null);
        verify(_service).view(isNull());
    }

    @Test
    public void aCookieThatNamesNoSessionIsNobody() throws Exception {
        Session ended = _sessions.newSession();
        _sessions.end(ended.getId());
        handle(HttpMethod.GET, "/session", null, ended);
        verify(_service).view(isNull());
    }

    // ---- test mode

    @Test
    public void inTestModeOnlyTheSessionAndLoggingOutAreThere() throws Exception {
        mode(ArgonauthMode.TEST);
        when(_service.logOut(any(), any())).thenReturn("/gemp-lotr/ng/login");

        assertEquals(200, statusOf(HttpMethod.GET, "/session", null));
        assertEquals(200, statusOf(HttpMethod.POST, "/logout", "scope=alias"));
        assertEquals(404, statusOf(HttpMethod.GET, "/login", null));
        assertEquals(404, statusOf(HttpMethod.GET, "/callback?code=c&state=s", null));
        assertEquals(404, statusOf(HttpMethod.POST, "/enter", "playerId=1"));
        assertEquals(404, statusOf(HttpMethod.POST, "/create", "name=Frodo"));
        assertEquals(404, statusOf(HttpMethod.POST, "/link", "login=Frodo&password=x"));
        assertEquals(404, statusOf(HttpMethod.POST, "/connect", "password=x"));
        verify(_service, never()).startConnect(any(), any(), any());
        verify(_service, never()).startSignIn(any(), any(), any());
        verify(_service, never()).completeSignIn(any(), any(), any(), any(), any());
        verify(_service, never()).enter(any(), anyInt(), any());
        verify(_service, never()).create(any(), any(), any());
        verify(_service, never()).link(any(), any(), any(), any());
    }

    @Test
    public void anAddressThatIsNotOneOfTheseOrTheWrongWayToAskIs404() throws Exception {
        assertEquals(404, statusOf(HttpMethod.GET, "/nothing", null));
        assertEquals(404, statusOf(HttpMethod.GET, "", null));
        assertEquals(404, statusOf(HttpMethod.POST, "/session", ""));
        assertEquals(404, statusOf(HttpMethod.POST, "/login", ""));
        assertEquals(404, statusOf(HttpMethod.POST, "/callback", "code=c&state=s"));
        // nothing that changes anything answers a GET, which another site's link could make
        assertEquals(404, statusOf(HttpMethod.GET, "/logout?scope=all", null));
        assertEquals(404, statusOf(HttpMethod.GET, "/enter?playerId=1", null));
        assertEquals(404, statusOf(HttpMethod.GET, "/create?name=Frodo", null));
        assertEquals(404, statusOf(HttpMethod.GET, "/link?login=Frodo&password=x", null));
        assertEquals(404, statusOf(HttpMethod.GET, "/connect?password=x", null));
        verifyNoInteractions(_writer);
    }

    // ---- to Argonauth and back

    @Test
    public void startingASignInSendsTheBrowserToArgonauthWithASessionToComeBackTo() throws Exception {
        when(_service.startSignIn(any(), any(), any())).thenReturn("https://auth.example/identity/o/authorize?state=s");

        handle(HttpMethod.GET, "/login?then=create&return=%2Fdeckbuilder", null, null);

        ArgumentCaptor<Session> asking = ArgumentCaptor.forClass(Session.class);
        verify(_service).startSignIn(asking.capture(), eq("create"), eq("/deckbuilder"));
        assertSame("a session was made for the browser that had none", asking.getValue(), _sessions.getSession(asking.getValue().getId()));
        Map<String, String> headers = redirect();
        assertEquals("https://auth.example/identity/o/authorize?state=s", headers.get("location"));
        String cookie = headers.get("set-cookie");
        assertTrue(cookie, cookie.startsWith("loggedUser=" + asking.getValue().getId() + ";"));
        assertTrue(cookie, cookie.contains("Path=/gemp-lotr-server"));
        assertTrue(cookie, cookie.contains("HTTPOnly"));
        assertTrue(cookie, cookie.contains("SameSite=Lax"));
        assertFalse("plain http stays possible for local development", cookie.contains("Secure"));
    }

    @Test
    public void aBrowserThatHasASessionStartsTheSignInFromIt() throws Exception {
        when(_service.startSignIn(any(), any(), any())).thenReturn("https://auth.example/a");
        Session session = _sessions.getSession(_sessions.logUser("OldHand"));

        handle(HttpMethod.GET, "/login?then=connect", null, session);

        verify(_service).startSignIn(same(session), eq("connect"), isNull());
        assertTrue(redirect().get("set-cookie").startsWith("loggedUser=" + session.getId() + ";"));
    }

    @Test
    public void overHttpsTheCookieIsKeptToHttps() throws Exception {
        when(_service.startSignIn(any(), any(), any())).thenReturn("https://auth.example/a");
        HttpRequest request = request(HttpMethod.GET, "/login", null, null);
        request.headers().set("X-Forwarded-Proto", "https");
        new ArgonauthRequestHandler(_context, Runnable::run).handleRequest("/login", request, _context, _writer, IP);
        assertTrue(redirect().get("set-cookie").contains("Secure"));
    }

    @Test
    public void aSignInThatCannotBeStartedLandsOnTheLoginPageSayingSo() throws Exception {
        when(_service.startSignIn(any(), any(), any()))
                .thenThrow(new ArgonauthException(ArgonauthException.Reason.UNAVAILABLE, "Argonauth did not answer"));
        handle(HttpMethod.GET, "/login", null, null);
        assertEquals("/gemp-lotr/ng/login?pc=failed", redirect().get("location"));
    }

    @Test
    public void theCallbackHandsArgonauthsAnswerOverAndGoesWhereItIsTold() throws Exception {
        Session before = _sessions.newSession();
        Session after = _sessions.newSession();
        when(_service.completeSignIn(any(), any(), any(), any(), any())).thenReturn(new Landing(after, "/gemp-lotr/ng/hall"));

        handle(HttpMethod.GET, "/callback?code=the-code&state=the-state", null, before);

        verify(_service).completeSignIn(same(before), eq("the-code"), eq("the-state"), isNull(), eq(IP));
        Map<String, String> headers = redirect();
        assertEquals("/gemp-lotr/ng/hall", headers.get("location"));
        assertTrue("the new session id is given to the browser", headers.get("set-cookie").startsWith("loggedUser=" + after.getId() + ";"));
    }

    @Test
    public void aCallbackThatChangesNothingLeavesTheCookieAlone() throws Exception {
        when(_service.completeSignIn(any(), any(), any(), any(), any())).thenReturn(new Landing(null, "/gemp-lotr/ng/login?pc=cancelled"));

        handle(HttpMethod.GET, "/callback?error=access_denied&state=the-state", null, null);

        verify(_service).completeSignIn(isNull(), isNull(), eq("the-state"), eq("access_denied"), eq(IP));
        Map<String, String> headers = redirect();
        assertEquals("/gemp-lotr/ng/login?pc=cancelled", headers.get("location"));
        assertFalse(headers.containsKey("set-cookie"));
    }

    @Test
    public void connectingIsStartedByAPostWithThePasswordAndAnswersWhereToGo() throws Exception {
        Session session = _sessions.getSession(_sessions.logUser("OldHand"));
        when(_service.startConnect(any(), any(), any())).thenReturn("https://auth.example/identity/o/authorize?state=s");

        handle(HttpMethod.POST, "/connect", "password=s3cret", session);

        verify(_service).startConnect(same(session), eq("s3cret"), eq(IP));
        verify(_writer).writeJsonResponse("{\"next\":\"https://auth.example/identity/o/authorize?state=s\"}");

        when(_service.startConnect(any(), any(), any())).thenThrow(new Refusal(401, "bad-credentials"));
        handle(HttpMethod.POST, "/connect", "password=guess", session);
        verify(_writer).writeJsonResponse(401, "{\"error\":\"bad-credentials\"}");
    }

    @Test
    public void aSignInIsTurnedAwayWhenThereAreAsManySessionsAsThereMayBe() throws Exception {
        LoggedUserHolder full = mock(LoggedUserHolder.class);
        when(full.isFull()).thenReturn(true);
        _context.put(LoggedUserHolder.class, full);

        handle(HttpMethod.GET, "/login", null, null);

        assertEquals("/gemp-lotr/ng/login?pc=failed", redirect().get("location"));
        verify(full, never()).newSession();
        verify(_service, never()).startSignIn(any(), any(), any());
    }

    // ---- the alias list's actions

    @Test
    public void enteringCreatingAndLinkingAnswerTheSession() throws Exception {
        Session session = _sessions.newSession();
        when(_service.enter(any(), anyInt(), any())).thenReturn(VIEW);
        when(_service.create(any(), any(), any())).thenReturn(VIEW);
        when(_service.link(any(), any(), any(), any())).thenReturn(VIEW);

        handle(HttpMethod.POST, "/enter", "playerId=101", session);
        verify(_service).enter(same(session), eq(101), eq(IP));
        handle(HttpMethod.POST, "/create", "name=Under+hill", session);
        verify(_service).create(same(session), eq("Under hill"), eq(IP));
        handle(HttpMethod.POST, "/link", "login=OldHand&password=p%26ss%3Dword", session);
        verify(_service).link(same(session), eq("OldHand"), eq("p&ss=word"), eq(IP));

        verify(_writer, times(3)).writeJsonResponse(VIEW.toJson());
    }

    @Test
    public void aFieldThatWasNotSentIsNotMadeUp() throws Exception {
        when(_service.create(any(), any(), any())).thenReturn(VIEW);
        when(_service.link(any(), any(), any(), any())).thenReturn(VIEW);
        handle(HttpMethod.POST, "/create", "", null);
        verify(_service).create(isNull(), isNull(), eq(IP));
        handle(HttpMethod.POST, "/link", "login=OldHand", null);
        verify(_service).link(isNull(), eq("OldHand"), isNull(), eq(IP));
    }

    @Test
    public void anEnterWithoutAPlayerIdIsABadRequest() throws Exception {
        assertEquals(400, statusOf(HttpMethod.POST, "/enter", ""));
        assertEquals(400, statusOf(HttpMethod.POST, "/enter", "playerId=Frodo"));
        assertEquals(400, statusOf(HttpMethod.POST, "/enter", "playerId=1%20OR%201"));
        verify(_service, never()).enter(any(), anyInt(), any());
    }

    @Test
    public void aRefusalIsItsStatusAndItsError() throws Exception {
        when(_service.enter(any(), anyInt(), any())).thenThrow(new Refusal(409, "busy"));
        when(_service.create(any(), any(), any())).thenThrow(new Refusal(401, "reauth"));
        when(_service.link(any(), any(), any(), any())).thenThrow(new Refusal(429, "slow-down"));

        handle(HttpMethod.POST, "/enter", "playerId=101", null);
        verify(_writer).writeJsonResponse(409, "{\"error\":\"busy\"}");
        handle(HttpMethod.POST, "/create", "name=Frodo", null);
        verify(_writer).writeJsonResponse(401, "{\"error\":\"reauth\"}");
        handle(HttpMethod.POST, "/link", "login=a&password=b", null);
        verify(_writer).writeJsonResponse(429, "{\"error\":\"slow-down\"}");
    }

    @Test
    public void somethingUnexpectedIsA500AndNotASilence() throws Exception {
        when(_service.enter(any(), anyInt(), any())).thenThrow(new java.sql.SQLException("the database is away"));
        handle(HttpMethod.POST, "/enter", "playerId=101", null);
        verify(_writer).writeError(500);
    }

    @Test
    public void loggingOutAnswersWhereToGoNext() throws Exception {
        Session session = _sessions.newSession();
        when(_service.logOut(any(), any())).thenReturn("https://auth.example/identity/o/logout?client_id=gemp");

        handle(HttpMethod.POST, "/logout", "scope=all", session);
        verify(_service).logOut(same(session), eq("all"));
        verify(_writer).writeJsonResponse("{\"next\":\"https://auth.example/identity/o/logout?client_id=gemp\"}");

        handle(HttpMethod.POST, "/logout", "", null);
        verify(_service).logOut(isNull(), isNull());
    }

    // ---- nothing that waits on Argonauth runs on the IO thread

    @Test
    public void whateverMayWaitOnArgonauthIsLeftForLater() throws Exception {
        Session session = _sessions.newSession();
        defer(HttpMethod.GET, "/login", null, session);
        defer(HttpMethod.GET, "/callback?code=c&state=s", null, session);
        defer(HttpMethod.POST, "/enter", "playerId=101", session);
        defer(HttpMethod.POST, "/create", "name=Frodo", session);
        defer(HttpMethod.POST, "/link", "login=a&password=b", session);
        defer(HttpMethod.POST, "/logout", "scope=all", session);
        defer(HttpMethod.POST, "/connect", "password=x", session);
        defer(HttpMethod.GET, "/session", null, session);

        assertEquals(8, _deferred.size());
        verify(_service, never()).startConnect(any(), any(), any());
        verify(_service, never()).view(any());
        verify(_service, never()).startSignIn(any(), any(), any());
        verify(_service, never()).completeSignIn(any(), any(), any(), any(), any());
        verify(_service, never()).enter(any(), anyInt(), any());
        verify(_service, never()).create(any(), any(), any());
        verify(_service, never()).link(any(), any(), any(), any());
        verify(_service, never()).logOut(any(), any());
        verifyNoInteractions(_writer);
    }

    @Test
    public void theRealHandlerAnswersFromAVirtualThread() throws Exception {
        final boolean[] virtual = new boolean[1];
        when(_service.enter(any(), anyInt(), any())).thenAnswer(call -> {
            virtual[0] = Thread.currentThread().isVirtual();
            return VIEW;
        });
        new ArgonauthRequestHandler(_context).handleRequest("/enter", request(HttpMethod.POST, "/enter", "playerId=101", null), _context, _writer, IP);
        verify(_writer, timeout(5000)).writeJsonResponse(VIEW.toJson());
        assertTrue(virtual[0]);
    }
}
