package com.gempukku.lotro.async.handler;

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

public class LoginRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private static final Logger _log = LogManager.getLogger(LoginRequestHandler.class);

    private final LoginThrottle _loginThrottle;

    public LoginRequestHandler(Map<Type, Object> context) {
        super(context);
        _loginThrottle = extractObject(context, LoginThrottle.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.POST) {
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
            String login = getFormParameterSafely(postDecoder, "login");
            String password = getFormParameterSafely(postDecoder, "password");

            // Too many wrong passwords for this name or from this address: refused without checking the password
            long now = System.currentTimeMillis();
            if (_loginThrottle.isBlocked(login, remoteIp, now))
                throw new HttpProcessingException(429);

            Player player = _playerDao.loginUser(login, password);
            if (player != null) {
                _loginThrottle.succeeded(login, now);
                // loginUser never answers an account with a blank password, so there is no "set your password" case
                if (player.getType().contains(Player.Type.USER.getValue())) {
                    if (player.isTemporarilyBannedAt(new Date()))
                        throw new HttpProcessingException(409);
                    else
                        responseWriter.writeXmlResponse(null, logUserReturningHeaders(request, remoteIp, login));
                } else {
                    throw new HttpProcessingException(403);
                }
            } else {
                _loginThrottle.failed(login, remoteIp, now);
                throw new HttpProcessingException(401);
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }

}
