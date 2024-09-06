package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.packs.SetDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SingleEliminationOnDemandPrizes implements TournamentPrizes{
    private final List<String> _promos = new ArrayList<>();
    private final String _registryRepresentation;

    public SingleEliminationOnDemandPrizes(LotroCardBlueprintLibrary library, String registryRepresentation) {
        _registryRepresentation = registryRepresentation;
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.IsDecipherSet())
                _promos.addAll(setDefinition.getCardsOfRarity("P"));
        }
    }

    @Override
    public CardCollection getPrizeForTournament(PlayerStanding playerStanding, int playersCount) {
        DefaultCardCollection tournamentPrize = new DefaultCardCollection();
        if (playerStanding.points == 4) {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 2);
            tournamentPrize.addItem(getRandom(_promos), 1);
        } else if (playerStanding.points == 3) {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 2);
        } else {
            tournamentPrize.addItem("(S)All Decipher Choice - Booster", 1);
        }

        if (!tournamentPrize.getAll().iterator().hasNext())
            return null;
        return tournamentPrize;
    }

    @Override
    public CardCollection getTrophyForTournament(PlayerStanding playerStanding, int playersCount) {
        return null;
    }

    private String getRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPrizeDescription() {
        return "<div class='prizeHint' value='2 wins - 2 boosters and a random promo, 1 win - 2 boosters, 0 wins - 1 booster'>(2+promo)-2-1</div>";
    }
}
