package com.gempukku.lotro.league;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;

import java.util.List;

public interface LeaguePrizes {
    public CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie);

    public CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie);

    public CardCollection getPrizeForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed);

    public CardCollection getTrophiesForLeague(PlayerStanding standing, List<PlayerStanding> allStandings, int maxGamesPlayed);
}
