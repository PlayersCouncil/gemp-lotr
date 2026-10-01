package com.gempukku.lotro.service;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.CachedPlayerDAO;
import com.gempukku.lotro.db.DbAccess;
import com.gempukku.lotro.db.IpBanDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.reflection.Pojo;
import org.sql2o.reflection.PojoMetadata;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Temporary bans: what the admin endpoint stores, how login reads it back, the expiry boundary, forced logout of
 * live sessions and the player cache.
 *
 * The ban timestamp is epoch milliseconds in player.banned_until (decimal(20,0)). Login reads the row through sql2o
 * into DBDefs.Player; the admin "similar accounts" view reads the same column through plain JDBC getLong. The two
 * paths must agree.
 */
public class TempBanTest {
    private static final long DAY = 24L * 60 * 60 * 1000;

    /** Maps one column value onto DBDefs.Player exactly the way DbPlayerDAO.loginUser's sql2o query does. */
    private static DBDefs.Player mapBannedUntilLikeSql2o(Object columnValue) {
        PojoMetadata metadata = new PojoMetadata(DBDefs.Player.class, false, false, Map.of(), true);
        Pojo pojo = new Pojo(metadata, false);
        pojo.setProperty("banned_until", columnValue, new NoQuirks(DbAccess.CustomMappers));
        pojo.setProperty("type", "u", new NoQuirks(DbAccess.CustomMappers));
        return (DBDefs.Player) pojo.getObject();
    }

    @Test
    public void loginPathKeepsTheFullMillisecondTimestamp() {
        long sevenDaysFromNow = System.currentTimeMillis() + 7 * DAY;
        // MySQL Connector/J hands a decimal(20,0) back as BigDecimal; cover the other integral types too
        for (Object dbValue : new Object[]{new BigDecimal(sevenDaysFromNow), BigInteger.valueOf(sevenDaysFromNow), sevenDaysFromNow}) {
            DBDefs.Player row = mapBannedUntilLikeSql2o(dbValue);
            Player player = new Player(row, false);

            assertNotNull(player.getBannedUntil());
            assertEquals("column value " + dbValue.getClass().getSimpleName(), sevenDaysFromNow, player.getBannedUntil().getTime());
            assertTrue(player.getBannedUntil().after(new Date()));
            assertTrue(player.isTemporarilyBannedAt(new Date()));
        }
    }

    @Test
    public void nullBanColumnMeansNotBanned() {
        Player player = new Player(mapBannedUntilLikeSql2o(null), false);
        assertNull(player.getBannedUntil());
        assertFalse(player.isTemporarilyBannedAt(new Date()));
    }

    @Test
    public void banLastsUpToButNotIncludingTheStoredInstant() {
        long until = 1_800_000_000_000L;
        Player player = new Player(1, "troll", "pw", "un", null, new Date(until), null, null, false);

        assertTrue(player.isTemporarilyBannedAt(new Date(until - 1)));
        assertFalse(player.isTemporarilyBannedAt(new Date(until)));
        assertFalse(player.isTemporarilyBannedAt(new Date(until + 1)));
    }

    @Test
    public void everyDurationOfferedByTheAdminPageEndsInTheFuture() throws Exception {
        // userAdmin.html offers 1, 3, 7, 14 and 30 days; anything >= 25 days used to overflow int arithmetic
        for (int days : new int[]{1, 3, 7, 14, 30, 90, 365}) {
            PlayerDAO dao = Mockito.mock(PlayerDAO.class);
            Mockito.when(dao.banPlayerTemporarily(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
            AdminService service = new AdminService(dao, Mockito.mock(IpBanDAO.class), new LoggedUserHolder());

            long before = System.currentTimeMillis();
            assertTrue(service.banUserTemp("troll", days));
            long after = System.currentTimeMillis();

            ArgumentCaptor<Long> until = ArgumentCaptor.forClass(Long.class);
            Mockito.verify(dao).banPlayerTemporarily(Mockito.eq("troll"), until.capture());
            assertTrue(days + " days: ban must end after now", until.getValue() > after);
            assertTrue(days + " days: ban length", until.getValue() >= before + days * DAY && until.getValue() <= after + days * DAY);
        }
    }

    @Test
    public void nonPositiveDurationIsRefusedWithoutTouchingTheDatabase() throws Exception {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        AdminService service = new AdminService(dao, Mockito.mock(IpBanDAO.class), new LoggedUserHolder());

        assertFalse(service.banUserTemp("troll", 0));
        assertFalse(service.banUserTemp("troll", -3));
        Mockito.verify(dao, Mockito.never()).banPlayerTemporarily(Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    public void tempBanEndsTheBannedPlayersLiveSessions() throws Exception {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Mockito.when(dao.banPlayerTemporarily(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
        LoggedUserHolder sessions = new LoggedUserHolder();
        String trollSession = sessions.logUser("troll");
        String bystanderSession = sessions.logUser("hobbit");

        assertTrue(new AdminService(dao, Mockito.mock(IpBanDAO.class), sessions).banUserTemp("troll", 3));

        assertNull(sessions.getLoggedUser(trollSession));
        assertEquals("hobbit", sessions.getLoggedUser(bystanderSession));
    }

    @Test
    public void tempBanAndUnbanRefreshTheCachedPlayer() throws Exception {
        PlayerDAO db = Mockito.mock(PlayerDAO.class);
        long until = System.currentTimeMillis() + 3 * DAY;
        Player clean = new Player(7, "troll", "pw", "u", null, null, null, null, false);
        Player banned = new Player(7, "troll", "pw", "un", null, new Date(until), null, null, false);
        Player unbanned = new Player(7, "troll", "pw", "un", null, null, null, null, false);
        Mockito.when(db.getPlayer("troll")).thenReturn(clean, banned, unbanned);
        Mockito.when(db.banPlayerTemporarily("troll", until)).thenReturn(true);
        Mockito.when(db.unBanPlayer("troll")).thenReturn(true);

        CachedPlayerDAO cache = new CachedPlayerDAO(db);
        assertNull(cache.getPlayer("troll").getBannedUntil());
        assertNull("served from cache", cache.getPlayer("troll").getBannedUntil());

        assertTrue(cache.banPlayerTemporarily("troll", until));
        assertEquals(until, cache.getPlayer("troll").getBannedUntil().getTime());

        assertTrue(cache.unBanPlayer("troll"));
        assertNull(cache.getPlayer("troll").getBannedUntil());
    }
}
