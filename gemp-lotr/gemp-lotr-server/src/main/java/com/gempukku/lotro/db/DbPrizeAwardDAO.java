package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import org.sql2o.Query;

import java.util.List;

public class DbPrizeAwardDAO implements PrizeAwardDAO {
    private final DbAccess _dbAccess;

    public DbPrizeAwardDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public List<DBDefs.PrizeAward> getAwards(String eventKind, String eventId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT id, event_kind, event_id, event_name, tier_index, tier_label, player, items, awarded_on
                        FROM gemp_db.prize_award
                        WHERE event_kind = :event_kind AND event_id = :event_id
                        ORDER BY id;
                        """;
                return conn.createQuery(sql)
                        .addParameter("event_kind", eventKind)
                        .addParameter("event_id", eventId)
                        .executeAndFetch(DBDefs.PrizeAward.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve prize awards for " + eventKind + " " + eventId, ex);
        }
    }

    @Override
    public int addAward(DBDefs.PrizeAward award) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                        INSERT INTO gemp_db.prize_award
                            (event_kind, event_id, event_name, tier_index, tier_label, player, items, awarded_on)
                        VALUES (:event_kind, :event_id, :event_name, :tier_index, :tier_label, :player, :items, :awarded_on);
                        """;
                Query query = conn.createQuery(sql, true);
                int id = query.addParameter("event_kind", award.event_kind)
                        .addParameter("event_id", award.event_id)
                        .addParameter("event_name", award.event_name)
                        .addParameter("tier_index", award.tier_index)
                        .addParameter("tier_label", award.tier_label)
                        .addParameter("player", award.player)
                        .addParameter("items", award.items)
                        .addParameter("awarded_on", award.awarded_on != null ? award.awarded_on : DateUtils.Now().toLocalDateTime())
                        .executeUpdate().getKey(Integer.class);
                conn.commit();
                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to log prize award", ex);
        }
    }
}
