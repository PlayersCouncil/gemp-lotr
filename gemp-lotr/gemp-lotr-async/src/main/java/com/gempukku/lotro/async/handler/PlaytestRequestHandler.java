package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Tester-flag endpoints.  The player-facing Playtesting tab (and its unauthenticated "recent replays" listing, which
 * exposed every format's latest games to anyone) has been retired now that playtesting runs on dedicated servers;
 * the PLAY_TESTER role and these flag endpoints stay.
 */
public class PlaytestRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private final PlayerDAO _playerDAO;

    private static final Logger _log = LogManager.getLogger(PlaytestRequestHandler.class);

    public PlaytestRequestHandler(Map<Type, Object> context) {
        super(context);
        _playerDAO = extractObject(context, PlayerDAO.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/addTesterFlag") && request.method() == HttpMethod.POST) {
            addTesterFlag(request, responseWriter);
        } else if (uri.equals("/removeTesterFlag") && request.method() == HttpMethod.POST) {
            removeTesterFlag(request, responseWriter);
        } else if (uri.equals("/getTesterFlag") && request.method() == HttpMethod.GET) {
            getTesterFlag(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void addTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player player = getResourceOwnerSafely(request, null);

            _playerDAO.addPlayerFlag(player.getName(), Player.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void removeTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player player = getResourceOwnerSafely(request, null);

            _playerDAO.removePlayerFlag(player.getName(), Player.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void getTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player player = getResourceOwnerSafely(request, null);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            Element hasTester = doc.createElement("hasTester");

            hasTester.setAttribute("result", String.valueOf(player.hasType(Player.Type.PLAY_TESTER)));

            responseWriter.writeXmlResponse(doc);

        } finally {
            postDecoder.destroy();
        }
    }
}
