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
import java.util.Map;
import java.util.stream.Collectors;

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
                        INSERT INTO tournament (tournament_id, name, start_date, type, parameters, stage, round) 
                        VALUES (:tid, :name, :start, :type, :parameters, :stage, :round)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", dbinfo.tournament_id)
                        .addParameter("name", dbinfo.name)
                        .addParameter("start", dbinfo.start_date)
                        .addParameter("type", dbinfo.type)
                        .addParameter("parameters", dbinfo.parameters)
                        .addParameter("stage", dbinfo.stage)
                        .addParameter("round", dbinfo.round);

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
                        INSERT INTO gemp_db.scheduled_tournament (tournament_id, name, format, start_date, type, parameters, started)
                        VALUES(:tid, :name, :format, :start, :type, :parameters, :started);
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", dbinfo.tournament_id)
                        .addParameter("name", dbinfo.name)
                        .addParameter("format", dbinfo.format)
                        .addParameter("start", dbinfo.start_date)
                        .addParameter("type", dbinfo.type)
                        .addParameter("parameters", dbinfo.parameters)
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
                            tournament_id, name, start_date, type, parameters, stage, round
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
                            tournament_id, name, start_date, type, parameters, stage, round
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
                            tournament_id, name, start_date, type, parameters, stage, round
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
                            tournament_id, name, start_date, type, parameters, stage, round
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
    public List<DBDefs.Tournament> getFinishedTournamentsBetween(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            tournament_id, name, start_date, type, parameters, stage, round
                        FROM tournament
                        WHERE stage = :finished
                            AND start_date >= :from
                            AND start_date < :to
                        ORDER BY start_date DESC;
                        """;
                return conn.createQuery(sql)
                        .addParameter("from", from)
                        .addParameter("to", to)
                        .addParameter("finished", Tournament.Stage.FINISHED.name())
                        .executeAndFetch(DBDefs.Tournament.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve finished tournaments between " + from + " and " + to, ex);
        }
    }

    @Override
    public Map<String, Integer> getFinishedTournamentPlayerCountsBetween(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT tp.tournament_id AS eventId, COUNT(*) AS count
                        FROM tournament_player tp
                        JOIN tournament t ON t.tournament_id = tp.tournament_id
                        WHERE t.stage = :finished
                            AND t.start_date >= :from
                            AND t.start_date < :to
                        GROUP BY tp.tournament_id;
                        """;
                List<DbLeagueDAO.EventCount> counts = conn.createQuery(sql)
                        .addParameter("from", from)
                        .addParameter("to", to)
                        .addParameter("finished", Tournament.Stage.FINISHED.name())
                        .executeAndFetch(DbLeagueDAO.EventCount.class);

                return counts.stream().collect(Collectors.toMap(x -> x.eventId, x -> x.count));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve tournament player counts between " + from + " and " + to, ex);
        }
    }

    @Override
    public List<String> getFinishedTournamentMonths() {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                // round > 0 mirrors the long-standing rule that a finished tournament with no round played
                // (a solo draft against bots, say) is not a real event.
                String sql = """
                        SELECT DISTINCT DATE_FORMAT(start_date, '%Y-%m') AS ym
                        FROM tournament
                        WHERE stage = :finished
                            AND round > 0;
                        """;
                return conn.createQuery(sql)
                        .addParameter("finished", Tournament.Stage.FINISHED.name())
                        .executeScalarList(String.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve finished tournament months", ex);
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
                    SELECT id, tournament_id, name, format, start_date, type, parameters, started
                    FROM scheduled_tournament
                    WHERE started = 0
                        AND start_date <= :start
                        AND start_date >= :now;
                        """;
                List<DBDefs.ScheduledTournament> result = conn.createQuery(sql)
                        .addParameter("start", tillDate)
                        .addParameter("now", ZonedDateTime.now())
                        .executeAndFetch(DBDefs.ScheduledTournament.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve Unstarted Scheduled Tournament Queues", ex);
        }
    }

    @Override
    public List<DBDefs.ScheduledTournament> getScheduledTournamentsBetween(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT id, tournament_id, name, format, start_date, type, parameters, started
                    FROM scheduled_tournament
                    WHERE start_date >= :from
                        AND start_date <= :to
                    ORDER BY start_date;
                        """;
                return conn.createQuery(sql)
                        .addParameter("from", from)
                        .addParameter("to", to)
                        .executeAndFetch(DBDefs.ScheduledTournament.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve scheduled tournaments between " + from + " and " + to, ex);
        }
    }

    @Override
    public DBDefs.ScheduledTournament getScheduledTournament(String tournamentId) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            id, tournament_id, name, format, start_date, type, parameters, started
                        FROM scheduled_tournament
                        WHERE tournament_id = :tid;
                        """;
                List<DBDefs.ScheduledTournament> results = conn.createQuery(sql)
                        .addParameter("tid", tournamentId)
                        .executeAndFetch(DBDefs.ScheduledTournament.class);

                return results.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve scheduled tournament '" + tournamentId + "'", ex);
        }
    }

    @Override
    public int updateScheduledTournament(DBDefs.ScheduledTournament dbinfo) {
        try {
            var db = _dbAccess.openDB();

            // started = 0 in the WHERE: a queue that starts between the admin's check and this write keeps its row
            String sql = """
                        UPDATE scheduled_tournament
                        SET name = :name, format = :format, start_date = :start, type = :type, parameters = :parameters
                        WHERE tournament_id = :tid
                            AND started = 0;
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                int changed = conn.createQuery(sql)
                        .addParameter("tid", dbinfo.tournament_id)
                        .addParameter("name", dbinfo.name)
                        .addParameter("format", dbinfo.format)
                        .addParameter("start", dbinfo.start_date)
                        .addParameter("type", dbinfo.type)
                        .addParameter("parameters", dbinfo.parameters)
                        .executeUpdate()
                        .getResult();
                conn.commit();
                return changed;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update scheduled tournament '" + dbinfo.tournament_id + "'", ex);
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
