package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.EventSerializer;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.polling.LongPollingResource;
import com.gempukku.polling.LongPollingSystem;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final LotroServer _lotroServer;
    private final LongPollingSystem longPollingSystem;
    private final Set<Phase> _autoPassDefault = new HashSet<>();

    private static final Logger _log = LogManager.getLogger(GameRequestHandler.class);

    public GameRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        super(context);
        _lotroServer = extractObject(context, LotroServer.class);
        this.longPollingSystem = longPollingSystem;

        _autoPassDefault.add(Phase.FELLOWSHIP);
        _autoPassDefault.add(Phase.MANEUVER);
        _autoPassDefault.add(Phase.ARCHERY);
        _autoPassDefault.add(Phase.ASSIGNMENT);
        _autoPassDefault.add(Phase.REGROUP);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/") && uri.endsWith("/cardInfo") && request.method() == HttpMethod.GET) {
            getCardInfo(request, uri.substring(1, uri.length() - 9), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/concede") && request.method() == HttpMethod.POST) {
            concede(request, uri.substring(1, uri.length() - 8), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/cancel") && request.method() == HttpMethod.POST) {
            cancel(request, uri.substring(1, uri.length() - 7), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getGameState(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            updateGameState(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void updateGameState(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        int channelNumber = Integer.parseInt(getFormParameterSafely(postDecoder, "channelNumber"));
        Integer decisionId = null;
        String decisionIdStr = getFormParameterSafely(postDecoder, "decisionId");
        if (decisionIdStr != null)
            decisionId = Integer.parseInt(decisionIdStr);
        String decisionValue = getFormParameterSafely(postDecoder, "decisionValue");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        LotroGameMediator gameMediator = _lotroServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(404);

        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), getAutoPassPhases(request));

        try {
            if (decisionId != null)
                gameMediator.playerAnswered(resourceOwner, channelNumber, decisionId, decisionValue);

            GameCommunicationChannel pollableResource = gameMediator.getCommunicationChannel(resourceOwner, channelNumber);
            GameUpdateLongPollingResource pollingResource = new GameUpdateLongPollingResource(pollableResource, channelNumber, gameMediator, resourceOwner, responseWriter);
            longPollingSystem.processLongPollingResource(pollingResource, pollableResource);
        } catch (SubscriptionConflictException exp) {
            throw new HttpProcessingException(409);
        } catch (PrivateInformationException e) {
            throw new HttpProcessingException(403);
        } catch (SubscriptionExpiredException e) {
            throw new HttpProcessingException(410);
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private class GameUpdateLongPollingResource implements LongPollingResource {
        private final GameCommunicationChannel _gameCommunicationChannel;
        private final LotroGameMediator _gameMediator;
        private final Player _resourceOwner;
        private final int _channelNumber;
        private final ResponseWriter _responseWriter;
        private boolean _processed;

        private GameUpdateLongPollingResource(GameCommunicationChannel gameCommunicationChannel, int channelNumber, LotroGameMediator gameMediator, Player resourceOwner, ResponseWriter responseWriter) {
            _gameCommunicationChannel = gameCommunicationChannel;
            _channelNumber = channelNumber;
            _gameMediator = gameMediator;
            _resourceOwner = resourceOwner;
            _responseWriter = responseWriter;
        }

        @Override
        public synchronized boolean wasProcessed() {
            return _processed;
        }

        @Override
        public synchronized void processIfNotProcessed() {
            if (!_processed) {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                    Document doc = documentBuilder.newDocument();
                    Element update = doc.createElement("update");

                    _gameMediator.processVisitor(_gameCommunicationChannel, _channelNumber, _resourceOwner.getName(), new SerializationVisitor(doc, update));

                    doc.appendChild(update);

                    _responseWriter.writeXmlResponse(doc);
                } catch (Exception e) {
                    logHttpError(_log, 500, "game update poller", e);
                    _responseWriter.writeError(500);
                }
                _processed = true;
            }
        }

    }

    private void cancel(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        LotroGameMediator gameMediator = _lotroServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(404);

        gameMediator.cancel(resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void concede(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        LotroGameMediator gameMediator = _lotroServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(404);

        gameMediator.concede(resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getCardInfo(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String cardIdStr = getQueryParameterSafely(queryDecoder, "cardId");
        if (cardIdStr.startsWith("extra")) {
            responseWriter.writeHtmlResponse("");
        } else {
            int cardId = Integer.parseInt(cardIdStr);

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            LotroGameMediator gameMediator = _lotroServer.getGameById(gameId);
            if (gameMediator == null)
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse(gameMediator.produceCardInfo(resourceOwner, cardId));
        }
    }

    private void getGameState(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        LotroGameMediator gameMediator = _lotroServer.getGameById(gameId);

        if (gameMediator == null)
            throw new HttpProcessingException(404);

        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), getAutoPassPhases(request));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element gameState = doc.createElement("gameState");

        try {
            gameMediator.signupUserForGame(resourceOwner, new SerializationVisitor(doc, gameState));
        } catch (PrivateInformationException e) {
            throw new HttpProcessingException(403);
        }

        doc.appendChild(gameState);

        responseWriter.writeXmlResponse(doc);
    }

    private Set<Phase> getAutoPassPhases(HttpRequest request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("autoPassPhases")) {
                    final String[] phases = cookie.value().split("0");
                    Set<Phase> result = new HashSet<>();
                    for (String phase : phases)
                        result.add(Phase.valueOf(phase));
                    return result;
                }
            }
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("autoPass") && cookie.value().equals("false"))
                    return Collections.emptySet();
            }
        }
        return _autoPassDefault;
    }

    private class SerializationVisitor implements ParticipantCommunicationVisitor {
        private final Document _doc;
        private final Element _element;
        private final EventSerializer _eventSerializer = new EventSerializer();

        private SerializationVisitor(Document doc, Element element) {
            _doc = doc;
            _element = element;
        }

        @Override
        public void visitChannelNumber(int channelNumber) {
            _element.setAttribute("cn", String.valueOf(channelNumber));
        }

        @Override
        public void visitGameEvent(GameEvent gameEvent) {
            _element.appendChild(_eventSerializer.serializeEvent(_doc, gameEvent));
        }

        @Override
        public void visitClock(Map<String, Integer> secondsLeft) {
            _element.appendChild(serializeClocks(_doc, secondsLeft));
        }
    }

    private Node serializeClocks(Document doc, Map<String, Integer> secondsLeft) {
        Element clocks = doc.createElement("clocks");
        for (Map.Entry<String, Integer> userClock : secondsLeft.entrySet()) {
            Element clock = doc.createElement("clock");
            clock.setAttribute("participantId", userClock.getKey());
            clock.appendChild(doc.createTextNode(userClock.getValue().toString()));
            clocks.appendChild(clock);
        }

        return clocks;
    }
}
