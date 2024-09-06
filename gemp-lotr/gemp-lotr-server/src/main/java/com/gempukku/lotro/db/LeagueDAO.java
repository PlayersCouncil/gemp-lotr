package com.gempukku.lotro.db;

import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.league.LeagueParams;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

public interface LeagueDAO {
    int addLeague(String name, long code, League.LeagueType type, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost);

    List<League> loadActiveLeagues(ZonedDateTime currentTime) throws SQLException;

    boolean setStatus(League league, int newStatus);
}
