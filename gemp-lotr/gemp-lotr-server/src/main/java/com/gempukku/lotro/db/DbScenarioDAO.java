package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;

import java.time.ZonedDateTime;
import java.util.List;

public class DbScenarioDAO implements ScenarioDAO {
    private final DbAccess _dbAccess;

    public DbScenarioDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    /** UTC, as text, the way the other tables' dates are written. */
    private static String now() {
        return DateUtils.FormatDateTime(DateUtils.Now());
    }

    @Override
    public int startRun(int playerId, String scenarioId, int version, Integer resumedAt, ZonedDateTime started) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String now = DateUtils.FormatDateTime(started);
                int id = conn.createQuery("""
                                INSERT INTO gemp_db.scenario_run (player_id, scenario_id, version, started, resumed_at)
                                VALUES (:player_id, :scenario_id, :version, :started, :resumed_at);
                                """, true)
                        .addParameter("player_id", playerId)
                        .addParameter("scenario_id", scenarioId)
                        .addParameter("version", version)
                        .addParameter("started", now)
                        .addParameter("resumed_at", resumedAt)
                        .executeUpdate().getKey(Integer.class);

                //A scenario that has been completed stays completed, at the version it was completed in
                conn.createQuery("""
                                INSERT INTO gemp_db.scenario_progress (player_id, scenario_id, version, status, attempts, last_played)
                                VALUES (:player_id, :scenario_id, :version, 'started', 1, :now)
                                ON DUPLICATE KEY UPDATE
                                    attempts = attempts + 1,
                                    last_played = :now,
                                    version = IF(status = 'complete', version, :version);
                                """)
                        .addParameter("player_id", playerId)
                        .addParameter("scenario_id", scenarioId)
                        .addParameter("version", version)
                        .addParameter("now", now)
                        .executeUpdate();
                conn.commit();
                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to record the start of scenario " + scenarioId, ex);
        }
    }

    @Override
    public void finishRun(DBDefs.ScenarioRun run) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String now = now();
                conn.createQuery("""
                                UPDATE gemp_db.scenario_run
                                SET ended = :ended, result = :result, last_step = :last_step, step_report = :step_report,
                                    problems = :problems, recording_id = :recording_id, journal = :journal,
                                    picks = :picks, score = :score, unlisted = :unlisted
                                WHERE id = :id;
                                """)
                        .addParameter("picks", run.picks)
                        .addParameter("score", run.score)
                        .addParameter("unlisted", run.unlisted)
                        .addParameter("ended", now)
                        .addParameter("result", run.result)
                        .addParameter("last_step", run.last_step)
                        .addParameter("step_report", run.step_report)
                        .addParameter("problems", run.problems)
                        .addParameter("recording_id", run.recording_id)
                        .addParameter("journal", run.journal)
                        .addParameter("id", run.id)
                        .executeUpdate();

                if ("complete".equals(run.result)) {
                    conn.createQuery("""
                                    UPDATE gemp_db.scenario_progress p
                                    JOIN gemp_db.scenario_run r ON r.player_id = p.player_id AND r.scenario_id = p.scenario_id
                                    SET p.status = 'complete',
                                        p.version = r.version,
                                        p.first_completed = COALESCE(p.first_completed, :now)
                                    WHERE r.id = :id;
                                    """)
                            .addParameter("now", now)
                            .addParameter("id", run.id)
                            .executeUpdate();
                }
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to record the end of scenario run " + run.id, ex);
        }
    }

    @Override
    public DBDefs.ScenarioRun getRunByRecording(String recordingId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery("""
                                SELECT id, player_id, scenario_id, version, started, ended, result, resumed_at, last_step,
                                       step_report, problems, recording_id
                                FROM gemp_db.scenario_run
                                WHERE recording_id = :recording_id;
                                """)
                        .addParameter("recording_id", recordingId)
                        .executeAndFetchFirst(DBDefs.ScenarioRun.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to look up the scenario run with recording " + recordingId, ex);
        }
    }

    @Override
    public void recordBestScore(int runId, int score, boolean higherIsBetter) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                conn.createQuery("""
                                UPDATE gemp_db.scenario_progress p
                                JOIN gemp_db.scenario_run r ON r.player_id = p.player_id AND r.scenario_id = p.scenario_id
                                SET p.best_score = :score
                                WHERE r.id = :id AND (p.best_score IS NULL OR (CASE WHEN :higher THEN :score > p.best_score ELSE :score < p.best_score END));
                                """)
                        .addParameter("score", score)
                        .addParameter("higher", higherIsBetter)
                        .addParameter("id", runId)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to record the best score of scenario run " + runId, ex);
        }
    }

    @Override
    public List<DBDefs.ScenarioRun> getUnlistedRuns(String scenarioId, int limit) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery("""
                                SELECT r.id, r.player_id, r.scenario_id, r.version, r.started, r.ended, r.result, r.resumed_at, r.last_step,
                                       r.recording_id, r.picks, r.score, r.unlisted, p.name AS player_name
                                FROM gemp_db.scenario_run r
                                JOIN gemp_db.player p ON p.id = r.player_id
                                WHERE r.scenario_id = :scenario_id AND r.unlisted = 1
                                ORDER BY r.id DESC
                                LIMIT :limit;
                                """)
                        .addParameter("scenario_id", scenarioId)
                        .addParameter("limit", limit)
                        .executeAndFetch(DBDefs.ScenarioRun.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to list the unlisted solutions of scenario " + scenarioId, ex);
        }
    }

    @Override
    public List<DBDefs.ScenarioProgress> getProgress(int playerId) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery("""
                                SELECT player_id, scenario_id, version, status, attempts, best_score, first_completed, last_played
                                FROM gemp_db.scenario_progress
                                WHERE player_id = :player_id;
                                """)
                        .addParameter("player_id", playerId)
                        .executeAndFetch(DBDefs.ScenarioProgress.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve scenario progress for player " + playerId, ex);
        }
    }
}
