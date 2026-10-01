package com.gempukku.lotro.async.handler;

import com.alibaba.fastjson2.JSON;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.IgnoreListService;
import com.gempukku.util.JsonUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerInfoRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {

    private static final Logger _log = LogManager.getLogger(PlayerInfoRequestHandler.class);

    /** The hall's chat room, as HallServer creates it; its user list is the one with the incognito option. */
    private static final String HALL_CHAT_ROOM = "Game Hall";

    private final IgnoreListService _ignoreListService;
    private final ChatServer _chatServer;

    public PlayerInfoRequestHandler(Map<Type, Object> context) {
        super(context);
        _ignoreListService = new IgnoreListService(extractObject(context, IgnoreDAO.class), _playerDao);
        _chatServer = extractObject(context, ChatServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, "participantId");
            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            responseWriter.writeJsonResponse(JsonUtils.Serialize(resourceOwner.GetUserInfo()));

        // ==== tabs-account: ignore list and hall incognito for the logged-in player ====
        } else if (uri.equals("/ignores") && request.method() == HttpMethod.GET) {
            getIgnores(request, responseWriter);
        } else if (uri.equals("/ignores/add") && request.method() == HttpMethod.POST) {
            changeIgnore(request, responseWriter, true);
        } else if (uri.equals("/ignores/remove") && request.method() == HttpMethod.POST) {
            changeIgnore(request, responseWriter, false);
        } else if (uri.equals("/incognito") && request.method() == HttpMethod.POST) {
            setIncognito(request, responseWriter);
        // ==== end tabs-account ====
        } else {
            throw new HttpProcessingException(404);
        }
    }

    // ==== tabs-account ====

    /** GET /player/ignores -> {"ignored": [names, sorted]} */
    private void getIgnores(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Player resourceOwner = getResourceOwnerSafely(request, getQueryParameterSafely(queryDecoder, "participantId"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ignored", _ignoreListService.getIgnoredPlayers(resourceOwner.getName()));
        responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
    }

    /**
     * POST /player/ignores/add or /player/ignores/remove, form parameter "name".  Always 200 with
     * {"outcome", "changed", "error", "name", "message", "ignored"}: a rejected name (too short or long, yourself, no
     * such player) is a normal answer with error=true and a message to show, not an HTTP error.
     */
    private void changeIgnore(HttpRequest request, ResponseWriter responseWriter, boolean add) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player resourceOwner = getResourceOwnerSafely(request, getFormParameterSafely(postDecoder, "participantId"));
            String name = getFormParameterSafely(postDecoder, "name");

            IgnoreListService.Result change = add
                    ? _ignoreListService.ignore(resourceOwner.getName(), name)
                    : _ignoreListService.unignore(resourceOwner.getName(), name);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("outcome", change.outcome().name());
            result.put("changed", change.outcome() == IgnoreListService.Outcome.ADDED || change.outcome() == IgnoreListService.Outcome.REMOVED);
            result.put("error", change.outcome().isError());
            result.put("name", change.name());
            result.put("message", change.message());
            result.put("ignored", _ignoreListService.getIgnoredPlayers(resourceOwner.getName()));
            responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
        } finally {
            postDecoder.destroy();
        }
    }

    /**
     * POST /player/incognito, form parameter "incognito" (true/false): the same switch as the /incognito and
     * /endIncognito chat commands.  It lasts while you stay in the hall chat; re-entering the hall resets it to
     * visible.  -> {"inRoom": bool, "incognito": bool}; 409 when you are not in the hall chat.
     */
    private void setIncognito(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            Player resourceOwner = getResourceOwnerSafely(request, getFormParameterSafely(postDecoder, "participantId"));
            boolean incognito = Boolean.parseBoolean(getFormParameterSafely(postDecoder, "incognito"));

            ChatRoomMediator hallChat = _chatServer.getChatRoom(HALL_CHAT_ROOM);
            if (hallChat == null || !hallChat.isInRoom(resourceOwner.getName()))
                throw new HttpProcessingException(409, "You are not in the Game Hall chat.");
            hallChat.setIncognito(resourceOwner.getName(), incognito);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("inRoom", true);
            result.put("incognito", hallChat.isIncognito(resourceOwner.getName()));
            responseWriter.writeJsonResponse(JsonUtils.Serialize(result));
        } finally {
            postDecoder.destroy();
        }
    }
    // ==== end tabs-account ====
}
