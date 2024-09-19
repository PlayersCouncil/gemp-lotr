package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.league.LeagueParams;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DbLeagueDAO implements LeagueDAO {
    private final DbAccess _dbAccess;

    public DbLeagueDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public int addLeague(String name, long code, League.LeagueType type, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost) {
        try {
            var db = _dbAccess.openDB();

            String sql = """
                        INSERT INTO gemp_db.league
                            (name, code, `type`, parameters, start_date, end_date, status, cost)
                        VALUES(:name, :code, :type, :parameters, :start, :end, :status, :cost);
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
                    .addParameter("cost", cost);

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
