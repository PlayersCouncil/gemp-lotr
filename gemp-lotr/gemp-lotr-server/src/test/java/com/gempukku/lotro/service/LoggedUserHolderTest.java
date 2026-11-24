package com.gempukku.lotro.service;

import com.gempukku.lotro.service.LoggedUserHolder.PcAccount;
import com.gempukku.lotro.service.LoggedUserHolder.PendingSignIn;
import com.gempukku.lotro.service.LoggedUserHolder.Session;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class LoggedUserHolderTest {
    private final LoggedUserHolder _sessions = new LoggedUserHolder();

    @Test
    public void aPasswordLoginIsASessionPlayingAsThatAlias() {
        String id = _sessions.logUser("Frodo");
        assertEquals("Frodo", _sessions.getLoggedUser(id));
        assertNull(_sessions.getSession(id).getPcAccount());
        assertNull(_sessions.getLoggedUser("nobody's"));
        assertNull(_sessions.getLoggedUser(null));
    }

    @Test
    public void idsAreLongAndDoNotRepeat() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            String id = _sessions.newSession().getId();
            assertTrue(id, id.matches("[A-Za-z0-9]{32}"));
            assertTrue(ids.add(id));
        }
    }

    @Test
    public void aPcAccountAndAnAliasAreKeptApart() {
        Session session = _sessions.newSession();
        session.setPcAccount(new PcAccount("sub-1", "Frodo Baggins", "token", 2000, "id-token"));
        assertNull(_sessions.getLoggedUser(session.getId()));

        session.setPlayer("Underhill");
        assertEquals("Underhill", _sessions.getLoggedUser(session.getId()));
        session.setPlayer(null);
        assertNull(_sessions.getLoggedUser(session.getId()));
        assertEquals("sub-1", _sessions.getSession(session.getId()).getPcAccount().sub());

        assertTrue(session.getPcAccount().isFresh(1999));
        assertFalse(session.getPcAccount().isFresh(2000));
    }

    @Test
    public void aCompletedSignInGetsANewIdAndTheOldOneIsWorthNothing() {
        Session before = _sessions.newSession();
        before.setPlayer("Frodo");
        before.setPcAccount(new PcAccount("sub-1", "Frodo Baggins", "token", 2000, "id-token"));

        Session after = _sessions.renew(before);

        assertNotEquals(before.getId(), after.getId());
        assertNull(_sessions.getSession(before.getId()));
        assertEquals("Frodo", _sessions.getLoggedUser(after.getId()));
        assertEquals("sub-1", after.getPcAccount().sub());
    }

    @Test
    public void aSignInIsAnsweredOnceByItsOwnStateAndInTime() {
        Session session = _sessions.newSession();
        PendingSignIn pending = new PendingSignIn("state-1", "nonce", "verifier", "link", "/hall", 1000);

        session.setPending(pending);
        assertNull(session.takePending("another state", 1001));
        // asked for with the wrong state, it is gone all the same
        assertNull(session.takePending("state-1", 1001));

        session.setPending(pending);
        assertSame(pending, session.takePending("state-1", 1000 + LoggedUserHolder.PENDING_SIGN_IN_MILLIS));
        assertNull(session.takePending("state-1", 1001));

        session.setPending(pending);
        assertNull(session.takePending("state-1", 1001 + LoggedUserHolder.PENDING_SIGN_IN_MILLIS));

        session.setPending(pending);
        assertNull(session.takePending(null, 1001));
    }

    @Test
    public void endingASessionAndABanBothEndIt() {
        String frodo = _sessions.logUser("Frodo");
        String frodoElsewhere = _sessions.logUser("Frodo");
        Session viaPcAccount = _sessions.newSession();
        viaPcAccount.setPcAccount(new PcAccount("sub-1", "Frodo Baggins", "token", 2000, "id-token"));
        viaPcAccount.setPlayer("Frodo");
        String sam = _sessions.logUser("Sam");

        _sessions.end(frodo);
        _sessions.end(null);
        assertNull(_sessions.getLoggedUser(frodo));
        assertEquals("Frodo", _sessions.getLoggedUser(frodoElsewhere));

        _sessions.forceLogoutUser("Frodo");
        assertNull(_sessions.getLoggedUser(frodoElsewhere));
        // the PC account signed in with the banned alias goes too: the person signs in again
        assertNull(_sessions.getSession(viaPcAccount.getId()));
        assertEquals("Sam", _sessions.getLoggedUser(sam));
    }

    @Test
    public void everyIdleSessionIsEndedByTheSweepNotOneAMinute() {
        String a = _sessions.logUser("A");
        String b = _sessions.logUser("B");
        String c = _sessions.logUser("C");
        long now = System.currentTimeMillis();

        _sessions.removeIdle(now + LoggedUserHolder.SESSION_IDLE_MILLIS - 60_000);
        assertEquals("A", _sessions.getLoggedUser(a));

        // b and c are left alone for a month; a is used again
        long later = now + LoggedUserHolder.SESSION_IDLE_MILLIS + 60_000;
        _sessions.removeIdle(later);
        assertNull(_sessions.getSession(b));
        assertNull(_sessions.getSession(c));
    }

    @Test
    public void aSessionThatIsNobodysIsNotKeptLong() {
        // anyone can have one made by asking to sign in: it carries the sign-in to Argonauth and back, no more
        LoggedUserHolder.Session nobody = _sessions.newSession();
        LoggedUserHolder.Session player = _sessions.getSession(_sessions.logUser("Frodo"));
        LoggedUserHolder.Session pcAccount = _sessions.newSession();
        pcAccount.setPcAccount(new LoggedUserHolder.PcAccount("sub", "Frodo Baggins", "token", 0, "id.token"));
        long now = System.currentTimeMillis();

        _sessions.removeIdle(now + LoggedUserHolder.NOBODYS_SESSION_IDLE_MILLIS - 60_000);
        assertNotNull(_sessions.getSession(nobody.getId()));

        _sessions.removeIdle(System.currentTimeMillis() + LoggedUserHolder.NOBODYS_SESSION_IDLE_MILLIS + 60_000);
        assertNull(_sessions.getSession(nobody.getId()));
        assertNotNull(_sessions.getSession(player.getId()));
        assertNotNull(_sessions.getSession(pcAccount.getId()));
    }
}
