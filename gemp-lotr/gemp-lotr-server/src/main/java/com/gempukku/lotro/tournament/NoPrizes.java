package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;

public class NoPrizes implements TournamentPrizes{
    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {
        return null;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {
        return null;
    }

    @Override
    public String getRegistryRepresentation() {
        return null;
    }

    @Override
    public String getPrizeDescription() {
        return "No prizes";
    }
}
