package com.gempukku.lotro.league;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.packs.ProductLibrary;

import java.lang.invoke.VarHandle;
import java.util.List;

public class IncentiveLeaguePrizes extends FixedLeaguePrizes{

    final private EventAutoPrizes _autoPrizes;
    public IncentiveLeaguePrizes(ProductLibrary productLibrary, EventAutoPrizes autoPrizes) {
        super(productLibrary);
        _autoPrizes = autoPrizes;
    }

    @Override
    public CardCollection getTrophiesForLeague(PlayerStanding player, List<PlayerStanding> allStandings, int maxGamesPlayed) {
        var prizes = new DefaultCardCollection();

        int cutoffPoints = 0;
        //We are only awarding to e.g. top 10, but there's 11 players or more
        if(allStandings.size() > _autoPrizes.topCutoff()) {
            //While the person in e.g. 10th place is the cutoff, we are lenient and award prizes
            // to anyone who tied their score below them, so that Strength of Schedule isn't
            // constantly ruining everyone's day.
            cutoffPoints = allStandings.get(_autoPrizes.topCutoff()).points;
        }

        if(player.points >= cutoffPoints) {
            for(var prize : _autoPrizes.topPrizes()) {
                prizes.addItem(prize);
            }
        }

        if(player.gamesPlayed >= _autoPrizes.participationGames()) {
            for(var prize : _autoPrizes.participationPrizes()) {
                prizes.addItem(prize);
            }
        }

        return prizes;
    }
}
