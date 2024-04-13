package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.ResponseWriter;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.util.Map;

public interface UriRequestHandler {
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception;

    default void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 itself should error out
        if(code % 400 < 100 && code != 400) {
            log.debug("HTTP " + code + " response for " + uri);
        }
        // record an HTTP 400
        else if(code == 400 || code % 500 < 100) {
            log.error("HTTP code " + code + " response for " + uri, exp);
        }
    }
}
