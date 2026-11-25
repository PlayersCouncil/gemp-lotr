package com.gempukku.lotro.db;

import java.util.List;
import java.util.Map;

public class DbPlayerArgonauthDAO implements PlayerArgonauthDAO {
    private static final String UPSERT = """
            INSERT INTO player_argonauth (player_id, argonauth_sub, proof)
            VALUES (:player_id, :sub, :proof)
            ON DUPLICATE KEY UPDATE
                linked_at = IF(argonauth_sub = VALUES(argonauth_sub), linked_at, CURRENT_TIMESTAMP),
                argonauth_sub = VALUES(argonauth_sub), proof = VALUES(proof)
            """;

    private final DbAccess _dbAccess;

    public DbPlayerArgonauthDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public List<Integer> getPlayerIds(String sub) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery("SELECT player_id FROM player_argonauth WHERE argonauth_sub = :sub ORDER BY player_id")
                        .addParameter("sub", sub)
                        .executeScalarList(Integer.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to read the accounts of a PC account", ex);
        }
    }

    @Override
    public String getSub(int playerId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery("SELECT argonauth_sub FROM player_argonauth WHERE player_id = :player_id")
                        .addParameter("player_id", playerId)
                        .executeScalar(String.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to read the PC account of player " + playerId, ex);
        }
    }

    @Override
    public void setLink(int playerId, String sub, String proof) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                conn.createQuery(UPSERT)
                        .addParameter("player_id", playerId)
                        .addParameter("sub", sub)
                        .addParameter("proof", proof)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to record the PC account of player " + playerId, ex);
        }
    }

    @Override
    public void removeLink(int playerId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                conn.createQuery("DELETE FROM player_argonauth WHERE player_id = :player_id")
                        .addParameter("player_id", playerId)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to remove the PC account of player " + playerId, ex);
        }
    }

    @Override
    public void replaceLinks(String sub, Map<Integer, String> proofByPlayerId) {
        try {
            var db = _dbAccess.openDB();
            // One transaction: a reader never sees the PC account with none of its accounts.
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                List<Integer> had = conn.createQuery("SELECT player_id FROM player_argonauth WHERE argonauth_sub = :sub")
                        .addParameter("sub", sub)
                        .executeScalarList(Integer.class);
                for (Integer playerId : had) {
                    if (!proofByPlayerId.containsKey(playerId))
                        conn.createQuery("DELETE FROM player_argonauth WHERE player_id = :player_id AND argonauth_sub = :sub")
                                .addParameter("player_id", playerId)
                                .addParameter("sub", sub)
                                .executeUpdate();
                }
                for (Map.Entry<Integer, String> link : proofByPlayerId.entrySet()) {
                    conn.createQuery(UPSERT)
                            .addParameter("player_id", link.getKey())
                            .addParameter("sub", sub)
                            .addParameter("proof", link.getValue())
                            .executeUpdate();
                }
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to record the accounts of a PC account", ex);
        }
    }
}
