package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentDAO;
import com.gempukku.lotro.tournament.TournamentInfo;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DbTournamentDAO implements TournamentDAO {
    private final DbAccess _dbAccess;
    public DbTournamentDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addTournament(DBDefs.Tournament dbinfo) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO tournament (tournament_id, start_date, draft_type, name, format, 
                            collection, stage, pairing, round, manual_kickoff, prizes) 
                        VALUES (:tid, :start, :draft, :name, :format,
                            :collection, :stage, :pairing, :round, :kickoff, :prizes)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", dbinfo.tournament_id)
                        .addParameter("start", dbinfo.start_date)
                        .addParameter("draft", dbinfo.draft_type)
                        .addParameter("name", dbinfo.name)
                        .addParameter("format", dbinfo.format)
                        .addParameter("collection", dbinfo.collection)
                        .addParameter("stage", dbinfo.stage)
                        .addParameter("pairing", dbinfo.pairing)
                        .addParameter("round", dbinfo.round)
                        .addParameter("kickoff", dbinfo.manual_kickoff)
                        .addParameter("prizes", dbinfo.prizes);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert tournament", ex);
        }
    }

    @Override
    public void addScheduledTournament(DBDefs.ScheduledTournament dbinfo) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO gemp_db.scheduled_tournament (tournament_id, name, format, 
                            start_date, cost, playoff, tiebreaker, prizes, minimum_players, manual_kickoff, started)
                        VALUES(:tid, :name, :format, 
                            :start, :cost, :playoff, :tiebreaker, :prizes, :minimum_players, :manual_kickoff, :started);
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", dbinfo.tournament_id)
                        .addParameter("name", dbinfo.name)
                        .addParameter("format", dbinfo.format)
                        .addParameter("start", dbinfo.start_date)
                        .addParameter("cost", dbinfo.cost)
                        .addParameter("playoff", dbinfo.playoff)
                        .addParameter("tiebreaker", dbinfo.tiebreaker)
                        .addParameter("prizes", dbinfo.prizes)
                        .addParameter("minimum_players", dbinfo.minimum_players)
                        .addParameter("manual_kickoff", dbinfo.manual_kickoff)
                        .addParameter("started", dbinfo.started);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert scheduled tournament", ex);
        }
    }

    @Override
    public DBDefs.Tournament getTournamentById(String tournamentId) {

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            tournament_id, start_date, draft_type, name, format, 
                            collection, stage, pairing, round, manual_kickoff, prizes 
                        FROM tournament 
                        WHERE tournament_id = :id;
                        """;
                List<DBDefs.Tournament> result = conn.createQuery(sql)
                        .addParameter("id", tournamentId)
                        .executeAndFetch(DBDefs.Tournament.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve tournament with ID " + tournamentId, ex);
        }
    }

    @Override
    public List<DBDefs.Tournament> getUnfinishedTournaments() {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            tournament_id, start_date, draft_type, name, format, 
                            collection, stage, pairing, round, manual_kickoff, prizes 
                        FROM tournament 
                        WHERE stage <> :finished;
                        """;
                List<DBDefs.Tournament> results = conn.createQuery(sql)
                        .addParameter("finished", Tournament.Stage.FINISHED.name())
                        .executeAndFetch(DBDefs.Tournament.class);

                return results;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve unfinished tournaments", ex);
        }
    }

    @Override
    public DBDefs.Tournament getTournament(String tournamentId) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            tournament_id, start_date, draft_type, name, format, 
                            collection, stage, pairing, round, manual_kickoff, prizes 
                        FROM tournament 
                        WHERE tournament_id = :tid;
                        """;
                List<DBDefs.Tournament> results = conn.createQuery(sql)
                        .addParameter("tid", tournamentId)
                        .executeAndFetch(DBDefs.Tournament.class);

                return results.getFirst();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve tournament '" + tournamentId + "'", ex);
        }
    }

    @Override
    public List<DBDefs.Tournament> getFinishedTournamentsSince(ZonedDateTime time) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            tournament_id, start_date, draft_type, name, format, 
                            collection, stage, pairing, round, manual_kickoff, prizes 
                        FROM tournament 
                        WHERE stage = :finished 
                            AND start_date > :start;
                        """;
                List<DBDefs.Tournament> results = conn.createQuery(sql)
                        .addParameter("start", time)
                        .addParameter("finished", Tournament.Stage.FINISHED.name())
                        .executeAndFetch(DBDefs.Tournament.class);

                return results;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve finished tournaments", ex);
        }
    }

    @Override
    public void updateTournamentStage(String tournamentId, Tournament.Stage stage) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("update tournament set stage=? where tournament_id=?")) {
                    statement.setString(1, stage.name());
                    statement.setString(2, tournamentId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void updateTournamentRound(String tournamentId, int round) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("update tournament set round=? where tournament_id=?")) {
                    statement.setInt(1, round);
                    statement.setString(2, tournamentId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public List<DBDefs.ScheduledTournament> getUnstartedScheduledTournamentQueues(ZonedDateTime tillDate) {
        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT id, tournament_id, name, format, start_date, cost, playoff,
                        tiebreaker, prizes, minimum_players, manual_kickoff, started
                    FROM scheduled_tournament
                    WHERE started = 0
                        AND start_date <= :start;
                        """;
                List<DBDefs.ScheduledTournament> result = conn.createQuery(sql)
                        .addParameter("start", tillDate)
                        .executeAndFetch(DBDefs.ScheduledTournament.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve Unstarted Scheduled Tournament Queues", ex);
        }
    }

    @Override
    public void updateScheduledTournamentStarted(String scheduledTournamentId) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        UPDATE scheduled_tournament 
                        SET started = 1 
                        WHERE tournament_id = :id;
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql);
                query.addParameter("id", scheduledTournamentId);
                query.executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update ScheduledTournament started status", ex);
        }
    }
}
