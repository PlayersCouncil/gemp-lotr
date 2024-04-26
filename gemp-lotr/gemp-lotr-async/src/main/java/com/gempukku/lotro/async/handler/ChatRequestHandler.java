package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.ChatCommandErrorException;
import com.gempukku.lotro.chat.ChatMessage;
import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.game.ChatCommunicationChannel;
import com.gempukku.lotro.game.Player;
import com.gempukku.polling.LongPollingResource;
import com.gempukku.polling.LongPollingSystem;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ChatRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final ChatServer _chatServer;
    private final LongPollingSystem longPollingSystem;
    private final Parser _markdownParser;
    private final HtmlRenderer _markdownRenderer;

    private static final Logger _log = LogManager.getLogger(ChatRequestHandler.class);

    public ChatRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        super(context);
        _chatServer = extractObject(context, ChatServer.class);
        this.longPollingSystem = longPollingSystem;

        List<Extension> adminExt = Arrays.asList(StrikethroughExtension.create(), AutolinkExtension.create());
        _markdownParser = Parser.builder()
                .extensions(adminExt)
                .build();
        _markdownRenderer = HtmlRenderer.builder()
                .nodeRendererFactory(htmlContext -> new LinkShredder(htmlContext))
                .extensions(adminExt)
                .escapeHtml(true)
                .sanitizeUrls(true)
                .softbreak("<br />")
                .build();
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

    private Pattern QuoteExtender = Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE);

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
                if (message != null && message.trim().length() > 0) {
                    String newMsg;
                    newMsg = message.trim().replaceAll("\n\n\n+", "\n\n\n");
                    newMsg = QuoteExtender.matcher(newMsg).replaceAll("$1\n");
                    //Escaping underscores so that URLs with lots of underscores (i.e. wiki links) aren't mangled
                    // Besides, who uses _this_ instead of *this*?
                    newMsg = newMsg.replace("_", "\\_");

                    //Need to preserve any commands being made
                    if(!newMsg.startsWith("/")) {
                        newMsg = _markdownRenderer.render(_markdownParser.parse(newMsg));
                        // Prevent quotes with newlines from displaying side-by-side
                        newMsg = newMsg.replaceAll("</blockquote>[\n \t]*<blockquote>", "</blockquote><br /><blockquote>");
                        //Make all links open in a new tab
                        newMsg = newMsg.replaceAll("<(a href=\".*?\")>", "<$1 target=\"blank\">");
                    }

                    chatRoom.sendMessage(resourceOwner.getName(), newMsg, admin);
                    responseWriter.writeXmlResponse(null);
                } else {
                    ChatCommunicationChannel pollableResource = chatRoom.getChatRoomListener(resourceOwner.getName());
                    ChatUpdateLongPollingResource polledResource = new ChatUpdateLongPollingResource(chatRoom, room, resourceOwner.getName(), admin, responseWriter);
                    longPollingSystem.processLongPollingResource(polledResource, pollableResource);
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

    //Processing to implement:
    // + quotes restricted to one line
    // - triple quote to avoid this??
    // + remove url text processing
    // + remove image processing
    // - re-enable bare url linking

    private class LinkShredder implements NodeRenderer {

        private final HtmlWriter html;

        LinkShredder(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            // Return the node types we want to use this renderer for.
            return new HashSet<>(Arrays.asList(
               Link.class,
               Image.class
            ));
        }

        @Override
        public void render(Node node) {
            if(node instanceof Link link) {
                if(link.getTitle() != null) {
                    html.text(link.getTitle() + ": " + link.getDestination());
                }
                else {
                    if(link.getFirstChild() != null
                            && link.getFirstChild() instanceof Text text
                            && !text.getLiteral().equals(link.getDestination()))
                    {
                        html.text(text.getLiteral() + ": " + link.getDestination());
                    }
                    else {
                        html.tag("a", Collections.singletonMap("href", link.getDestination()));
                        html.text(link.getDestination());
                        html.tag("/a");
                    }
                }

            }
            else if(node instanceof Image image){
                html.text(image.getTitle() + ": " + image.getDestination());
            }
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

                    serializeChatRoomData(room, chatMessages, usersInRoom, doc);

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

            serializeChatRoomData(room, chatMessages, usersInRoom, doc);

            responseWriter.writeXmlResponse(doc);
        } catch (PrivateInformationException exp) {
            logHttpError(_log, 403, request.uri(), exp);
            throw new HttpProcessingException(403);
        }
    }

    private void serializeChatRoomData(String room, List<ChatMessage> chatMessages, Collection<String> usersInRoom, Document doc) {
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
        for (String userInRoom : usersInRoom)
            users.add(formatPlayerNameForChatList(userInRoom));

        for (String userInRoom : users) {
            Element user = doc.createElement("user");
            user.appendChild(doc.createTextNode(userInRoom));
            chatElem.appendChild(user);
        }
    }

    private static class CaseInsensitiveStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    private String formatPlayerNameForChatList(String userInRoom) {
        final Player player = _playerDao.getPlayer(userInRoom);
        if (player != null) {
            if (player.hasType(Player.Type.ADMIN))
                return "* "+userInRoom;
            else if (player.hasType(Player.Type.LEAGUE_ADMIN))
                return "+ "+userInRoom;
        }
        return userInRoom;
    }
}
