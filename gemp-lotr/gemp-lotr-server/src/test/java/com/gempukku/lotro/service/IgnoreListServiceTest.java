package com.gempukku.lotro.service;

import com.gempukku.lotro.db.CachedIgnoreDAO;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.IgnoreListService.Outcome;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * The ignore-list logic behind GET /player/ignores and POST /player/ignores/add|remove, run through the real
 * {@link CachedIgnoreDAO} (as the server wires it) over an in-memory store.
 */
public class IgnoreListServiceTest {
    /** Stands in for the ignores table. */
    private static class MemoryIgnoreDAO implements IgnoreDAO {
        final Map<String, Set<String>> rows = new HashMap<>();
        int writes = 0;

        @Override
        public Set<String> getIgnoredUsers(String playerId) {
            return new TreeSet<>(rows.getOrDefault(playerId, Set.of()));
        }

        @Override
        public boolean addIgnoredUser(String playerId, String ignoredName) {
            writes++;
            rows.computeIfAbsent(playerId, k -> new TreeSet<>()).add(ignoredName);
            return true;
        }

        @Override
        public boolean removeIgnoredUser(String playerId, String ignoredName) {
            writes++;
            rows.getOrDefault(playerId, new TreeSet<>()).remove(ignoredName);
            return true;
        }
    }

    private MemoryIgnoreDAO _store;
    private CachedIgnoreDAO _ignoreDao;
    private IgnoreListService _service;

    private static Player player(String name) {
        return new Player(1, name, "pass", "u", null, null, null, null, false);
    }

    @Before
    public void setUp() {
        _store = new MemoryIgnoreDAO();
        _ignoreDao = new CachedIgnoreDAO(_store);
        PlayerDAO playerDao = Mockito.mock(PlayerDAO.class);
        for (String name : List.of("alice", "Bob", "carol", "Dave_99"))
            Mockito.when(playerDao.getPlayer(name)).thenReturn(player(name));
        // the players table matches names without regard to case in some installs; the registered spelling wins
        Mockito.when(playerDao.getPlayer("bob")).thenReturn(player("Bob"));
        Mockito.when(playerDao.getPlayer("ALICE")).thenReturn(player("alice"));
        _service = new IgnoreListService(_ignoreDao, playerDao);
    }

    @Test
    public void addingStoresTheRegisteredName() {
        var result = _service.ignore("alice", "  bob ");

        assertEquals(Outcome.ADDED, result.outcome());
        assertEquals("Bob", result.name());
        assertEquals(Set.of("Bob"), _store.rows.get("alice"));
        assertEquals(List.of("Bob"), _service.getIgnoredPlayers("alice"));
        // the cached set the hall's table filter reads is updated too
        assertTrue(_ignoreDao.getIgnoredUsers("alice").contains("Bob"));
    }

    @Test
    public void addingTwiceIsReportedNotDuplicated() {
        _service.ignore("alice", "Bob");
        int writes = _store.writes;

        var result = _service.ignore("alice", "BOB".toLowerCase());
        assertEquals(Outcome.ALREADY_IGNORED, result.outcome());
        assertEquals("Bob", result.name());
        assertEquals(writes, _store.writes);
        assertFalse(result.outcome().isError());
    }

    @Test
    public void sameValidationAsTheChatCommand() {
        assertEquals(Outcome.INVALID_NAME, _service.ignore("alice", "x").outcome());
        assertEquals(Outcome.INVALID_NAME, _service.ignore("alice", "   ").outcome());
        assertEquals(Outcome.INVALID_NAME, _service.ignore("alice", null).outcome());
        assertEquals(Outcome.INVALID_NAME, _service.ignore("alice", "y".repeat(31)).outcome());
        assertEquals(Outcome.SELF, _service.ignore("alice", "alice").outcome());
        assertEquals(Outcome.SELF, _service.ignore("alice", "ALICE").outcome());
        assertTrue(Outcome.SELF.isError());
        assertEquals(0, _store.writes);
    }

    @Test
    public void unknownPlayersAreRejected() {
        var result = _service.ignore("alice", "nobody");
        assertEquals(Outcome.UNKNOWN_PLAYER, result.outcome());
        assertTrue(result.message().contains("nobody"));
        assertEquals(0, _store.writes);
    }

    @Test
    public void removingTakesTheListedSpelling() {
        _service.ignore("alice", "Bob");
        _service.ignore("alice", "carol");

        var result = _service.unignore("alice", "bob");
        assertEquals(Outcome.REMOVED, result.outcome());
        assertEquals("Bob", result.name());
        assertEquals(List.of("carol"), _service.getIgnoredPlayers("alice"));
        assertFalse(_ignoreDao.getIgnoredUsers("alice").contains("Bob"));
    }

    @Test
    public void removingSomeoneNotListed() {
        assertEquals(Outcome.NOT_IGNORED, _service.unignore("alice", "carol").outcome());
        assertEquals(Outcome.INVALID_NAME, _service.unignore("alice", "").outcome());
        assertEquals(0, _store.writes);
    }

    @Test
    public void oldEntriesFromTheChatCommandCanStillBeRemoved() {
        // /ignore never checked that the player exists, so a list can hold names no player has
        _store.rows.put("alice", new TreeSet<>(Set.of("ghost", "Zed")));
        _ignoreDao.clearCache();

        assertEquals(List.of("ghost", "Zed"), _service.getIgnoredPlayers("alice"));
        assertEquals(Outcome.REMOVED, _service.unignore("alice", "GHOST").outcome());
        assertEquals(List.of("Zed"), _service.getIgnoredPlayers("alice"));
    }

    @Test
    public void listIsSortedIgnoringCaseAndPerPlayer() {
        _service.ignore("alice", "carol");
        _service.ignore("alice", "Bob");
        _service.ignore("alice", "Dave_99");
        _service.ignore("carol", "alice");

        assertEquals(List.of("Bob", "carol", "Dave_99"), _service.getIgnoredPlayers("alice"));
        assertEquals(List.of("alice"), _service.getIgnoredPlayers("carol"));
        assertEquals(List.of(), _service.getIgnoredPlayers("Bob"));
    }

    @Test
    public void messagesNameThePlayer() {
        assertEquals("Bob added to your ignore list.", _service.ignore("alice", "Bob").message());
        assertEquals("Bob removed from your ignore list.", _service.unignore("alice", "Bob").message());
        assertEquals("You cannot ignore yourself.", _service.ignore("alice", "alice").message());
    }

    @Test
    public void withoutAPlayerDaoItStillListsAndRemovesButCannotAdd() {
        _store.rows.put("alice", new TreeSet<>(Set.of("Galadriel_of_Lothlorien")));
        IgnoreListService removeOnly = new IgnoreListService(_ignoreDao);

        assertEquals(List.of("Galadriel_of_Lothlorien"), removeOnly.getIgnoredPlayers("alice"));
        assertEquals(Outcome.REMOVED, removeOnly.unignore("alice", "galadriel_of_lothlorien").outcome());
        assertTrue(_store.rows.get("alice").isEmpty());
        // validation still answers before the missing DAO matters; a real add is refused loudly, not silently
        assertEquals(Outcome.INVALID_NAME, removeOnly.ignore("alice", "x").outcome());
        assertThrows(IllegalStateException.class, () -> removeOnly.ignore("alice", "carol"));
    }
}
