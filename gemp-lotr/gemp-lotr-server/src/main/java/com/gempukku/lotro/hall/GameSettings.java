package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.league.LeagueSerieInfo;

public record GameSettings(CollectionType collectionType, LotroFormat format, String tournamentId, League league, LeagueSerieInfo leagueSerie,
                           boolean competitive, boolean privateGame, boolean isInviteOnly, boolean hiddenGame,
                           GameTimer timeSettings, String userDescription, boolean isSolo
) {

}
