package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.league.LeagueParams;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbLeagueDAO implements LeagueDAO {
    private final DbAccess _dbAccess;

    public DbLeagueDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public int addLeague(String name, long code, League.LeagueType type, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost, Integer scheduleId) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO gemp_db.league
                            (name, code, `type`, parameters, start_date, end_date, status, cost, schedule_id)
                        VALUES(:name, :code, :type, :parameters, :start, :end, :status, :cost, :scheduleId);
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("name", name)
                    .addParameter("code", code)
                    .addParameter("type", type.toString())
                    .addParameter("parameters", parameters.toString())
                    .addParameter("start", start.format(DateUtils.DateFormat))
                    .addParameter("end", end.format(DateUtils.DateFormat))
                    .addParameter("status", 0)
                    .addParameter("cost", cost)
                    .addParameter("scheduleId", scheduleId);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert league", ex);
        }
    }

    public List<League> loadActiveLeagues(ZonedDateTime after)  {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                             id
                            ,name
                            ,code
                            ,`type`
                            ,parameters
                            ,start_date
                            ,end_date
                            ,status
                            ,cost
                            ,schedule_id
                        FROM gemp_db.league
                        WHERE end_date >= :after
                        ORDER BY start_date DESC;        
                        """;
                List<DBDefs.League> result = conn.createQuery(sql)
                        .addParameter("after", after)
                        .executeAndFetch(DBDefs.League.class);

                return result.stream().map(League::new).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league entries", ex);
        }
    }


    public List<DBDefs.League> loadLeaguesEndingBetween(LocalDate from, LocalDate to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                             id
                            ,name
                            ,code
                            ,`type`
                            ,parameters
                            ,start_date
                            ,end_date
                            ,status
                            ,cost
                            ,schedule_id
                        FROM gemp_db.league
                        WHERE end_date >= :from
                            AND end_date < :to
                        ORDER BY end_date DESC;
                        """;
                return conn.createQuery(sql)
                        .addParameter("from", from)
                        .addParameter("to", to)
                        .executeAndFetch(DBDefs.League.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve leagues ending between " + from + " and " + to, ex);
        }
    }

    public Map<String, Integer> getParticipantCountsForLeaguesEndingBetween(LocalDate from, LocalDate to) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                // league_participation.league_type stores the league code as a string, so it is compared against
                // the CAST of league.code rather than to the bigint itself.
                String sql = """
                        SELECT lp.league_type AS eventId, COUNT(*) AS count
                        FROM gemp_db.league_participation lp
                        JOIN gemp_db.league l ON lp.league_type = CAST(l.code AS CHAR)
                        WHERE l.end_date >= :from
                            AND l.end_date < :to
                        GROUP BY lp.league_type;
                        """;
                List<EventCount> counts = conn.createQuery(sql)
                        .addParameter("from", from)
                        .addParameter("to", to)
                        .executeAndFetch(EventCount.class);

                return counts.stream().collect(Collectors.toMap(x -> x.eventId, x -> x.count));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league participant counts between " + from + " and " + to, ex);
        }
    }

    public List<String> getLeagueEndMonths(LocalDate before) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT DISTINCT DATE_FORMAT(end_date, '%Y-%m') AS ym
                        FROM gemp_db.league
                        WHERE end_date < :before;
                        """;
                return conn.createQuery(sql)
                        .addParameter("before", before)
                        .executeScalarList(String.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league end months", ex);
        }
    }

    /**
     * One row of a "count per event" aggregate; sql2o needs a public type with public fields to map into.
     */
    public static class EventCount {
        public String eventId;
        public int count;
    }

    public League loadLeagueByCode(long code) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                             id
                            ,name
                            ,code
                            ,`type`
                            ,parameters
                            ,start_date
                            ,end_date
                            ,status
                            ,cost
                            ,schedule_id
                        FROM gemp_db.league
                        WHERE code = :code;
                        """;
                List<DBDefs.League> result = conn.createQuery(sql)
                        .addParameter("code", code)
                        .executeAndFetch(DBDefs.League.class);

                if (result.isEmpty())
                    return null;

                return new League(result.getFirst());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league by code", ex);
        }
    }

    public void updateLeague(long code, String name, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                UPDATE gemp_db.league
                                SET name = :name
                                    ,parameters = :parameters
                                    ,start_date = :start
                                    ,end_date = :end
                                    ,cost = :cost
                                WHERE code = :code
                            """;
                conn.createQuery(sql)
                        .addParameter("name", name)
                        .addParameter("parameters", parameters.toString())
                        .addParameter("start", start.format(DateUtils.DateFormat))
                        .addParameter("end", end.format(DateUtils.DateFormat))
                        .addParameter("cost", cost)
                        .addParameter("code", code)
                        .executeUpdate();

                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update league", ex);
        }
    }

    public boolean setStatus(League league, int newStatus)  {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                UPDATE league
                                SET status = :newStatus
                                WHERE code = :code
                            """;
                conn.createQuery(sql)
                        .addParameter("newStatus", newStatus)
                        .addParameter("code", league.getCode())
                        .executeUpdate();

                conn.commit();

                return conn.getResult() == 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update league status", ex);
        }
    }


}
