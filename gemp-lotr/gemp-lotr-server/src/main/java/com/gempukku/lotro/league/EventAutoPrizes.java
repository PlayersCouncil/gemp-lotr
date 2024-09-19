package com.gempukku.lotro.league;

import com.gempukku.lotro.game.CardCollection;

import java.util.List;

public record EventAutoPrizes(List<CardCollection.Item> topPrizes,
                              int topCutoff,
                              List<CardCollection.Item> participationPrizes,
                              int participationGames) {
}
