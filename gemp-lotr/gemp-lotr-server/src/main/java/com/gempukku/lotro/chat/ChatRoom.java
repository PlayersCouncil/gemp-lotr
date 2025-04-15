package com.gempukku.lotro.chat;

import java.util.*;
import java.util.stream.Collectors;

public class ChatRoom {
    private static final int MAX_MESSAGE_HISTORY_COUNT = 500;
    private final LinkedList<ChatMessage> _lastMessages = new LinkedList<>();
    private final Map<String, ChatRoomInfo> _chatRoomListeners = new TreeMap<>(
            String::compareToIgnoreCase);
    private final boolean muteJoinPartMessages;
    private final boolean allowIncognito;

    public ChatRoom(boolean muteJoinPartMessages, boolean allowIncognito) {
        this.muteJoinPartMessages = muteJoinPartMessages;
        this.allowIncognito = allowIncognito;
    }

    public void setUserIncognitoMode(String username, boolean incognito) {
        if (allowIncognito) {
            final ChatRoomInfo chatRoomInfo = _chatRoomListeners.get(username);
            if (chatRoomInfo != null)
                chatRoomInfo.incognito = incognito;
        }
    }

    public void postMessage(String from, String message, boolean addToHistory, boolean fromAdmin) {
        ChatMessage chatMessage = new ChatMessage(new Date(), from, message, fromAdmin);
        if (addToHistory) {
            _lastMessages.add(chatMessage);
            shrinkLastMessages();
        }
        for (Map.Entry<String, ChatRoomInfo> listeners : _chatRoomListeners.entrySet())
            listeners.getValue().chatRoomListener.messageReceived(chatMessage);
    }

    public void postToUser(String from, String to, String message) {
        ChatMessage chatMessage = new ChatMessage(new Date(), from, message, false);
        final ChatRoomListener chatRoomListener = _chatRoomListeners.get(to).chatRoomListener;
        if (chatRoomListener != null) {
            chatRoomListener.messageReceived(chatMessage);
        }
    }

    public void joinChatRoom(String playerId, ChatRoomListener listener) {
        boolean wasInRoom = _chatRoomListeners.containsKey(playerId);
        _chatRoomListeners.put(playerId, new ChatRoomInfo(listener, false));
        for (ChatMessage lastMessage : _lastMessages)
            listener.messageReceived(lastMessage);
        if (!wasInRoom && !muteJoinPartMessages)
            postMessage("System", playerId + " joined the room", true, false);
    }

    public void partChatRoom(String playerId) {
        boolean wasInRoom = (_chatRoomListeners.remove(playerId) != null);
        if (wasInRoom && !muteJoinPartMessages)
            postMessage("System", playerId + " left the room", true, false);
    }

    public Collection<String> getUsersInRoom(boolean includeIncognito) {
        if (includeIncognito)
            return new ArrayList<>(_chatRoomListeners.keySet());
        else {
            return _chatRoomListeners.entrySet().stream().filter(
                    entry -> !entry.getValue().incognito).map(Map.Entry::getKey).collect(Collectors.toList());
        }
    }

    public long getSecsSinceLastMessage() {
        return (System.currentTimeMillis() - _lastMessages.getLast().getWhen().getTime()) / 1000;
    }

    private void shrinkLastMessages() {
        while (_lastMessages.size() > MAX_MESSAGE_HISTORY_COUNT) {
            _lastMessages.removeFirst();
        }
    }

    private static class ChatRoomInfo {
        private final ChatRoomListener chatRoomListener;
        private boolean incognito;

        public ChatRoomInfo(ChatRoomListener chatRoomListener, boolean incognito) {
            this.chatRoomListener = chatRoomListener;
            this.incognito = incognito;
        }
    }
}
