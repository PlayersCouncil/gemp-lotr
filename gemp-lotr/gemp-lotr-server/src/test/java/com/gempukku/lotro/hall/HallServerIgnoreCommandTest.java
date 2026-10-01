package com.gempukku.lotro.hall;

import com.gempukku.lotro.chat.ChatCommandCallback;
import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.CachedIgnoreDAO;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.TournamentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * The hall's /ignore and /unignore chat commands: /unignore takes any name /ignore accepts (up to 30 characters, not
 * just 10 as it used to) and matches the list the same way My Account's Remove does.
 */
public class HallServerIgnoreCommandTest {
    private static class MemoryIgnoreDAO implements IgnoreDAO {
        final Map<String, Set<String>> rows = new HashMap<>();

        @Override
        public Set<String> getIgnoredUsers(String playerId) {
            return new TreeSet<>(rows.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean addIgnoredUser(String playerId, String ignoredName) {
            return rows.computeIfAbsent(playerId, k -> new TreeSet<>()).add(ignoredName);
        }

        @Override
        public boolean removeIgnoredUser(String playerId, String ignoredName) {
            return rows.getOrDefault(playerId, new TreeSet<>()).remove(ignoredName);
        }
    }

    private MemoryIgnoreDAO _store;
    private ChatRoomMediator _hallChat;
    private final Map<String, ChatCommandCallback> _commands = new HashMap<>();

    @Before
    public void setUp() {
        _store = new MemoryIgnoreDAO();
        ChatServer chatServer = Mockito.mock(ChatServer.class);
        _hallChat = Mockito.mock(ChatRoomMediator.class);
        Mockito.when(chatServer.createChatRoom(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(_hallChat);

        new HallServer(new CachedIgnoreDAO(_store), Mockito.mock(LotroServer.class), chatServer,
                Mockito.mock(LeagueService.class), Mockito.mock(TournamentService.class),
                Mockito.mock(LotroCardBlueprintLibrary.class), Mockito.mock(LotroFormatLibrary.class),
                Mockito.mock(CollectionsManager.class), Mockito.mock(AdminService.class));

        ArgumentCaptor<String> names = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatCommandCallback> callbacks = ArgumentCaptor.forClass(ChatCommandCallback.class);
        Mockito.verify(_hallChat, Mockito.atLeastOnce()).addChatCommandCallback(names.capture(), callbacks.capture());
        for (int i = 0; i < names.getAllValues().size(); i++)
            _commands.put(names.getAllValues().get(i), callbacks.getAllValues().get(i));
    }

    private String run(String command, String from, String parameters) throws Exception {
        Mockito.clearInvocations(_hallChat);
        _commands.get(command).commandReceived(from, parameters, false);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        Mockito.verify(_hallChat, Mockito.atLeastOnce()).sendToUser(Mockito.eq("System"), Mockito.eq(from), message.capture());
        return message.getValue();
    }

    @Test
    public void unignoreTakesALongName() throws Exception {
        String longName = "Galadriel_of_Lothlorien";   // 23 characters: /unignore used to refuse anything over 10
        assertEquals("User " + longName + " added to ignore list", run("ignore", "alice", " " + longName + " "));
        assertEquals(Set.of(longName), _store.rows.get("alice"));

        assertEquals("User " + longName + " removed from ignore list", run("unignore", "alice", longName));
        assertTrue(_store.rows.get("alice").isEmpty());
    }

    @Test
    public void unignoreAcceptsTheSameMaximumAsIgnore() throws Exception {
        String thirty = "x".repeat(30);
        run("ignore", "alice", thirty);
        assertEquals("User " + thirty + " removed from ignore list", run("unignore", "alice", thirty));

        String thirtyOne = "y".repeat(31);
        assertEquals(thirtyOne + " is not a valid username", run("ignore", "alice", thirtyOne));
        assertEquals(thirtyOne + " is not a valid username", run("unignore", "alice", thirtyOne));
        assertEquals(" is not a valid username", run("unignore", "alice", "   "));
    }

    @Test
    public void unignoreMatchesTheListIgnoringCase() throws Exception {
        _store.rows.put("alice", new TreeSet<>(Set.of("Bob_The_Builder")));
        assertEquals("User Bob_The_Builder removed from ignore list", run("unignore", "alice", "bob_the_builder"));
        assertTrue(_store.rows.get("alice").isEmpty());
    }

    @Test
    public void unignoringSomeoneNotListedSaysSo() throws Exception {
        assertEquals("User somebody_else wasn't on your ignore list. Try ignoring them first.",
                run("unignore", "alice", "somebody_else"));
    }
}
