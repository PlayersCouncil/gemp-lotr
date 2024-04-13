package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson.JSON;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.GameHistoryService;
import com.gempukku.lotro.game.Player;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PlaytestRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private final PlayerDAO _playerDAO;
    private final GameHistoryService _gameHistoryService;
    private final Gson JsonConvert = new Gson();

    private static final Logger _log = LogManager.getLogger(PlaytestRequestHandler.class);

    public PlaytestRequestHandler(Map<Type, Object> context) {
        super(context);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/addTesterFlag") && request.method() == HttpMethod.POST) {
            addTesterFlag(request, responseWriter);
        } else if (uri.equals("/removeTesterFlag") && request.method() == HttpMethod.POST) {
            removeTesterFlag(request, responseWriter);
        } else if (uri.equals("/getTesterFlag") && request.method() == HttpMethod.GET) {
            getTesterFlag(request, responseWriter);
        } else if (uri.equals("/getRecentReplays") && request.method() == HttpMethod.POST) {
            getRecentReplays(request, responseWriter);
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

    private void getRecentReplays(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {

            String format = getFormParameterSafely(postDecoder, "format");
            int count = Integer.parseInt(getFormParameterSafely(postDecoder, "count"));

            final List<DBDefs.GameHistory> gameHistory = _gameHistoryService.getGameHistoryForFormat(format, count);

            responseWriter.writeJsonResponse(JSON.toJSONString(gameHistory));

        } finally {
            postDecoder.destroy();
        }
    }

}
