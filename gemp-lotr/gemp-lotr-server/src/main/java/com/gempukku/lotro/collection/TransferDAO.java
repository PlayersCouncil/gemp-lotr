package com.gempukku.lotro.collection;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface TransferDAO {
    boolean hasUndeliveredPackages(Player player);
    boolean hasUndeliveredSealedContents(Player player);
    boolean hasUndeliveredAnnouncement(Player player);
    Map<String, ? extends CardCollection> consumeUndeliveredPackages(Player player);
    Map<String, ? extends CardCollection> consumeUndeliveredPackContents(Player player);

    int addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency, CardCollection items);
    int addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency, CardCollection items, String message);
    int addTransferToRaw(boolean notifyPlayer, String player, String reason, String collectionName, int currency, String rawContents, String message);
    int addTransferFrom(String player, String reason, String collectionName, int currency, CardCollection items);

    boolean hasUndeliveredLeagueNotifications(Player player);
    List<DBDefs.Transfer> consumeUndeliveredLeagueNotifications(Player player);

    DBDefs.Announcement getUndeliveredAnnouncement(Player player);
    DBDefs.Announcement getUndeliveredAnnouncement(Player player, DBDefs.Announcement announcement);
    DBDefs.Transfer addAnnouncementEntryForPlayer(DBDefs.Announcement announcement, Player player);
    void dismissAnnouncement(DBDefs.Announcement announcement, Player player);
    void snoozeAnnouncement(DBDefs.Announcement announcement, Player player);

    DBDefs.Announcement getCurrentAnnouncement();
    int addServerAnnouncement(String title, String markdown, ZonedDateTime start, ZonedDateTime until);

    // Patch notes feed
    /**
     * Every announcement whose start has passed (ended ones included), newest first, with its Markdown content as
     * stored.  Server Info &gt; Patch Notes shows them among the notes (PatchNotesLibrary, which caches them).
     */
    List<DBDefs.Announcement> getPastAnnouncements();

    // Patch note announcements

    /**
     * Adds an announcement unless one whose content contains {@code marker} already exists (PatchNoteAnnouncer: the
     * marker names the patch note).  The check and the insert happen under a database-wide lock, so server
     * processes sharing a database never both add it.
     *
     * @return the new announcement's id, or -1 when one with the marker already exists
     */
    int addServerAnnouncementIfAbsent(String marker, String title, String markdown, ZonedDateTime start, ZonedDateTime until);
}
