package com.gempukku.lotro.collection;

import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.Player;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** CachedTransferDAO's current announcement: the patch note marker is never shown, and a change is noticed. */
public class CachedTransferDAOAnnouncementTest {
    private final AtomicReference<DBDefs.Announcement> _current = new AtomicReference<>();
    private final AtomicInteger _currentLookups = new AtomicInteger();
    private final AtomicInteger _deliveryChecks = new AtomicInteger();
    private final AtomicLong _now = new AtomicLong(1_000_000);
    private final Player _player = new Player(1, "frodo", "pass", "u", null, null, null, null, false);

    private static DBDefs.Announcement announcement(int id, String content) {
        DBDefs.Announcement a = new DBDefs.Announcement();
        a.id = id;
        a.title = "Title " + id;
        a.content = content;
        a.start = LocalDateTime.of(2026, 9, 27, 0, 0);
        a.until = LocalDateTime.of(2026, 10, 11, 0, 0);
        return a;
    }

    /** the database: its current announcement is _current; a player has an undelivered one when there is one */
    private TransferDAO delegate() {
        return (TransferDAO) Proxy.newProxyInstance(TransferDAO.class.getClassLoader(), new Class<?>[]{TransferDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getCurrentAnnouncement" -> {
                        _currentLookups.incrementAndGet();
                        DBDefs.Announcement a = _current.get();
                        yield a == null ? null : announcement(a.id, a.content);     // a fresh copy, as the database gives
                    }
                    case "hasUndeliveredAnnouncement" -> {
                        _deliveryChecks.incrementAndGet();
                        yield _current.get() != null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private CachedTransferDAO cached() {
        return new CachedTransferDAO(delegate(), new MarkdownParser(), _now::get);
    }

    @Test
    public void patchNoteMarkerIsNotShown() {
        _current.set(announcement(7, "<!-- gemp-patchnote:2026-09-27-hall-overhaul -->\n### New\n- A thing.\n\n**[Read the full patch notes here](#patch-notes/2026-09-27-hall-overhaul)**\n"));
        String html = cached().getCurrentAnnouncement().content;
        assertFalse(html, html.contains("gemp-patchnote"));
        assertFalse(html, html.contains("&lt;!--"));
        assertTrue(html, html.startsWith("<h3>New</h3>"));
        assertTrue(html, html.contains("href=\"#patch-notes/2026-09-27-hall-overhaul\""));
    }

    @Test
    public void adminAnnouncementsRenderAsBefore() {
        _current.set(announcement(3, "**Worlds** are coming.\n<!-- not a marker -->"));
        String html = cached().getCurrentAnnouncement().content;
        assertEquals(new MarkdownParser().renderMarkdown("**Worlds** are coming.\n<!-- not a marker -->", false), html);
    }

    @Test
    public void theCurrentAnnouncementIsCachedBetweenChecks() {
        _current.set(announcement(1, "One"));
        CachedTransferDAO dao = cached();
        dao.getCurrentAnnouncement();
        int lookups = _currentLookups.get();
        _now.addAndGet(CachedTransferDAO.ANNOUNCEMENT_RECHECK_MS - 1);
        for (int i = 0; i < 10; i++)
            dao.getCurrentAnnouncement();
        assertEquals(lookups, _currentLookups.get());
    }

    @Test
    public void anAnnouncementThatStartsLaterReachesPlayersAlreadyChecked() {
        CachedTransferDAO dao = cached();
        assertFalse(dao.hasUndeliveredAnnouncement(_player));
        assertFalse(dao.hasUndeliveredAnnouncement(_player));
        assertEquals(1, _deliveryChecks.get());           // remembered: nothing for frodo

        _current.set(announcement(5, "Starts now"));       // its start time passed, or another process added it
        _now.addAndGet(CachedTransferDAO.ANNOUNCEMENT_RECHECK_MS);
        assertTrue(dao.hasUndeliveredAnnouncement(_player));
        assertEquals(5, dao.getCurrentAnnouncement().id);
    }

    @Test
    public void anEndedAnnouncementStopsBeingShown() {
        _current.set(announcement(5, "Running"));
        CachedTransferDAO dao = cached();
        assertEquals(5, dao.getCurrentAnnouncement().id);
        _current.set(null);                                 // its until passed
        _now.addAndGet(CachedTransferDAO.ANNOUNCEMENT_RECHECK_MS);
        assertNull(dao.getCurrentAnnouncement());
    }

    @Test
    public void addingAPatchNoteAnnouncementRefreshesTheCache() {
        AtomicInteger inserts = new AtomicInteger();
        TransferDAO delegate = (TransferDAO) Proxy.newProxyInstance(TransferDAO.class.getClassLoader(), new Class<?>[]{TransferDAO.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "addServerAnnouncementIfAbsent" -> {
                        if (inserts.getAndIncrement() > 0)
                            yield -1;
                        _current.set(announcement(9, (String) args[2]));
                        yield 9;
                    }
                    case "getCurrentAnnouncement" -> _current.get() == null ? null : announcement(_current.get().id, _current.get().content);
                    case "hasUndeliveredAnnouncement" -> _current.get() != null;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        CachedTransferDAO dao = new CachedTransferDAO(delegate, new MarkdownParser(), _now::get);
        assertNull(dao.getCurrentAnnouncement());
        assertFalse(dao.hasUndeliveredAnnouncement(_player));

        assertEquals(9, dao.addServerAnnouncementIfAbsent("<!-- gemp-patchnote:2026-09-27 -->", "T",
                "<!-- gemp-patchnote:2026-09-27 -->\nNew.", null, null));
        assertEquals(9, dao.getCurrentAnnouncement().id);
        assertTrue(dao.hasUndeliveredAnnouncement(_player));   // without waiting for the recheck
        assertEquals(-1, dao.addServerAnnouncementIfAbsent("<!-- gemp-patchnote:2026-09-27 -->", "T", "x", null, null));
    }
}
