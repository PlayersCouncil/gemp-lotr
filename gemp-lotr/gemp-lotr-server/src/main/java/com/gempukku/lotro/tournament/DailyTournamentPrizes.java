package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.packs.ProductLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DailyTournamentPrizes implements TournamentPrizes {
    private final String _registryRepresentation;
    private final ProductLibrary _productLibrary;

    public DailyTournamentPrizes(String registryRepresentation, ProductLibrary productLibrary) {
        _registryRepresentation = registryRepresentation;
        _productLibrary = productLibrary;
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount) {

        DefaultCardCollection prize = new DefaultCardCollection();
        int hasBye = playerStanding.byeRound > 0 ? 1 : 0;
        if (playerStanding.playerWins + hasBye >= 2) {
            prize.addItem("Placement Random Chase Card Selector", getMajorPrizeCount(playerStanding.standing), true);
            prize.addItem("(S)Tengwar", getTengwarPrizeCount(playerStanding.standing), true);
        }

        prize.addAndOpenPack("Any Random Foil", getMinorPrizeCount(playerStanding.standing), _productLibrary);

        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount) {
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
            <div class='prizeHint' value='<ul><li>1st - 10 random foil cards, 4 Tengwar selection, 3 High Place Event Reward</li><li>2nd - 8 random foil cards, 3 Tengwar selection, 2 High Place Event Reward</li><li>3rd - 6 random foil cards, 2 Tengwar selection, 1 High Place Event Reward</li><li>4th - 4 random foil cards, 1 Tengwar selection</li><li>5th-8th - 3 random foil cards</li><li>9th-16th - 2 random foil cards</li><li>17th-32nd - 1 random foil card</li></ul>'>Prize Breakdown</div>""";
    }
}
