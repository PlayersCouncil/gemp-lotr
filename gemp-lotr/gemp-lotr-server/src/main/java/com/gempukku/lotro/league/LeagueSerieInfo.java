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

    /** Null for a finished league whose format has since been retired from the format library. */
    LotroFormat getFormat();

    /**
     * The serie's format code as the league was defined with it.  Unlike {@code getFormat().getCode()} this is still
     * available when the format has been retired; null only when the definition never had one.
     */
    default String getFormatCode() {
        LotroFormat format = getFormat();
        return format == null ? null : format.getCode();
    }

    CollectionType getCollectionType();

    CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);
}
