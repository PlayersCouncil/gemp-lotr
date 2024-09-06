package com.gempukku.lotro.league;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.packs.ProductLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FixedLeaguePrizes implements LeaguePrizes {

    private final ProductLibrary _productLibrary;
    public FixedLeaguePrizes(ProductLibrary productLibrary) {
        _productLibrary = productLibrary;
    }

    @Override
    public CardCollection getPrizeForLeagueMatchWinner(int winCountThisSerie, int totalGamesPlayedThisSerie) {
        DefaultCardCollection winnerPrize = new DefaultCardCollection();
        if (winCountThisSerie % 2 == 1) {
            winnerPrize.addAndOpenPack("Any Random Foil", 1, _productLibrary);
        } else {
            if (winCountThisSerie <= 4) {
                winnerPrize.addAndOpenPack("Random Uncommon Foil", 1, _productLibrary);
            } else if (winCountThisSerie <= 8) {
                winnerPrize.addAndOpenPack("Random Rare Foil", 1, _productLibrary);
            } else {
                winnerPrize.addAndOpenPack("Random Ultra Rare Foil", 1, _productLibrary);
            }
        }
        return winnerPrize;
    }

    @Override
    public CardCollection getPrizeForLeagueMatchLoser(int winCountThisSerie, int totalGamesPlayedThisSerie) {
        return null;
    }

    @Override
    public CardCollection getPrizeForLeague(int position, int playersCount, int gamesPlayed, int maxGamesPlayed) {
        DefaultCardCollection prize = new DefaultCardCollection();
        prize.addAndOpenPack("Any Random Foil", getMinorPrizeCount(position), _productLibrary);
        prize.addItem("Event Chase Booster", getMajorPrizeCount(position), true);
        prize.addAndOpenPack("Random Tengwar", getTengwarPrizeCount(position), _productLibrary);
        if (prize.getAll().iterator().hasNext())
            return prize;
        return null;
    }

    @Override
    public CardCollection getTrophiesForLeague(PlayerStanding standing, List<PlayerStanding> allStandings, int maxGamesPlayed) {
        return null;
    }


// Old sealed leagues:
//1st - 60 boosters, 4 tengwar, 3 foil rares
//2nd - 55 boosters, 3 tengwar, 2 foil rares
//3rd - 50 boosters, 2 tengwar, 1 foil rare
//4th - 45 boosters, 1 tengwar
//5th-8th - 40 boosters
//9th-16th - 35 boosters
//17th-32nd - 20 boosters
//33rd-64th - 10 boosters
//65th-128th - 5 boosters

//Old My Cards leagues:
//1st - 30 boosters, 4 tengwar, 3 foil rares
//2nd - 25 boosters, 3 tengwar, 2 foil rares
//3rd - 20 boosters, 2 tengwar, 1 foil rares
//4th - 15 boosters, 1 tengwar
//5th-8th - 10 boosters
//9th-16th - 5 boosters
//17th-32nd - 2 boosters

// Old Constructed leagues:
//1st - 10 boosters, 4 tengwar, 3 foil rares
//2nd - 8 boosters, 3 tengwar, 2 foil rares
//3rd - 6 boosters, 2 tengwar, 1 foil rare
//4th - 4 boosters, 1 tengwar
//5th-8th - 3 boosters
//9th-16th - 2 boosters
//17th-32nd - 1 boosters


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

    private String getRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private List<String> getRandomFoil(List<String> list, int count) {
        List<String> result = new LinkedList<>();
        for (String element : list)
            result.add(element + "*");
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result.subList(0, count);
    }

}
