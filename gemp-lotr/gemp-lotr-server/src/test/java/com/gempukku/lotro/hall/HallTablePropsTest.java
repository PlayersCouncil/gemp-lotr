package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.game.Adventure;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * The per-viewer table props the hall long-poll sends (contract C4): {@code createdAt} on every table and
 * {@code invitedYou="true"} only on the invitee's own channel.
 */
public class HallTablePropsTest {
    private TableHolder tableHolder;
    private LotroFormat format;

    @Before
    public void setUp() {
        IgnoreDAO ignoreDAO = Mockito.mock(IgnoreDAO.class);
        Mockito.when(ignoreDAO.getIgnoredUsers(Mockito.anyString())).thenReturn(Collections.emptySet());
        tableHolder = new TableHolder(null, null, ignoreDAO);

        Adventure adventure = Mockito.mock(Adventure.class);
        Mockito.when(adventure.isSolo()).thenReturn(false);
        format = Mockito.mock(LotroFormat.class);
        Mockito.when(format.getAdventure()).thenReturn(adventure);
        Mockito.when(format.getName()).thenReturn("Fellowship Block");
    }

    private GameSettings settings(String desc, boolean inviteOnly) {
        return new GameSettings(null, format, null, null, null, false, false, inviteOnly, false,
                GameTimer.DEFAULT_TIMER, desc, false);
    }

    private static Player player(String name) {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    /** Runs one viewer's channel against the table holder and returns the table props it would send. */
    private Map<String, Map<String, String>> tablesSeenBy(Player viewer) {
        HallServer hallServer = Mockito.mock(HallServer.class);
        Mockito.doAnswer(invocation -> {
            tableHolder.processTables(false, viewer, invocation.getArgument(1));
            return null;
        }).when(hallServer).processHall(Mockito.any(), Mockito.any());

        Map<String, Map<String, String>> added = new LinkedHashMap<>();
        HallChannelVisitor visitor = Mockito.mock(HallChannelVisitor.class);
        Mockito.doAnswer(invocation -> {
            added.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(visitor).addTable(Mockito.anyString(), Mockito.anyMap());

        new HallCommunicationChannel(1).processCommunicationChannel(hallServer, viewer, visitor);
        return added;
    }

    @Test
    public void isInviteeMatchesTheDescriptionConvention() {
        assertTrue(TableHolder.isInvitee(settings("Bob", true), "Bob"));
        assertTrue("the player lookup behind the invite is case-insensitive",
                TableHolder.isInvitee(settings("bob", true), "Bob"));
        assertTrue("library-deck tables prefix the description",
                TableHolder.isInvitee(settings("(New Player) Bob", true), "Bob"));
        assertFalse(TableHolder.isInvitee(settings("Bob", false), "Bob"));
        assertFalse("never a suffix match", TableHolder.isInvitee(settings("JimBob", true), "Bob"));
        assertFalse(TableHolder.isInvitee(settings("Bob", true), "Bobby"));
        assertFalse(TableHolder.isInvitee(settings(null, true), "Bob"));
        assertFalse(TableHolder.isInvitee(settings("Bob", true), null));
    }

    @Test
    public void waitingTablesCarryCreatedAtAndPerViewerInvite() throws HallException {
        long before = System.currentTimeMillis();
        tableHolder.createTable(player("Alice"), settings("Bob", true), new LotroDeck("deck"));
        tableHolder.createTable(player("Carol"), settings("anyone for a quick game?", false), new LotroDeck("deck"));
        long after = System.currentTimeMillis();

        Map<String, Map<String, String>> bobSees = tablesSeenBy(player("Bob"));
        assertEquals(2, bobSees.size());
        Map<String, String> invite = bobSees.get("1");
        assertEquals("true", invite.get("invitedYou"));
        assertEquals("true", invite.get("isInviteOnly"));
        assertEquals("Bob", invite.get("userDescription"));
        long createdAt = Long.parseLong(invite.get("createdAt"));
        assertTrue(createdAt >= before && createdAt <= after);
        assertNull("absent, not \"false\", on tables that are not an invite for you", bobSees.get("2").get("invitedYou"));
        assertNotNull(bobSees.get("2").get("createdAt"));

        Map<String, Map<String, String>> daveSees = tablesSeenBy(player("Dave"));
        assertNull(daveSees.get("1").get("invitedYou"));
        assertEquals(invite.get("createdAt"), daveSees.get("1").get("createdAt"));

        Map<String, Map<String, String>> aliceSees = tablesSeenBy(player("Alice"));
        assertNull("the creator is not the invitee", aliceSees.get("1").get("invitedYou"));
        assertEquals("true", aliceSees.get("1").get("playing"));
    }
}
