package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.AppConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class WebRequestHandler implements UriRequestHandler {
    private final String _root;

    public WebRequestHandler() {
        this(AppConfig.getWebPath());
    }

    public WebRequestHandler(String root) {
        _root = root;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals(""))
            uri = "index.html";

        uri = uri.replace('/', File.separatorChar);

        if ((uri.contains(".."))
                || uri.contains(File.separator + ".")
                || uri.startsWith(".") || uri.endsWith("."))
            throw new HttpProcessingException(403);

        File file = new File(_root + uri);
        if (!file.getCanonicalPath().startsWith(_root))
            throw new HttpProcessingException(403);

        // A directory serves its index.html (e.g. /gemp-lotr/ng/deckbuilder/ for the Angular deck builder)
        if (file.isDirectory())
            file = new File(file, "index.html");

        if (!file.exists())
            throw new HttpProcessingException(404);

        final String etag = "\""+file.lastModified()+"\"";

        if (clientHasCurrentVersion(request, etag))
            throw new HttpProcessingException(304);

        responseWriter.writeFile(file, Collections.singletonMap(HttpHeaderNames.ETAG.toString(), etag));
    }

    private boolean clientHasCurrentVersion(HttpRequest request, String etag) {
        String ifNoneMatch = request.headers().get(HttpHeaderNames.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            String[] clientKnownVersions = ifNoneMatch.split(",");
            for (String clientKnownVersion : clientKnownVersions) {
                if (clientKnownVersion.trim().equals(etag))
                    return true;
            }
        }
        return false;
    }
}
