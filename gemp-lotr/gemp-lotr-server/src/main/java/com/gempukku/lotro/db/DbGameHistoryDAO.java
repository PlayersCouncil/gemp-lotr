package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.game.Player;
import org.sql2o.Query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DbGameHistoryDAO implements GameHistoryDAO {
    private final DbAccess _dbAccess;

    public DbGameHistoryDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason, String winRecordingId, String loseRecordingId,
                               String formatName, String tournament, String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate, int replayVersion) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO game_history (winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version)
                        VALUES (:winner, :winnerId, :loser, :loserId, :win_reason, :lose_reason, :win_recording_id, :lose_recording_id, 
                            :format_name, :tournament, :winner_deck_name, :loser_deck_name, :start_date, :end_date, :version)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("winner", winner)
                        .addParameter("winnerId", winnerId)
                        .addParameter("loser", loser)
                        .addParameter("loserId", loserId)
                        .addParameter("win_reason", winReason)
                        .addParameter("lose_reason", loseReason)
                        .addParameter("win_recording_id", winRecordingId)
                        .addParameter("lose_recording_id", loseRecordingId)
                        .addParameter("format_name", formatName)
                        .addParameter("tournament", tournament)
                        .addParameter("winner_deck_name", winnerDeckName)
                        .addParameter("loser_deck_name", loserDeckName)
                        .addParameter("start_date", DateUtils.FormatDateTime(startDate))
                        .addParameter("end_date", DateUtils.FormatDateTime(endDate))
                        .addParameter("version", replayVersion);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert game history", ex);
        }
    }

    public List<DBDefs.GameHistory> getGameHistoryForPlayer(Player player, int start, int count) {

        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version
                    FROM game_history 
                    WHERE winner = :playerName 
                        OR loser = :playerName 
                    ORDER BY end_date DESC
                    LIMIT :start, :count;
                """;
                List<DBDefs.GameHistory> result = conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("start", start)
                        .addParameter("count", count)
                        .executeAndFetch(DBDefs.GameHistory.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history for player", ex);
        }

    }

    public DBDefs.GameHistory getGameHistory(String recordID) {
        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version 
                    FROM game_history 
                    WHERE win_recording_id = :recordID 
                        OR lose_recording_id = :recordID;
                """;
                List<DBDefs.GameHistory> result = conn.createQuery(sql)
                        .addParameter("recordID", recordID)
                        .executeAndFetch(DBDefs.GameHistory.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history for player", ex);
        }

    }

    public boolean doesReplayIDExist(String id) {
        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*)
                        FROM game_history
                        WHERE win_recording_id = :id
                            OR lose_recording_id = :id
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("id", id)
                        .executeScalar(Integer.class);

                return result >= 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve existence of replay ID", ex);
        }
    }

    @Override
    public List<DBDefs.GameHistory> getGamesForTournament(String tournamentName)  {
        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version
                        FROM game_history
                        WHERE tournament = :name
                        ORDER BY end_date DESC
                        """;

                return conn.createQuery(sql)
                        .addParameter("name", tournamentName)
                        .executeAndFetch(DBDefs.GameHistory.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history for tournament", ex);
        }
    }

    public int getGameHistoryForPlayerCount(Player player) {
        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*)
                        FROM game_history
                        WHERE winner = :player
                            OR loser = :player
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("player", player.getName())
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of player games", ex);
        }
    }

    public int getActivePlayersCount(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*) 
                        FROM 
                        (
                            SELECT winner 
                            FROM game_history 
                            WHERE end_date BETWEEN :from AND :to
                             
                            UNION 
                            
                            SELECT loser 
                            FROM game_history 
                            WHERE end_date BETWEEN :from AND :to
                        ) AS U
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("from", from.format(DateUtils.DateFormat))
                        .addParameter("to", to.format(DateUtils.DateFormat))
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of active players", ex);
        }
    }

    public int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*) 
                        FROM game_history 
                        WHERE end_date BETWEEN :from AND :to;
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("from", from.format(DateUtils.DateFormat))
                        .addParameter("to", to.format(DateUtils.DateFormat))
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of games played", ex);
        }
    }

    @Override
    public int getBotGamesPlayedCount(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                // bot accounts are named ~something (DbPlayerDAO.registerBot); players' names cannot contain ~
                String sql = """
                        SELECT COUNT(*)
                        FROM game_history
                        WHERE end_date BETWEEN :from AND :to
                            AND (winner LIKE '~%' OR loser LIKE '~%');
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("from", from.format(DateUtils.DateFormat))
                        .addParameter("to", to.format(DateUtils.DateFormat))
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of bot games played", ex);
        }
    }

    // Excludes bot games (bot accounts start with '~'); Server Stats reports them separately (getBotGamesPlayedCount).
    public List<DBDefs.FormatStats> GetAllGameFormatData(ZonedDateTime from, ZonedDateTime to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                        	 COUNT(*) AS Count
                        	,format_name AS Format
                        	,CASE WHEN tournament IS NULL OR tournament LIKE 'Casual %' THEN 1 ELSE 0 END AS Casual
                        FROM game_history
                        WHERE end_date BETWEEN :from AND :to
                          AND winner NOT LIKE '~%' AND loser NOT LIKE '~%'
                        GROUP BY format_name, CASE WHEN tournament IS NULL OR tournament LIKE 'Casual %' THEN 1 ELSE 0 END
                        """;
                List<DBDefs.FormatStats> result = conn.createQuery(sql)
                        .addParameter("from", from.format(DateUtils.DateFormat))
                        .addParameter("to", to.format(DateUtils.DateFormat))
                        .executeAndFetch(DBDefs.FormatStats.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve format stats", ex);
        }
    }


    public List<PlayerStatistic> getCasualPlayerStatistics(Player player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select deck_name, format_name, sum(win), sum(lose) from" +
                                " (select winner_deck_name as deck_name, format_name, 1 as win, 0 as lose from game_history where winner=? and (tournament is null or tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')" +
                                " union all select loser_deck_name as deck_name, format_name, 0 as win, 1 as lose from game_history where loser=? and (tournament is null or tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')) as u" +
                                " group by deck_name, format_name order by format_name, deck_name")) {
                    statement.setString(1, player.getName());
                    statement.setString(2, player.getName());
                    List<PlayerStatistic> result = new LinkedList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next())
                            result.add(new PlayerStatistic(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
                    }
                    return result;
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of games played", exp);
        }
    }

    @Override
    public List<DBDefs.GameHistory> getLastGames(String requestedFormatName, int count) {

        try {

            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version
                        FROM game_history
                        WHERE format_name = :formatName 
                        ORDER BY end_date DESC
                        LIMIT :count
                    """;
                List<DBDefs.GameHistory> result = conn.createQuery(sql)
                        .addParameter("formatName", requestedFormatName)
                        .addParameter("count", count)
                        .executeAndFetch(DBDefs.GameHistory.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve last games", ex);
        }
    }

    public List<PlayerStatistic> getCompetitivePlayerStatistics(Player player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select deck_name, format_name, sum(win), sum(lose) from" +
                                " (select winner_deck_name as deck_name, format_name, 1 as win, 0 as lose from game_history where winner=? and (tournament is not null and not tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')" +
                                " union all select loser_deck_name as deck_name, format_name, 0 as win, 1 as lose from game_history where loser=? and (tournament is not null and not tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')) as u" +
                                " group by deck_name, format_name order by format_name, deck_name")) {
                    statement.setString(1, player.getName());
                    statement.setString(2, player.getName());
                    List<PlayerStatistic> result = new LinkedList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next())
                            result.add(new PlayerStatistic(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
                    }
                    return result;
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of games played", exp);
        }
    }

    // ---- tabs-account: filtered, paged game history (SQL built by GameHistoryQuery, values bound as parameters) ----

    @Override
    public List<DBDefs.GameHistory> getGameHistoryForPlayer(Player player, GameHistoryFilter filter, int start, int count) {
        GameHistoryQuery query = GameHistoryQuery.forPlayer(player.getName(), filter);
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                Query q = conn.createQuery(query.selectSql());
                bind(q, query.parameters());
                return q.addParameter("start", start)
                        .addParameter("count", count)
                        .executeAndFetch(DBDefs.GameHistory.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve filtered game history for player", ex);
        }
    }

    @Override
    public int getGameHistoryForPlayerCount(Player player, GameHistoryFilter filter) {
        GameHistoryQuery query = GameHistoryQuery.forPlayer(player.getName(), filter);
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                Query q = conn.createQuery(query.countSql());
                bind(q, query.parameters());
                Integer result = q.executeScalar(Integer.class);
                return Objects.requireNonNullElse(result, 0);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to count filtered game history for player", ex);
        }
    }

    @Override
    public List<String> getGameHistoryFormats(Player player) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(GameHistoryQuery.formatsSql())
                        .addParameter("me", player.getName())
                        .executeScalarList(String.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history formats for player", ex);
        }
    }

    @Override
    public List<String> getGameHistoryEvents(Player player, int limit) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(GameHistoryQuery.eventsSql())
                        .addParameter("me", player.getName())
                        .addParameter("limit", limit)
                        .executeScalarList(String.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history events for player", ex);
        }
    }

    private static void bind(Query query, java.util.Map<String, Object> parameters) {
        for (var entry : parameters.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String str)
                query.addParameter(entry.getKey(), str);
            else if (value instanceof Integer i)
                query.addParameter(entry.getKey(), i);
            else
                query.addParameter(entry.getKey(), value);
        }
    }
}
