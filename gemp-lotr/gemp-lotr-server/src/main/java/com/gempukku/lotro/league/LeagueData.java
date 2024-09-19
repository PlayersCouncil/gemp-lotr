package com.gempukku.lotro.league;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;

import java.time.ZonedDateTime;
import java.util.List;

public interface LeagueData {
    boolean isSoloDraftLeague();

    List<LeagueSerieInfo> getSeries();

    SoloDraft getSoloDraft();

    CardCollection createLeagueCollection(CollectionsManager collectionsManager, Player player, ZonedDateTime currentTime);

    int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, ZonedDateTime currentTime);

    default int getMaxRepeatMatchesPerSerie() {
        return 1;
    }

    LeagueParams getParameters();
}
