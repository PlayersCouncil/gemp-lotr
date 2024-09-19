package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;

public interface TournamentPrizes {
    CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount);
    CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount);
    String getRegistryRepresentation();
    String getPrizeDescription();
}
