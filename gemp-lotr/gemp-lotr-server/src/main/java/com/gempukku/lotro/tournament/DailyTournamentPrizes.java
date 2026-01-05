package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.packs.ProductLibrary;


public class DailyTournamentPrizes implements TournamentPrizes {
    private final String _registryRepresentation;
    private final ProductLibrary _productLibrary;

    public DailyTournamentPrizes(String registryRepresentation, ProductLibrary productLibrary) {
        _registryRepresentation = registryRepresentation;
        _productLibrary = productLibrary;
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {

        DefaultCardCollection prize = new DefaultCardCollection();
        int hasBye = playerStanding.byeRound > 0 ? 1 : 0;

        int effectiveStanding = playerStanding.standing;
        if (playerStanding.points == firstPlacePoints) {
            // Tie for first place, everyone with first place points considered as tied for first
            effectiveStanding = 1;
        }

        if (playerStanding.playerWins + hasBye >= 2) {
            prize.addItem("Placement Random Chase Card Selector", getMajorPrizeCount(effectiveStanding), true);
            prize.addItem("(S)Tengwar", getTengwarPrizeCount(effectiveStanding), true);
        }

        prize.addAndOpenPack("Any Random Foil", getMinorPrizeCount(effectiveStanding), _productLibrary);

        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount, int firstPlacePoints) {
        return null;
    }


    private int getMinorPrizeCount(int position) {
        if (position < 5)
            return 12 - position * 2;
        else if (position < 9)
            return 3;
        else if (position < 17)
            return 2;
        else if (position < 33)
            return 1;
        return 0;
    }


    private int getMajorPrizeCount(int position) {
        if (position < 11)
            return 11 - position;
        return 0;
    }

    private int getTengwarPrizeCount(int position) {
        if (position < 5)
            return 5 - position;
        return 0;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPrizeDescription() {
        return """
                <div class='prizeHint' value='<ul>
                    <li>2+ Wins: Required for Event Awards and Tengwar cards</li>
                    <li>Top 4: Get Tengwar cards (up to 4 for 1st place)</li>
                    <li>Top 10: Get Event Award (up to 10 for 1st place)</li>
                    <li>Top 32: Receive random foils (more for higher placement)</li>
                </ul>'>Prize Breakdown</div>
                """;
    }
}
