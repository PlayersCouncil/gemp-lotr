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
}
