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
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount) {
        // If atleast 4 players, get one reward for each win or bye
        if (playersCount < 4) {
            return null;
        }

        DefaultCardCollection prize = new DefaultCardCollection();
        int hasBye = playerStanding.byeRound > 0 ? 1 : 0;
        int numberOfWins = playerStanding.playerWins + hasBye;
        prize.addItem("Placement Random Chase Card Selector", numberOfWins, true);

        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount) {
        return null;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPrizeDescription() {
        return """
            <div class='prizeHint' value='If 4+ players, get one Event Award for each win or bye'>Prize Breakdown</div>""";
    }
}
