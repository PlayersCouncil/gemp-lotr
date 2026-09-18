package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import org.sql2o.Query;

import java.util.List;

public class DbLeagueScheduleDAO implements LeagueScheduleDAO {
    private final DbAccess _dbAccess;

    public DbLeagueScheduleDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    private static final String SELECT_COLUMNS = """
            SELECT id, name, league_type, template, events, name_pattern, next_event_date, next_event_index,
                   interval_months, lead_days, active, last_created_league_id, last_run, last_error
            FROM gemp_db.league_schedule
            """;

    @Override
    public List<DBDefs.LeagueSchedule> getAllSchedules() {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " ORDER BY name;")
                        .executeAndFetch(DBDefs.LeagueSchedule.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league schedules", ex);
        }
    }

    @Override
    public DBDefs.LeagueSchedule getSchedule(int id) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.open()) {
                return conn.createQuery(SELECT_COLUMNS + " WHERE id = :id;")
                        .addParameter("id", id)
                        .executeAndFetchFirst(DBDefs.LeagueSchedule.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve league schedule " + id, ex);
        }
    }

    @Override
    public int saveSchedule(DBDefs.LeagueSchedule schedule) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                int id;
                if (schedule.id == 0) {
                    String sql = """
                            INSERT INTO gemp_db.league_schedule
                                (name, league_type, template, events, name_pattern, next_event_date, next_event_index,
                                 interval_months, lead_days, active)
                            VALUES (:name, :league_type, :template, :events, :name_pattern, :next_event_date, :next_event_index,
                                    :interval_months, :lead_days, :active);
                            """;
                    Query query = conn.createQuery(sql, true);
                    bindDefinition(query, schedule);
                    id = query.executeUpdate().getKey(Integer.class);
                } else {
                    String sql = """
                            UPDATE gemp_db.league_schedule
                            SET name = :name, league_type = :league_type, template = :template, events = :events,
                                name_pattern = :name_pattern, next_event_date = :next_event_date,
                                next_event_index = :next_event_index, interval_months = :interval_months,
                                lead_days = :lead_days, active = :active, last_error = NULL
                            WHERE id = :id;
                            """;
                    Query query = conn.createQuery(sql);
                    bindDefinition(query, schedule);
                    query.addParameter("id", schedule.id).executeUpdate();
                    id = schedule.id;
                }
                conn.commit();
                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to save league schedule", ex);
        }
    }

    private static void bindDefinition(Query query, DBDefs.LeagueSchedule schedule) {
        query.addParameter("name", schedule.name)
                .addParameter("league_type", schedule.league_type)
                .addParameter("template", schedule.template)
                .addParameter("events", schedule.events)
                .addParameter("name_pattern", schedule.name_pattern)
                .addParameter("next_event_date", schedule.next_event_date)
                .addParameter("next_event_index", schedule.next_event_index)
                .addParameter("interval_months", schedule.interval_months)
                .addParameter("lead_days", schedule.lead_days)
                .addParameter("active", schedule.active);
    }

    @Override
    public void updateProgress(DBDefs.LeagueSchedule schedule) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                        UPDATE gemp_db.league_schedule
                        SET next_event_date = :next_event_date, next_event_index = :next_event_index,
                            last_created_league_id = :last_created_league_id, last_run = :last_run,
                            last_error = :last_error
                        WHERE id = :id;
                        """;
                conn.createQuery(sql)
                        .addParameter("next_event_date", schedule.next_event_date)
                        .addParameter("next_event_index", schedule.next_event_index)
                        .addParameter("last_created_league_id", schedule.last_created_league_id)
                        .addParameter("last_run", schedule.last_run)
                        .addParameter("last_error", schedule.last_error)
                        .addParameter("id", schedule.id)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update league schedule progress", ex);
        }
    }

    @Override
    public void deleteSchedule(int id) {
        try {
            var db = _dbAccess.openDB();
            try (org.sql2o.Connection conn = db.beginTransaction()) {
                // leagues the schedule already created stay, but no longer point at a schedule that does not exist
                conn.createQuery("UPDATE gemp_db.league SET schedule_id = NULL WHERE schedule_id = :id;")
                        .addParameter("id", id)
                        .executeUpdate();
                conn.createQuery("DELETE FROM gemp_db.league_schedule WHERE id = :id;")
                        .addParameter("id", id)
                        .executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to delete league schedule " + id, ex);
        }
    }
}
