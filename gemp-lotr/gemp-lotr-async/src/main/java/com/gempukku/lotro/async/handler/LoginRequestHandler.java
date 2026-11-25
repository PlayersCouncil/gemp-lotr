package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.argonauth.PcAccountService;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.LoginThrottle;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private static final Logger _log = LogManager.getLogger(LoginRequestHandler.class);

    /**
     * The answer for an account a PC account has claimed (PC-ACCOUNT.md): it is entered through that PC account,
     * and by its password not at all. The page says "That account is linked to a PC Account, please use that to
     * log in." (Ketura, 2026-11-24).
     */
    public static final int LINKED_TO_PC_ACCOUNT = 423;

    private final LoginThrottle _loginThrottle;
    /** Absent where there are no PC accounts to ask about (some tests). */
    private final PcAccountService _pcAccounts;
    private final Executor _later;

    public LoginRequestHandler(Map<Type, Object> context) {
        this(context, Executors.newVirtualThreadPerTaskExecutor());
    }

    LoginRequestHandler(Map<Type, Object> context, Executor later) {
        super(context);
        _loginThrottle = extractObject(context, LoginThrottle.class);
        _pcAccounts = extractObject(context, PcAccountService.class);
        _later = later;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.POST) {
            String login;
            String password;
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
                // A name has no spaces in it: ones typed round it are dropped, so that "Frodo " is not a second way
                // to name Frodo (to the limit on attempts, and to a ban, which ends sessions by name).
                String typed = getFormParameterSafely(postDecoder, "login");
                login = typed == null ? null : typed.strip();
                password = getFormParameterSafely(postDecoder, "password");
            } finally {
                postDecoder.destroy();
            }
            boolean https = cameOverHttps(request);

            // Claimed by a PC account, the password is not looked at, right or wrong. Gemp's own copy answers for
            // nearly every name at once. For a name it says is claimed, Argonauth is asked whether that is still so
            // (an unlink there is not sent here), which may wait, and so is not done on this thread.
            if (_pcAccounts != null && _pcAccounts.isClaimedByCopy(login)) {
                _later.execute(() -> {
                    try {
                        if (_pcAccounts.claimedBy(login) != null)
                            responseWriter.writeError(LINKED_TO_PC_ACCOUNT);
                        else
                            byPassword(login, password, remoteIp, https, responseWriter);
                    } catch (HttpProcessingException exp) {
                        responseWriter.writeError(exp.getStatus());
                    } catch (Exception exp) {
                        _log.error("A login failed", exp);
                        responseWriter.writeError(500);
                    }
                });
                return;
            }
            byPassword(login, password, remoteIp, https, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void byPassword(String login, String password, String remoteIp, boolean https, ResponseWriter responseWriter) throws Exception {
        // Too many wrong passwords for this name or from this address: refused without checking the password
        long now = System.currentTimeMillis();
        if (_loginThrottle.isBlocked(login, remoteIp, now))
            throw new HttpProcessingException(429);

        Player player = _playerDao.loginUser(login, password);
        if (player != null) {
            _loginThrottle.succeeded(login, remoteIp, now);
            // loginUser never answers an account with a blank password, so there is no "set your password" case
            if (player.getType().contains(Player.Type.USER.getValue())) {
                if (player.isTemporarilyBannedAt(new Date()))
                    throw new HttpProcessingException(409);
                else
                    responseWriter.writeXmlResponse(null, logUserReturningHeaders(https, remoteIp, player.getName()));
            } else {
                throw new HttpProcessingException(403);
            }
        } else {
            _loginThrottle.failed(login, remoteIp, now);
            throw new HttpProcessingException(401);
        }
    }

}
