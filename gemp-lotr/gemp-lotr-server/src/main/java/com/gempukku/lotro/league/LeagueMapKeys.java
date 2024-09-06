package com.gempukku.lotro.league;

import com.gempukku.lotro.db.vo.League;

public class LeagueMapKeys {
    public static String getLeagueMapKey(League league) {
        return String.valueOf(league.getCode());
    }

    public static String getLeagueSerieMapKey(League league, LeagueSerieInfo leagueSerie) {
        return league.getCode() + ":" + leagueSerie.getName();
    }
}
