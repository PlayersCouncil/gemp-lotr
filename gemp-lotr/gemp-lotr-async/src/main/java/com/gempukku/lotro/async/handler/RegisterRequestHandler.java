package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.argonauth.ArgonauthConfig;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.db.LoginInvalidException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.util.Map;

public class RegisterRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private static final Logger _log = LogManager.getLogger(RegisterRequestHandler.class);
    private final ArgonauthConfig _argonauth;

    public RegisterRequestHandler(Map<Type, Object> context) {
        super(context);
        _argonauth = extractObject(context, ArgonauthConfig.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.POST) {
            // Where there are PC accounts, a new account is made through one (Create New Gemp Alias) and no other way.
            if (_argonauth != null && _argonauth.enabled())
                throw new HttpProcessingException(403);
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
            String login = getFormParameterSafely(postDecoder, "login");
            String password = getFormParameterSafely(postDecoder, "password");
            try {
                if (_playerDao.registerUser(login, password, remoteIp)) {
                    responseWriter.writeXmlResponse(null, logUserReturningHeaders(request, remoteIp, login));
                } else {
                    throw new HttpProcessingException(409);
                }
            } catch (LoginInvalidException exp) {
                logHttpError(_log, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
