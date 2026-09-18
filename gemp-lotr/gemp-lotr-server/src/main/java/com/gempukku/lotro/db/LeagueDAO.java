package com.gempukku.lotro.db;

import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.league.LeagueParams;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

public interface LeagueDAO {
    /**
     * @param scheduleId the league_schedule row this league was created by, or null when an admin created it directly
     */
    int addLeague(String name, long code, League.LeagueType type, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost, Integer scheduleId);

    List<League> loadActiveLeagues(ZonedDateTime currentTime) throws SQLException;

    League loadLeagueByCode(long code);

    /**
     * Rewrites the editable columns of an existing league row.  The code is the key and is never changed.
     */
    void updateLeague(long code, String name, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost);

    boolean setStatus(League league, int newStatus);
}
