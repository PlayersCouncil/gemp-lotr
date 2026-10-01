package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.league.LeagueParams;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface LeagueDAO {
    /**
     * @param scheduleId the league_schedule row this league was created by, or null when an admin created it directly
     */
    int addLeague(String name, long code, League.LeagueType type, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost, Integer scheduleId);

    List<League> loadActiveLeagues(ZonedDateTime currentTime) throws SQLException;

    League loadLeagueByCode(long code);

    /**
     * The raw rows of every league whose {@code end_date} falls in the half-open range [from, to), newest first.
     * Rows rather than {@link League} objects because the league VO carries no dates and no raw parameters, and
     * because the completed-event browser must not pay for parsing a league's parameters it is not going to show.
     */
    List<DBDefs.League> loadLeaguesEndingBetween(LocalDate from, LocalDate to);

    /**
     * How many players joined each league whose {@code end_date} falls in [from, to), keyed by the league code as
     * a string.  One grouped query for the whole month rather than one query per league.
     */
    Map<String, Integer> getParticipantCountsForLeaguesEndingBetween(LocalDate from, LocalDate to);

    /**
     * Every {@code yyyy-MM} in which at least one league ended strictly before {@code before}, unordered.
     */
    List<String> getLeagueEndMonths(LocalDate before);

    /**
     * Rewrites the editable columns of an existing league row.  The code is the key and is never changed.
     */
    void updateLeague(long code, String name, LeagueParams parameters, ZonedDateTime start, ZonedDateTime end, int cost);

    boolean setStatus(League league, int newStatus);
}
