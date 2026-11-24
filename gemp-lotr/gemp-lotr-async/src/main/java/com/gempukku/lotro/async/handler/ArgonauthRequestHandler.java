package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.argonauth.ArgonauthException;
import com.gempukku.lotro.argonauth.PcAccountService;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

/**
 * `/gemp-lotr-server/argonauth/...`: the PC account's addresses, exactly as PC-ACCOUNT.md lists them. What each
 * does is {@link PcAccountService}'s; this turns requests into its calls and its answers into HTTP.
 *
 * Everything is answered later, from a virtual thread, since nearly all of it may wait on Argonauth: the request's
 * parameters are read here, on the server's IO thread, and this class touches nothing of the request after that
 * (its body is released when this returns; the response writer still reads its headers, which are not).
 */
public class ArgonauthRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private static final Logger _log = LogManager.getLogger(ArgonauthRequestHandler.class);

    private final PcAccountService _service;
    private final Executor _later;

    public ArgonauthRequestHandler(Map<Type, Object> context) {
        this(context, Executors.newVirtualThreadPerTaskExecutor());
    }

    ArgonauthRequestHandler(Map<Type, Object> context, Executor later) {
        super(context);
        _service = extractObject(context, PcAccountService.class);
        _later = later;
    }

    /** An answer that is worked out off the IO thread. */
    private interface Deferred {
        void answer() throws Exception;
    }

    private void later(ResponseWriter responseWriter, Deferred work) {
        _later.execute(() -> {
            try {
                work.answer();
            } catch (PcAccountService.Refusal refusal) {
                responseWriter.writeJsonResponse(refusal.getStatus(), refusal.toJson());
            } catch (Exception exp) {
                _log.error("A PC account request failed", exp);
                responseWriter.writeError(500);
            }
        });
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        boolean get = request.method() == HttpMethod.GET;
        boolean post = request.method() == HttpMethod.POST;
        LoggedUserHolder.Session session = getSession(request);

        if (uri.equals("/session") && get) {
            later(responseWriter, () -> responseWriter.writeJsonResponse(_service.view(session).toJson()));
        } else if (uri.equals("/logout") && post) {
            String scope = form(request, "scope").get("scope");
            later(responseWriter, () -> {
                JSONObject answer = new JSONObject();
                answer.put("next", _service.logOut(session, scope));
                responseWriter.writeJsonResponse(answer.toJSONString());
            });
        } else if (!_service.getConfig().enabled()) {
            // Test mode: there is no PC account, and nothing here to find.
            throw new HttpProcessingException(404);
        } else if (uri.equals("/login") && get) {
            QueryStringDecoder query = new QueryStringDecoder(request.uri());
            String then = getQueryParameterSafely(query, "then");
            String returnPath = getQueryParameterSafely(query, "return");
            // Register PC Account: the same trip, to Argonauth's sign-up page
            boolean signUp = "1".equals(getQueryParameterSafely(query, "register"));
            boolean https = cameOverHttps(request);
            // A browser that is nobody yet gets a session to carry the sign-in there and back. Anyone can ask for
            // that, any number of times, so there is a ceiling on sessions (and such a session is short-lived).
            if (session == null && _loggedUserHolder.isFull()) {
                _log.error("There are as many sessions as there may be: a PC sign-in was turned away");
                redirect(responseWriter, PcAccountService.APP + "/login?pc=failed", null);
                return;
            }
            LoggedUserHolder.Session asking = session != null ? session : _loggedUserHolder.newSession();
            later(responseWriter, () -> {
                String address;
                try {
                    address = _service.startSignIn(asking, then, returnPath, signUp);
                } catch (ArgonauthException exp) {
                    _log.warn("A PC sign-in could not be started (" + exp.getReason() + "): " + exp.getMessage());
                    address = PcAccountService.APP + "/login?pc=failed";
                }
                redirect(responseWriter, address, sessionCookie(https, asking.getId()));
            });
        } else if (uri.equals("/callback") && get) {
            QueryStringDecoder query = new QueryStringDecoder(request.uri());
            String code = getQueryParameterSafely(query, "code");
            String state = getQueryParameterSafely(query, "state");
            String error = getQueryParameterSafely(query, "error");
            boolean https = cameOverHttps(request);
            later(responseWriter, () -> {
                PcAccountService.Landing landing = _service.completeSignIn(session, code, state, error, remoteIp);
                redirect(responseWriter, landing.location(),
                        landing.session() == null ? null : sessionCookie(https, landing.session().getId()));
            });
        } else if (uri.equals("/connect") && post) {
            String password = form(request, "password").get("password");
            later(responseWriter, () -> {
                JSONObject answer = new JSONObject();
                answer.put("next", _service.startConnect(session, password, remoteIp));
                responseWriter.writeJsonResponse(answer.toJSONString());
            });
        } else if (uri.equals("/enter") && post) {
            int playerId;
            try {
                playerId = Integer.parseInt(String.valueOf(form(request, "playerId").get("playerId")));
            } catch (NumberFormatException exp) {
                throw new HttpProcessingException(400);
            }
            later(responseWriter, () -> responseWriter.writeJsonResponse(_service.enter(session, playerId, remoteIp).toJson()));
        } else if (uri.equals("/create") && post) {
            String name = form(request, "name").get("name");
            later(responseWriter, () -> responseWriter.writeJsonResponse(_service.create(session, name, remoteIp).toJson()));
        } else if (uri.equals("/link") && post) {
            Map<String, String> form = form(request, "login", "password");
            later(responseWriter, () -> responseWriter.writeJsonResponse(
                    _service.link(session, form.get("login"), form.get("password"), remoteIp).toJson()));
        } else {
            throw new HttpProcessingException(404);
        }
    }

    /** A 302. Nothing on the way to or from Argonauth is to be kept by a cache: the callback's address has the code in it. */
    private static void redirect(ResponseWriter responseWriter, String location, String cookie) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(LOCATION.toString(), location);
        headers.put(CACHE_CONTROL.toString(), "no-store");
        if (cookie != null)
            headers.put(SET_COOKIE.toString(), cookie);
        responseWriter.writeError(302, headers);
    }

    /** These fields of the request's form; a field that was not sent is absent. */
    private Map<String, String> form(HttpRequest request, String... names) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            for (String name : names) {
                String value = getFormParameterSafely(postDecoder, name);
                if (value != null)
                    values.put(name, value);
            }
        } finally {
            postDecoder.destroy();
        }
        return values;
    }
}
