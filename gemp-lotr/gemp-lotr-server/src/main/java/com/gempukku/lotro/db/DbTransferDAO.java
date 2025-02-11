package com.gempukku.lotro.db;

import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.Player;
import org.sql2o.Query;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbTransferDAO implements TransferDAO {
    private final DbAccess _dbAccess;

    public static String FROM = "from";
    public static String TO = "to";

    public static String OPENED_PACK = "Opened pack";
    public static String ANNOUNCEMENT = "Announcement";

    public DbTransferDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public int addTransferFrom(String player, String reason, String collectionName, int currency, CardCollection items) {
        if (currency <= 0 && !items.getAll().iterator().hasNext())
            return -1;

        try {
            var db = _dbAccess.openDB();

            //notify and date_recorded are handled by default values in the database
            String sql = """
                        INSERT INTO transfer (player, reason, collection, currency, contents, direction)
                        VALUES (:player, :reason, :collection, :currency, :contents, :direction)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("player", player)
                        .addParameter("reason", reason)
                        .addParameter("collection", collectionName)
                        .addParameter("currency", currency)
                        .addParameter("contents", serializeContentsColumn(items))
                        .addParameter("direction", FROM);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert transfer from", ex);
        }
    }

    @Override
    public int addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency, CardCollection items) {
        if (currency <= 0 && !items.getAll().iterator().hasNext())
            return -1;

        try {
            var db = _dbAccess.openDB();

            //date_recorded is handled by a default value in the database
            String sql = """
                        INSERT INTO transfer (notify, player, reason, collection, currency, contents, direction)
                        VALUES (:notify, :player, :reason, :collection, :currency, :contents, :direction)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("notify", notifyPlayer)
                        .addParameter("player", player)
                        .addParameter("reason", reason)
                        .addParameter("collection", collectionName)
                        .addParameter("currency", currency)
                        .addParameter("contents", serializeContentsColumn(items))
                        .addParameter("direction", TO);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert transfer to", ex);
        }
    }

    @Override
    public boolean hasUndeliveredPackages(Player player) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT COUNT(*) 
                    FROM transfer 
                    WHERE player = :playerName 
                        AND notify = TRUE
                        AND date_recorded < NOW()
                """ + " AND reason NOT IN ('" + ANNOUNCEMENT + "', '" + OPENED_PACK + "') ";;
                Integer result = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .executeScalar(Integer.class);

                return result > 0;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve undelivered transfers for player", ex);
        }
    }

    @Override
    public boolean hasUndeliveredSealedContents(Player player) {
        return hasUndeliveredPackagesOfType(player, OPENED_PACK);
    }

    @Override
    public boolean hasUndeliveredAnnouncement(Player player) {
        return getUndeliveredAnnouncement(player) != null;
    }

    private boolean hasUndeliveredPackagesOfType(Player player, String type) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT COUNT(*) 
                    FROM transfer 
                    WHERE player = :playerName 
                        AND notify = TRUE
                        AND reason = :type
                        AND date_recorded < NOW();
                """;
                Integer result = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("type", type)
                        .executeScalar(Integer.class);

                return result > 0;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve undelivered transfers of type for player", ex);
        }
    }

    // For now, very naive synchronization.
    // This function delivers only items that are not server announcements or the result of a player opening packs.
    // Thus, this is intended to be used only by the game hall, and not the deckbuilder (where pack openings should go)
    @Override
    public synchronized Map<String, ? extends CardCollection> consumeUndeliveredPackages(Player player) {
        String clause = " AND reason NOT IN ('" + ANNOUNCEMENT + "', '" + OPENED_PACK + "') ";
        return consumePackages(player, clause);
    }

    @Override
    public synchronized Map<String, ? extends CardCollection> consumeUndeliveredPackContents(Player player) {
        String clause = " AND reason = '" + OPENED_PACK + "' ";
        return consumePackages(player, clause);
    }

    private synchronized Map<String, ? extends CardCollection> consumePackages(Player player, String whereClause) {
        try {
            var db = _dbAccess.openDB();

            List<DBDefs.Transfer> undeliveredRows = null;

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT id, notify, player, reason, collection, currency, contents, date_recorded, direction
                    FROM transfer 
                    WHERE player = :playerName 
                        AND notify = TRUE
                        AND date_recorded < NOW()
                """ + whereClause;

                undeliveredRows = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .executeAndFetch(DBDefs.Transfer.class);
            }

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                    UPDATE transfer 
                    SET notify = FALSE 
                    WHERE player = :playerName 
                        AND notify = TRUE
                        AND date_recorded < NOW()
                """ + whereClause;

                Query query = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addToBatch();
                query.executeBatch();
                conn.commit();
            }

            return convertTransferRowToCollectionMap(undeliveredRows);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to consume undelivered packages", ex);
        }
    }

    @Override
    public DBDefs.Announcement getUndeliveredAnnouncement(Player player) {
        try {
            var currentAnnouncement = getCurrentAnnouncement();
            var transferEntry = addAnnouncementEntryForPlayer(currentAnnouncement, player);

            if(transferEntry == null || !transferEntry.notify || DateUtils.Now().isBefore(transferEntry.GetUTCDateRecorded()))
                return null;

            return currentAnnouncement;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to get undelivered announcement for player", ex);
        }
    }

    @Override
    public DBDefs.Announcement getUndeliveredAnnouncement(Player player, DBDefs.Announcement currentAnnouncement) {
        try {
            if(currentAnnouncement == null) {
                currentAnnouncement = getCurrentAnnouncement();
            }
            var transferEntry = addAnnouncementEntryForPlayer(currentAnnouncement, player);

            if(transferEntry == null || !transferEntry.notify || DateUtils.Now().isBefore(transferEntry.GetUTCDateRecorded()))
                return null;

            return currentAnnouncement;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to get undelivered announcement for player", ex);
        }
    }

    @Override
    public synchronized void dismissAnnouncement(DBDefs.Announcement announcement, Player player) {
        if(announcement == null || player == null)
            return;

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                    UPDATE transfer 
                    SET notify = FALSE 
                    WHERE player = :playerName 
                        AND collection = :announcementId
                """ + " AND reason = '" + ANNOUNCEMENT + "' ";

                Query query = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("announcementId", String.valueOf(announcement.id))
                        .addToBatch();
                query.executeBatch();
                conn.commit();
            }

        } catch (Exception ex) {
            throw new RuntimeException("Unable to dismiss announcement for player", ex);
        }
    }

    @Override
    public synchronized void snoozeAnnouncement(DBDefs.Announcement announcement, Player player) {
        if(announcement == null || player == null)
            return;

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                    UPDATE transfer 
                    SET date_recorded = NOW() + INTERVAL 24 HOUR 
                    WHERE player = :playerName 
                        AND collection = :announcementId
                """ + " AND reason = '" + ANNOUNCEMENT + "' ";

                Query query = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("announcementId", String.valueOf(announcement.id))
                        .addToBatch();
                query.executeBatch();
                conn.commit();
            }

        } catch (Exception ex) {
            throw new RuntimeException("Unable to snooze announcement for player", ex);
        }
    }

    @Override
    public synchronized DBDefs.Transfer addAnnouncementEntryForPlayer(DBDefs.Announcement announcement, Player player) {

        if(announcement == null || player == null)
            return null;

        var existingEntry = getAnnouncementEntryForPlayer(announcement, player);
        if(existingEntry != null)
            return existingEntry;

        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO transfer (notify, player, reason, collection, contents, direction)
                        VALUES (:notify, :player, :reason, :collection, :contents, :direction)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true)
                        .addParameter("notify", true)
                        .addParameter("player", player.getName())
                        .addParameter("reason", ANNOUNCEMENT)
                        .addParameter("collection", String.valueOf(announcement.id))
                        .addParameter("contents", announcement.title)
                        .addParameter("direction", TO);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();


                sql = """
                    SELECT id, notify, player, reason, collection, contents, date_recorded, direction 
                    FROM transfer 
                    WHERE id = :id;
                """;

                var result = conn.createQuery(sql, true)
                        .addParameter("id", id)
                        .executeAndFetchFirst(DBDefs.Transfer.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert announcement transfer entry for player", ex);
        }
    }

    public synchronized DBDefs.Transfer getAnnouncementEntryForPlayer(DBDefs.Announcement announcement, Player player) {
        if(announcement == null || player == null)
            return null;

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT id, notify, player, reason, collection, contents, date_recorded, direction 
                    FROM transfer 
                    WHERE player = :playerName 
                        AND collection = :announcementId
                        AND reason = :type;
                """;
                DBDefs.Transfer result = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("announcementId", String.valueOf(announcement.id))
                        .addParameter("type", ANNOUNCEMENT)
                        .executeAndFetchFirst(DBDefs.Transfer.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve announcement transfer entry for player", ex);
        }
    }

    @Override
    public int addServerAnnouncement(String title, String markdown, ZonedDateTime start, ZonedDateTime until) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO announcements (title, content, start, until)
                        VALUES (:title, :content, :start, :until)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true)
                        .addParameter("title", title)
                        .addParameter("content", markdown)
                        .addParameter("start", DateUtils.FormatDateTime(start))
                        .addParameter("until", DateUtils.FormatDateTime(until));

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert announcement", ex);
        }
    }

    @Override
    public DBDefs.Announcement getCurrentAnnouncement() {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT id, title, content, start, until 
                    FROM announcements 
                    WHERE until > NOW()
                        AND start < NOW()
                    ORDER BY start DESC, id DESC
                    LIMIT 1;
                """;
                DBDefs.Announcement result = conn.createQuery(sql)
                        .executeAndFetchFirst(DBDefs.Announcement.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve undelivered transfers for player", ex);
        }
    }

    private Map<String, ? extends CardCollection> convertTransferRowToCollectionMap(List<DBDefs.Transfer> rows) {
        Map<String, DefaultCardCollection> result = new HashMap<>();

        for(var row : rows) {
            var collection = result.get(row.collection);
            if(collection == null) {
                collection = new DefaultCardCollection();
            }

            collection.addCurrency(row.currency);
            var contents = deserializeContentsColumn(row.contents);
            contents.getAll().forEach(collection::addItem);

            result.put(row.collection, collection);
        }

        return result;
    }

    private String serializeContentsColumn(CardCollection cardCollection) {
        var sb = new StringBuilder();
        for (CardCollection.Item item : cardCollection.getAll()) {
            sb.append(item.getCount()).append("x").append(item.getBlueprintId()).append(",");
        }
        return sb.toString();
    }

    private CardCollection deserializeContentsColumn(String collection) {
        var cardCollection = new DefaultCardCollection();
        for (String item : collection.split(",")) {
            if (item.isEmpty())
                continue;

            String[] itemSplit = item.split("x", 2);
            int count = Integer.parseInt(itemSplit[0]);
            String blueprintId = itemSplit[1];
            cardCollection.addItem(blueprintId, count);
        }

        return cardCollection;
    }
}
