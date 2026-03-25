package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import org.sql2o.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbRtmdFeedbackDAO implements RtmdFeedbackDAO {
    private final DbAccess _dbAccess;

    public DbRtmdFeedbackDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addComparisonVote(String blueprintA, String blueprintB, String winner, String ipAddress) {
        // Normalize pair order: lexicographically smaller ID is always blueprint_a
        String a, b;
        if (blueprintA.compareTo(blueprintB) <= 0) {
            a = blueprintA;
            b = blueprintB;
        } else {
            a = blueprintB;
            b = blueprintA;
        }

        try {
            var db = _dbAccess.openDB();
            String sql = """
                INSERT INTO rtmd_comparison_vote (blueprint_a, blueprint_b, winner, ip_address)
                VALUES (:a, :b, :winner, :ip)
                """;
            try (var conn = db.beginTransaction()) {
                conn.createQuery(sql)
                        .addParameter("a", a)
                        .addParameter("b", b)
                        .addParameter("winner", winner)
                        .addParameter("ip", ipAddress)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert comparison vote", ex);
        }
    }

    @Override
    public Map<String, Integer> getPairVoteCounts() {
        try {
            var db = _dbAccess.openDB();
            String sql = """
                SELECT blueprint_a, blueprint_b, COUNT(*) as cnt
                FROM rtmd_comparison_vote
                GROUP BY blueprint_a, blueprint_b
                """;
            try (var conn = db.open()) {
                var rows = conn.createQuery(sql).executeAndFetchTable();
                Map<String, Integer> result = new HashMap<>();
                for (var row : rows.asList()) {
                    String key = row.get("blueprint_a") + "|" + row.get("blueprint_b");
                    result.put(key, ((Number) row.get("cnt")).intValue());
                }
                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve pair vote counts", ex);
        }
    }

    @Override
    public List<DBDefs.RtmdComparisonVote> getAllComparisonVotes() {
        try {
            var db = _dbAccess.openDB();
            String sql = "SELECT * FROM rtmd_comparison_vote ORDER BY voted_at ASC, id ASC";
            try (var conn = db.open()) {
                return conn.createQuery(sql).executeAndFetch(DBDefs.RtmdComparisonVote.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve comparison votes", ex);
        }
    }

    @Override
    public int addIdeaSubmission(String ideaText, String ipAddress) {
        try {
            var db = _dbAccess.openDB();
            String sql = """
                INSERT INTO rtmd_idea_submission (idea_text, ip_address)
                VALUES (:text, :ip)
                """;
            try (var conn = db.beginTransaction()) {
                int id = conn.createQuery(sql, true)
                        .addParameter("text", ideaText)
                        .addParameter("ip", ipAddress)
                        .executeUpdate()
                        .getKey(Integer.class);
                conn.commit();
                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert idea submission", ex);
        }
    }

    @Override
    public List<DBDefs.RtmdIdeaSubmission> getRandomSubmissions(int count) {
        try {
            var db = _dbAccess.openDB();
            String sql = "SELECT * FROM rtmd_idea_submission ORDER BY RAND() LIMIT :count";
            try (var conn = db.open()) {
                return conn.createQuery(sql)
                        .addParameter("count", count)
                        .executeAndFetch(DBDefs.RtmdIdeaSubmission.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve random submissions", ex);
        }
    }

    @Override
    public boolean voteOnIdea(int submissionId, String ipAddress, int vote) {
        try {
            var db = _dbAccess.openDB();
            String insertSql = """
                INSERT INTO rtmd_idea_vote (submission_id, ip_address, vote)
                VALUES (:id, :ip, :vote)
                """;
            String updateSql = """
                UPDATE rtmd_idea_submission SET
                  upvotes = (SELECT COUNT(*) FROM rtmd_idea_vote WHERE submission_id = :id AND vote = 1),
                  downvotes = (SELECT COUNT(*) FROM rtmd_idea_vote WHERE submission_id = :id AND vote = -1)
                WHERE id = :id
                """;
            try (var conn = db.beginTransaction()) {
                try {
                    conn.createQuery(insertSql)
                            .addParameter("id", submissionId)
                            .addParameter("ip", ipAddress)
                            .addParameter("vote", vote)
                            .executeUpdate();
                } catch (Exception e) {
                    // Duplicate key — this IP already voted on this submission
                    conn.rollback();
                    return false;
                }
                conn.createQuery(updateSql)
                        .addParameter("id", submissionId)
                        .executeUpdate();
                conn.commit();
                return true;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to vote on idea", ex);
        }
    }

    @Override
    public int getSubmissionCountForIp(String ipAddress) {
        try {
            var db = _dbAccess.openDB();
            String sql = "SELECT COUNT(*) as cnt FROM rtmd_idea_submission WHERE ip_address = :ip";
            try (var conn = db.open()) {
                var result = conn.createQuery(sql)
                        .addParameter("ip", ipAddress)
                        .executeAndFetchTable();
                return ((Number) result.asList().get(0).get("cnt")).intValue();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to count submissions for IP", ex);
        }
    }
}
