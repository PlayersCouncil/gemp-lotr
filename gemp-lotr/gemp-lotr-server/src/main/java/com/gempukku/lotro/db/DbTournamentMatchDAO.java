package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.tournament.TournamentMatch;
import com.gempukku.lotro.tournament.TournamentMatchDAO;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbTournamentMatchDAO implements TournamentMatchDAO {
    private final DbAccess _dbAccess;

    public DbTournamentMatchDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public int addMatch(String tournamentId, int round, String playerOne, String playerTwo) {

        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO tournament_match (tournament_id, round, player_one, player_two)
                        VALUES (:tid, :round, :player1, :player2)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", tournamentId)
                        .addParameter("round", round)
                        .addParameter("player1", playerOne)
                        .addParameter("player2", playerTwo);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert tournament match", ex);
        }
    }

    @Override
    public int addBye(String tournamentId, String player, int round) {
        return addMatch(tournamentId, round, player, "bye");
    }

    @Override
    public void setMatchResult(String tournamentId, int round, String winner) {

        try {
            var db = _dbAccess.openDB();

            String sql = """
                        UPDATE tournament_match 
                        SET winner = :winner 
                        WHERE tournament_id = :tid 
                            AND round = :round
                            AND (player_one = :winner or player_two = :winner)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("tid", tournamentId)
                        .addParameter("winner", winner)
                        .addParameter("round", round)
                        .addToBatch();
                query.executeBatch();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update tournament match with winner", ex);
        }
    }

    @Override
    public List<DBDefs.TournamentMatch> getMatches(String tournamentId) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            id, tournament_id, round, player_one, player_two, winner
                        FROM tournament_match 
                        WHERE tournament_id = :tid
                            AND player_two <> 'bye'
                        """;

                return conn.createQuery(sql)
                        .addParameter("tid", tournamentId)
                        .executeAndFetch(DBDefs.TournamentMatch.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve tournament matches for tournament '" + tournamentId + "'.", ex);
        }
    }



    @Override
    public Map<String, Integer> getPlayerByes(String tournamentId) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            *
                        FROM tournament_match 
                        WHERE tournament_id = :tid
                             AND player_two = 'bye'
                        """;

                var matches =  conn.createQuery(sql)
                        .addParameter("tid", tournamentId)
                        .executeAndFetch(DBDefs.TournamentMatch.class);
                var result = new HashMap<String, Integer>();
                for(var match : matches) {
                    result.put(match.player_one, match.round);
                }

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve byes from tournament matches for tournament '" + tournamentId + "'.", ex);
        }
    }
}
