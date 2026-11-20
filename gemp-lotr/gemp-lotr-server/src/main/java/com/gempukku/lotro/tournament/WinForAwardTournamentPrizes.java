package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.packs.ProductLibrary;

public class WinForAwardTournamentPrizes implements TournamentPrizes {
    private final String _registryRepresentation;
    private final ProductLibrary _productLibrary;

    public WinForAwardTournamentPrizes(String registryRepresentation, ProductLibrary productLibrary) {
        _registryRepresentation = registryRepresentation;
        _productLibrary = productLibrary;
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {
        // If atleast 4 players, get one reward for each win or bye
        if (playersCount < 4) {
            return null;
        }

        DefaultCardCollection prize = new DefaultCardCollection();
        int hasBye = playerStanding.byeRound > 0 ? 1 : 0;
        int numberOfWins = playerStanding.playerWins + hasBye;
        prize.addItem("Placement Random Chase Card Selector", numberOfWins, true);
        // The winner also gets one tengwar card
        if (playerStanding.points == firstPlacePoints) {
            prize.addItem("(S)Tengwar", 1, true);
        }

        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {
        return null;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public PrizeSummary getPrizeSummary() {
        return PrizeSummary.of("Prize Breakdown",
                "4+ Players required for any prizes",
                "1 Event Award per win or bye",
                "1 Tengwar card for the tournament winner");
    }
}
