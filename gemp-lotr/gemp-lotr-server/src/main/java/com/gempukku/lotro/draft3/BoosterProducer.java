package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;

public interface BoosterProducer {
    Booster getBooster(int round);
}
