package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import org.sql2o.Query;

import java.util.List;

public class DbPrizePlaceholderDAO implements PrizePlaceholderDAO {
    private final DbAccess _dbAccess;

    public DbPrizePlaceholderDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    private static final String SELECT_COLUMNS = """
            SELECT id, label, count, event_kind, event_id, event_name, tier_index, created, created_by,
                   resolved_blueprint, resolved_on, resolved_by, notes
            FROM gemp_db.prize_placeholder
            """;

    @Override
    public DBDefs.PrizePlaceholder getPlaceholder(int id) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " WHERE id = :id;")
                        .addParameter("id", id)
                        .executeAndFetchFirst(DBDefs.PrizePlaceholder.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve prize placeholder " + id, ex);
        }
    }

    @Override
    public List<DBDefs.PrizePlaceholder> getUnresolved() {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " WHERE resolved_blueprint IS NULL ORDER BY created, id;")
                        .executeAndFetch(DBDefs.PrizePlaceholder.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve unresolved prize placeholders", ex);
        }
    }

    @Override
    public List<DBDefs.PrizePlaceholder> getResolved(int limit) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " WHERE resolved_blueprint IS NOT NULL ORDER BY resolved_on DESC, id DESC LIMIT :limit;")
                        .addParameter("limit", Math.max(0, limit))
                        .executeAndFetch(DBDefs.PrizePlaceholder.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve resolved prize placeholders", ex);
        }
    }

    @Override
    public DBDefs.PrizePlaceholder findPromise(String eventKind, String eventId, String label) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                String sql = SELECT_COLUMNS + """
                        WHERE event_kind = :event_kind
                          AND label = :label
                          AND ((:event_id IS NULL AND event_id IS NULL) OR event_id = :event_id)
                        ORDER BY (resolved_blueprint IS NULL) DESC, id DESC
                        LIMIT 1;
                        """;
                return conn.createQuery(sql)
                        .addParameter("event_kind", eventKind)
                        .addParameter("label", label)
                        .addParameter("event_id", eventId)
                        .executeAndFetchFirst(DBDefs.PrizePlaceholder.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to look up prize placeholder for " + eventKind + "/" + eventId + "/" + label, ex);
        }
    }

    @Override
    public int createPlaceholder(DBDefs.PrizePlaceholder placeholder) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                        INSERT INTO gemp_db.prize_placeholder
                            (label, count, event_kind, event_id, event_name, tier_index, created, created_by, notes)
                        VALUES (:label, :count, :event_kind, :event_id, :event_name, :tier_index, :created, :created_by, :notes);
                        """;
                Query query = conn.createQuery(sql, true);
                int id = query.addParameter("label", placeholder.label)
                        .addParameter("count", placeholder.count)
                        .addParameter("event_kind", placeholder.event_kind)
                        .addParameter("event_id", placeholder.event_id)
                        .addParameter("event_name", placeholder.event_name)
                        .addParameter("tier_index", placeholder.tier_index)
                        .addParameter("created", placeholder.created != null ? placeholder.created : DateUtils.Now().toLocalDateTime())
                        .addParameter("created_by", placeholder.created_by)
                        .addParameter("notes", placeholder.notes)
                        .executeUpdate().getKey(Integer.class);
                conn.commit();
                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create prize placeholder", ex);
        }
    }

    @Override
    public void markResolved(int id, String blueprintId, String resolvedBy) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                        UPDATE gemp_db.prize_placeholder
                        SET resolved_blueprint = :blueprint, resolved_on = :resolved_on, resolved_by = :resolved_by
                        WHERE id = :id;
                        """;
                conn.createQuery(sql)
                        .addParameter("blueprint", blueprintId)
                        .addParameter("resolved_on", DateUtils.Now().toLocalDateTime())
                        .addParameter("resolved_by", resolvedBy)
                        .addParameter("id", id)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to mark prize placeholder " + id + " resolved", ex);
        }
    }

    @Override
    public List<DBDefs.PrizePlaceholder> findByEvent(String eventKind, String eventId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " WHERE event_kind = :event_kind AND event_id = :event_id ORDER BY id;")
                        .addParameter("event_kind", eventKind)
                        .addParameter("event_id", eventId)
                        .executeAndFetch(DBDefs.PrizePlaceholder.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to list prize placeholders of " + eventKind + " " + eventId, ex);
        }
    }

    @Override
    public void deletePlaceholder(int id) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                conn.createQuery("DELETE FROM gemp_db.prize_placeholder WHERE id = :id AND resolved_blueprint IS NULL;")
                        .addParameter("id", id)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to delete prize placeholder " + id, ex);
        }
    }
}
