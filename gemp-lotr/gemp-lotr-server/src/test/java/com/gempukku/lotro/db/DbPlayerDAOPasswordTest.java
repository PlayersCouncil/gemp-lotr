package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * A blank stored password logs nobody in and lets nobody claim the account (PC-ACCOUNT.md, "Linking needs a real
 * password").  Accounts with a blank password are bots, aliases made through a PC account, and old admin resets
 * that were never completed.  Before, a blank password logged in with any password, and registering the account's
 * name set a new one.
 * <p>
 * The database is a stand-in: every query answers the rows {@link #rows} holds, as the real one would for the
 * player's name.
 */
public class DbPlayerDAOPasswordTest {
    private final List<DBDefs.Player> rows = new ArrayList<>();
    private Sql2o sql2o;
    private Connection connection;
    private Query query;
    private DbPlayerDAO dao;

    @Before
    public void setUp() {
        query = mock(Query.class, Mockito.RETURNS_SELF);
        when(query.executeAndFetch(DBDefs.Player.class)).thenAnswer(invocation -> new ArrayList<>(rows));
        connection = mock(Connection.class);
        when(connection.createQuery(anyString())).thenReturn(query);
        when(connection.getResult()).thenReturn(1);
        sql2o = mock(Sql2o.class);
        when(sql2o.open()).thenReturn(connection);
        when(sql2o.beginTransaction()).thenReturn(connection);
        DbAccess dbAccess = mock(DbAccess.class);
        when(dbAccess.openDB()).thenReturn(sql2o);
        dao = new DbPlayerDAO(dbAccess, mock(CollectionDAO.class));
    }

    private static String sha256(String password) throws Exception {
        StringBuilder hex = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)))
            hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private void account(String name, String storedPassword, String type) {
        DBDefs.Player row = new DBDefs.Player();
        row.id = rows.size() + 1;
        row.name = name;
        row.password = storedPassword;
        row.type = type;
        rows.add(row);
    }

    // ---- loginUser ----

    @Test
    public void aRealPasswordLogsIn() throws Exception {
        account("Frodo", sha256("ring"), "u");
        Player player = dao.loginUser("Frodo", "ring");
        assertNotNull(player);
        assertEquals("Frodo", player.getName());
    }

    @Test
    public void aWrongPasswordDoesNot() throws Exception {
        account("Frodo", sha256("ring"), "u");
        assertNull(dao.loginUser("Frodo", "rings"));
        assertNull(dao.loginUser("Frodo", ""));
        assertNull(dao.loginUser("Frodo", null));
    }

    @Test
    public void aBlankStoredPasswordNeverLogsIn() throws Exception {
        for (String stored : new String[]{"", null, "   "}) {
            rows.clear();
            account("Sam", stored, "u");
            String shown = stored == null ? "null" : "'" + stored + "'";
            assertNull("stored " + shown + ", typed a password", dao.loginUser("Sam", "anything"));
            assertNull("stored " + shown + ", typed nothing", dao.loginUser("Sam", ""));
            assertNull("stored " + shown + ", no password sent", dao.loginUser("Sam", null));
        }
    }

    @Test
    public void aBotCannotLogIn() throws Exception {
        account("~AragornBot", "", "b");
        assertNull(dao.loginUser("~AragornBot", ""));
        assertNull(dao.loginUser("~AragornBot", "x"));
    }

    @Test
    public void noLoginSentAsksTheDatabaseNothing() throws Exception {
        assertNull(dao.loginUser(null, "x"));
        verify(sql2o, never()).open();
    }

    @Test
    public void theHashIsComparedIgnoringCaseAsTheDatabaseDid() {
        assertTrue(DbPlayerDAO.storedPasswordMatches("ABCDEF", "abcdef"));
        assertFalse(DbPlayerDAO.storedPasswordMatches("abcdef", "abcdee"));
        assertFalse(DbPlayerDAO.storedPasswordMatches("", ""));
        assertFalse(DbPlayerDAO.storedPasswordMatches(null, "abcdef"));
        assertFalse(DbPlayerDAO.storedPasswordMatches("abcdef", null));
    }

    // ---- registerUser ----

    @Test
    public void aNewNameIsRegistered() throws Exception {
        assertTrue(dao.registerUser("Pippin", "took", "10.0.0.1"));
        verify(query).addParameter("login", "Pippin");
        verify(query).addParameter("password", sha256("took"));
        verify(query).executeUpdate();
    }

    @Test
    public void aTakenNameIsRefusedWhateverItsPassword() throws Exception {
        for (String stored : new String[]{"", null, sha256("ring")}) {
            rows.clear();
            account("Sam", stored, "u");
            assertFalse(dao.registerUser("Sam", "new", "10.0.0.1"));
            assertFalse("the name is taken ignoring case", dao.registerUser("SAM", "new", "10.0.0.1"));
        }
        verify(sql2o, never()).beginTransaction();
        verify(query, never()).executeUpdate();
    }

    @Test
    public void aBotNameCannotBeRegistered() throws Exception {
        try {
            dao.registerUser("~AragornBot", "x", "10.0.0.1");
            fail("a name starting ~ is not a valid login");
        } catch (LoginInvalidException expected) {
        }
        account("~AragornBot", "", "b");
        try {
            dao.registerUser("~AragornBot", "x", "10.0.0.1");
            fail("not even when the bot exists");
        } catch (LoginInvalidException expected) {
        }
        verify(query, never()).executeUpdate();
    }

    // ---- registerBot: unchanged ----

    @Test
    public void botsAreStillCreatedWithABlankPassword() throws Exception {
        assertTrue(dao.registerBot("~GandalfBot"));
        verify(query).addParameter("login", "~GandalfBot");
        verify(query).addParameter("password", "");
        verify(query).addParameter("type", Player.Type.BOT.toString());
        verify(query).executeUpdate();
    }

    @Test
    public void anExistingBotIsNotCreatedAgain() throws Exception {
        account("~GandalfBot", "", "b");
        assertFalse(dao.registerBot("~GandalfBot"));
        verify(query, never()).executeUpdate();
    }
}
