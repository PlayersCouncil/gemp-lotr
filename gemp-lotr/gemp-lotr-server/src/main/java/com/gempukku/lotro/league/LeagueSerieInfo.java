package com.gempukku.lotro.league;

import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroFormat;

import java.time.ZonedDateTime;

public interface LeagueSerieInfo {
    ZonedDateTime getStart();

    ZonedDateTime getEnd();

    int getMaxMatches();

    boolean isLimited();

    String getName();

    LotroFormat getFormat();

    CollectionType getCollectionType();

    CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);
}
