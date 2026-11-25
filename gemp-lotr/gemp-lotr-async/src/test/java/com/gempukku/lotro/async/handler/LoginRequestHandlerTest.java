package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoggedUserHolder;
import com.gempukku.lotro.service.LoginThrottle;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * POST /login: what each outcome answers, and the limit on failed attempts (LoginThrottle, whose own counting is
 * LoginThrottleTest's).  The PlayerDAO is a stand-in; which stored passwords match is DbPlayerDAOPasswordTest's.
 */
public class LoginRequestHandlerTest {
    private static final String IP = "10.0.0.7";

    private Map<Type, Object> context;
    private PlayerDAO playerDao;
    private LoggedUserHolder loggedUserHolder;
    private LoginThrottle throttle;

    @Before
    public void setUp() {
        playerDao = mock(PlayerDAO.class);
        loggedUserHolder = mock(LoggedUserHolder.class);
        when(loggedUserHolder.logUser(anyString())).thenReturn("session-id");
        context = new HashMap<>();
        context.put(PlayerDAO.class, playerDao);
        context.put(LoggedUserHolder.class, loggedUserHolder);
        throttle = new LoginThrottle();
        context.put(LoginThrottle.class, throttle);
    }

    static HttpRequest loginPost(String login, String password) {
        Map<String, String> form = new LinkedHashMap<>();
        if (login != null)
            form.put("login", login);
        if (password != null)
            form.put("password", password);
        StringBuilder body = new StringBuilder();
        for (var entry : form.entrySet()) {
            if (!body.isEmpty())
                body.append('&');
            body.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/gemp-lotr-server/login",
                Unpooled.copiedBuffer(body.toString(), StandardCharsets.UTF_8));
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        return request;
    }

    private LoginRequestHandler handler() {
        // what would be answered later, off the IO thread, is answered at once here
        return new LoginRequestHandler(context, Runnable::run);
    }

    /** The status the attempt answered: 200 when it wrote a response, else the thrown status. */
    private int attempt(String login, String password) throws Exception {
        ResponseWriter writer = mock(ResponseWriter.class);
        try {
            handler().handleRequest("", loginPost(login, password), context, writer, IP);
        } catch (HttpProcessingException e) {
            verify(writer, never()).writeXmlResponse(any(), anyMap());
            return e.getStatus();
        }
        verify(writer).writeXmlResponse(isNull(), anyMap());
        return 200;
    }

    private static Player player(String name, String password, String type, Date bannedUntil) {
        return new Player(1, name, password, type, null, bannedUntil, null, null, false);
    }

    @Test
    public void theRightPasswordLogsIn() throws Exception {
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        assertEquals(200, attempt("Frodo", "ring"));
        verify(loggedUserHolder).logUser("Frodo");
        verify(playerDao).updateLastLoginIp("Frodo", IP);
    }

    @Test
    public void spacesTypedRoundANameAreNotPartOfIt() throws Exception {
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        assertEquals(200, attempt("  Frodo ", "ring"));
        // the session is the account's, by its own name: a ban of "Frodo" ends it
        verify(loggedUserHolder).logUser("Frodo");

        // and "Frodo " is not a second name for the limit to count separately
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME; i++)
            assertEquals(401, attempt("Frodo" + " ".repeat(i), "wrong"));
        assertEquals(429, attempt("Frodo", "ring"));
    }

    @Test
    public void noMatchIs401AndNever202() throws Exception {
        // what loginUser answers for a wrong password, and for any password at all on a blank-password account
        assertEquals(401, attempt("Sam", "anything"));
        assertEquals(401, attempt("Sam", ""));
        verify(loggedUserHolder, never()).logUser(anyString());
    }

    @Test
    public void aTemporarilyBannedPlayerIs409() throws Exception {
        when(playerDao.loginUser("Merry", "x")).thenReturn(player("Merry", "hash", "un",
                new Date(System.currentTimeMillis() + 60_000)));
        assertEquals(409, attempt("Merry", "x"));
    }

    @Test
    public void anAccountThatIsNotAUserIs403() throws Exception {
        when(playerDao.loginUser("Gollum", "x")).thenReturn(player("Gollum", "hash", "", null));
        assertEquals(403, attempt("Gollum", "x"));
    }

    // ---- the limit on failed attempts ----

    @Test
    public void tooManyFailuresForANameAre429WithoutCheckingThePassword() throws Exception {
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME; i++)
            assertEquals(401, attempt("Frodo", "guess" + i));
        assertEquals("even the right password", 429, attempt("Frodo", "ring"));
        assertEquals("the name ignoring case", 429, attempt("FRODO", "ring"));
        verify(playerDao, never()).loginUser("Frodo", "ring");
        assertEquals("another name from the same address is still checked", 401, attempt("Sam", "x"));
    }

    @Test
    public void aRefusedAttemptIsNotCountedAsAFailure() throws Exception {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME; i++)
            attempt("Frodo", "guess" + i);
        assertEquals(429, attempt("Frodo", "again"));
        long later = System.currentTimeMillis() + LoginThrottle.WINDOW_MILLIS;
        assertFalse("only the ten counted failures, all gone after the window", throttle.isBlocked("Frodo", IP, later));
    }

    @Test
    public void tooManyFailuresFromAnAddressAre429ForEveryName() throws Exception {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ADDRESS; i++)
            assertEquals(401, attempt("name" + i, "x"));
        assertEquals(429, attempt("Frodo", "ring"));
        verify(playerDao, never()).loginUser("Frodo", "ring");
    }

    @Test
    public void theRightPasswordClearsTheNamesFailures() throws Exception {
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME - 1; i++)
            attempt("Frodo", "guess" + i);
        assertEquals(200, attempt("Frodo", "ring"));
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME - 1; i++)
            assertEquals(401, attempt("Frodo", "again" + i));
        assertEquals("counting started again from the success", 200, attempt("Frodo", "ring"));
    }

    @Test
    public void aRightPasswordOnABannedAccountStillCountsAsRight() throws Exception {
        when(playerDao.loginUser("Merry", "x")).thenReturn(player("Merry", "hash", "un",
                new Date(System.currentTimeMillis() + 60_000)));
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_NAME - 1; i++)
            attempt("Merry", "guess" + i);
        assertEquals(409, attempt("Merry", "x"));
        assertEquals(401, attempt("Merry", "guess"));
        assertEquals("not blocked: the 409 cleared the count", 409, attempt("Merry", "x"));
    }

    // ---- an account a PC account has claimed

    private com.gempukku.lotro.argonauth.PcAccountService pcAccounts() {
        var pcAccounts = mock(com.gempukku.lotro.argonauth.PcAccountService.class);
        context.put(com.gempukku.lotro.argonauth.PcAccountService.class, pcAccounts);
        return pcAccounts;
    }

    @Test
    public void anAccountClaimedByAPcAccountIsNotLoggedInToByPasswordRightOrWrong() throws Exception {
        var pcAccounts = pcAccounts();
        when(pcAccounts.isClaimedByCopy("Frodo")).thenReturn(true);
        when(pcAccounts.claimedBy("Frodo")).thenReturn("760f5b53-1a93-4a03-91a7-d812c8f0fc23");
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        ResponseWriter writer = mock(ResponseWriter.class);

        handler().handleRequest("", loginPost(" Frodo ", "ring"), context, writer, IP);
        handler().handleRequest("", loginPost("Frodo", "wrong"), context, writer, IP);

        verify(writer, times(2)).writeError(LoginRequestHandler.LINKED_TO_PC_ACCOUNT);
        // the password is never looked at, so no guess is counted and nobody is logged in
        verify(playerDao, never()).loginUser(anyString(), anyString());
        verify(loggedUserHolder, never()).logUser(anyString());
        assertFalse(throttle.isBlocked("Frodo", IP, System.currentTimeMillis()));
    }

    @Test
    public void anAccountUnlinkedSinceGempLastHeardLogsInByPasswordAgain() throws Exception {
        var pcAccounts = pcAccounts();
        when(pcAccounts.isClaimedByCopy("Frodo")).thenReturn(true);
        when(pcAccounts.claimedBy("Frodo")).thenReturn(null);
        when(playerDao.loginUser("Frodo", "ring")).thenReturn(player("Frodo", "hash", "u", null));
        ResponseWriter writer = mock(ResponseWriter.class);

        handler().handleRequest("", loginPost("Frodo", "ring"), context, writer, IP);
        verify(writer).writeXmlResponse(isNull(), anyMap());
        verify(loggedUserHolder).logUser("Frodo");

        // and a wrong password is still a wrong password, said from the later thread too
        handler().handleRequest("", loginPost("Frodo", "wrong"), context, writer, IP);
        verify(writer).writeError(401);
    }

    @Test
    public void askingArgonauthAboutAClaimedAccountIsLeftForLater() throws Exception {
        var pcAccounts = pcAccounts();
        when(pcAccounts.isClaimedByCopy("Frodo")).thenReturn(true);
        when(pcAccounts.claimedBy("Frodo")).thenReturn("sub");
        java.util.List<Runnable> deferred = new java.util.ArrayList<>();
        ResponseWriter writer = mock(ResponseWriter.class);

        new LoginRequestHandler(context, deferred::add).handleRequest("", loginPost("Frodo", "ring"), context, writer, IP);

        assertEquals(1, deferred.size());
        verify(pcAccounts, never()).claimedBy(anyString());
        verifyNoInteractions(writer);
        deferred.get(0).run();
        verify(writer).writeError(LoginRequestHandler.LINKED_TO_PC_ACCOUNT);
    }

    @Test
    public void anAccountNobodyHasClaimedIsAnsweredAtOnceAsEver() throws Exception {
        var pcAccounts = pcAccounts();
        when(playerDao.loginUser("OldHand", "s3cret")).thenReturn(player("OldHand", "hash", "u", null));
        java.util.List<Runnable> deferred = new java.util.ArrayList<>();
        ResponseWriter writer = mock(ResponseWriter.class);

        new LoginRequestHandler(context, deferred::add).handleRequest("", loginPost("OldHand", "s3cret"), context, writer, IP);

        assertTrue(deferred.isEmpty());
        verify(writer).writeXmlResponse(isNull(), anyMap());
        verify(pcAccounts, never()).claimedBy(anyString());
    }

    @Test
    public void somethingUnexpectedWhileAskingIsA500() throws Exception {
        var pcAccounts = pcAccounts();
        when(pcAccounts.isClaimedByCopy("Frodo")).thenReturn(true);
        when(pcAccounts.claimedBy("Frodo")).thenThrow(new java.sql.SQLException("the database is away"));
        ResponseWriter writer = mock(ResponseWriter.class);

        handler().handleRequest("", loginPost("Frodo", "ring"), context, writer, IP);

        verify(writer).writeError(500);
    }
}
