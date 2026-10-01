package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.*;
import com.gempukku.lotro.game.ChatCommunicationChannel;
import com.gempukku.lotro.game.Player;
import com.gempukku.polling.LongPollingResource;
import com.gempukku.polling.LongPollingSystem;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChatRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final ChatServer _chatServer;
    private final LongPollingSystem _longPollingSystem;
    private final MarkdownParser _markdownParser;

    private static final Logger _log = LogManager.getLogger(ChatRequestHandler.class);

    public ChatRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        super(context);
        _chatServer = extractObject(context, ChatServer.class);
        _markdownParser = extractObject(context, MarkdownParser.class);
        _longPollingSystem = longPollingSystem;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getMessages(request, URLDecoder.decode(uri.substring(1), StandardCharsets.UTF_8), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            postMessages(request, URLDecoder.decode(uri.substring(1), StandardCharsets.UTF_8), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void postMessages(HttpRequest request, String room, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String message = getFormParameterSafely(postDecoder, "message");

            Player resourceOwner = getResourceOwnerSafely(request, participantId);

            ChatRoomMediator chatRoom = _chatServer.getChatRoom(room);
            if (chatRoom == null)
                throw new HttpProcessingException(404);

            try {
                final boolean admin = resourceOwner.hasType(Player.Type.ADMIN);
                final boolean leagueAdmin = resourceOwner.hasType(Player.Type.LEAGUE_ADMIN);
                if (message != null && !message.trim().isEmpty()) {
                    String newMsg = _markdownParser.renderMarkdown(message, true);
                    chatRoom.sendMessage(resourceOwner.getName(), newMsg, admin);
                    responseWriter.writeXmlResponse(null);
                } else {
                    ChatCommunicationChannel pollableResource = chatRoom.getChatRoomListener(resourceOwner.getName());
                    ChatUpdateLongPollingResource polledResource = new ChatUpdateLongPollingResource(chatRoom, room, resourceOwner.getName(), admin, responseWriter);
                    _longPollingSystem.processLongPollingResource(polledResource, pollableResource);
                }
            } catch (SubscriptionExpiredException exp) {
                logHttpError(_log, 410, request.uri(), exp);
                throw new HttpProcessingException(410);
            } catch (PrivateInformationException exp) {
                logHttpError(_log, 403, request.uri(), exp);
                throw new HttpProcessingException(403);
            } catch (ChatCommandErrorException exp) {
                logHttpError(_log, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
        } finally {
            postDecoder.destroy();
        }
    }


    private class ChatUpdateLongPollingResource implements LongPollingResource {
        private final ChatRoomMediator chatRoom;
        private final String room;
        private final String playerId;
        private final boolean admin;
        private final ResponseWriter responseWriter;
        private boolean processed;

        private ChatUpdateLongPollingResource(ChatRoomMediator chatRoom, String room, String playerId, boolean admin, ResponseWriter responseWriter) {
            this.chatRoom = chatRoom;
            this.room = room;
            this.playerId = playerId;
            this.admin = admin;
            this.responseWriter = responseWriter;
        }

        @Override
        public synchronized boolean wasProcessed() {
            return processed;
        }

        @Override
        public synchronized void processIfNotProcessed() {
            if (!processed) {
                try {
                    List<ChatMessage> chatMessages = chatRoom.getChatRoomListener(playerId).consumeMessages();

                    Collection<String> usersInRoom = chatRoom.getUsersInRoom(admin);

                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                    Document doc = documentBuilder.newDocument();

                    serializeChatRoomData(room, chatMessages, usersInRoom, playerId, chatRoom, doc);

                    responseWriter.writeXmlResponse(doc);
                } catch (SubscriptionExpiredException exp) {
                    logHttpError(_log, 410, "chat poller", exp);
                    responseWriter.writeError(410);
                } catch (Exception exp) {
                    logHttpError(_log, 500, "chat poller", exp);
                    responseWriter.writeError(500);
                }
                processed = true;
            }
        }
    }

    private void getMessages(HttpRequest request, String room, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        Player resourceOwner = getResourceOwnerSafely(request, participantId);

        ChatRoomMediator chatRoom = _chatServer.getChatRoom(room);
        if (chatRoom == null)
            throw new HttpProcessingException(404);
        try {
            final boolean admin = resourceOwner.hasType(Player.Type.ADMIN);
            List<ChatMessage> chatMessages = chatRoom.joinUser(resourceOwner.getName(), admin);
            Collection<String> usersInRoom = chatRoom.getUsersInRoom(admin);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            serializeChatRoomData(room, chatMessages, usersInRoom, resourceOwner.getName(), chatRoom, doc);

            responseWriter.writeXmlResponse(doc);
        } catch (PrivateInformationException exp) {
            logHttpError(_log, 403, request.uri(), exp);
            throw new HttpProcessingException(403);
        }
    }

    /**
     * The room's messages and user list.  Each {@code <user>} carries the bare player name as its text; roles travel
     * as attributes: {@code role="admin"} or {@code role="leagueAdmin"} (absent otherwise), and on the viewer's own
     * entry {@code self="true"} plus {@code incognito="true|false"}.  The viewer's own entry comes first and is listed
     * even while they are incognito (which otherwise hides them from everyone but admins).
     */
    private void serializeChatRoomData(String room, List<ChatMessage> chatMessages, Collection<String> usersInRoom,
                                       String viewer, ChatRoomMediator chatRoom, Document doc) {
        Element chatElem = doc.createElement("chat");
        chatElem.setAttribute("roomName", room);
        doc.appendChild(chatElem);

        for (ChatMessage chatMessage : chatMessages) {
            Element message = doc.createElement("message");
            message.setAttribute("from", chatMessage.getFrom());
            message.setAttribute("date", String.valueOf(chatMessage.getWhen().getTime()));
            message.appendChild(doc.createTextNode(chatMessage.getMessage()));
            chatElem.appendChild(message);
        }

        Set<String> users = new TreeSet<>(new CaseInsensitiveStringComparator());
        users.addAll(usersInRoom);

        if (viewer != null && (users.contains(viewer) || chatRoom.isInRoom(viewer))) {
            users.remove(viewer);
            Element self = createUserElement(doc, viewer);
            self.setAttribute("self", "true");
            self.setAttribute("incognito", String.valueOf(chatRoom.isIncognito(viewer)));
            chatElem.appendChild(self);
        }

        for (String userInRoom : users)
            chatElem.appendChild(createUserElement(doc, userInRoom));
    }

    private Element createUserElement(Document doc, String userName) {
        Element user = doc.createElement("user");
        user.appendChild(doc.createTextNode(userName));
        String role = chatRoleOf(userName);
        if (role != null)
            user.setAttribute("role", role);
        return user;
    }

    private static class CaseInsensitiveStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    /** "admin", "leagueAdmin" or null - sent as the user element's role attribute instead of a name prefix. */
    private String chatRoleOf(String userInRoom) {
        final Player player = _playerDao.getPlayer(userInRoom);
        if (player != null) {
            if (player.hasType(Player.Type.ADMIN))
                return "admin";
            else if (player.hasType(Player.Type.LEAGUE_ADMIN))
                return "leagueAdmin";
        }
        return null;
    }
}
