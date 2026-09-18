package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;

public interface LeagueScheduleDAO {
    List<DBDefs.LeagueSchedule> getAllSchedules();

    DBDefs.LeagueSchedule getSchedule(int id);

    /**
     * Inserts the schedule (when id is 0) or updates every definition column of an existing one.
     * @return the id of the row
     */
    int saveSchedule(DBDefs.LeagueSchedule schedule);

    /**
     * Records the outcome of a scheduler run: the advanced next event, what was created and any error.
     */
    void updateProgress(DBDefs.LeagueSchedule schedule);

    void deleteSchedule(int id);
}
