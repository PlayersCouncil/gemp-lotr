package com.gempukku.lotro.draft3;

public interface BoosterProducer {
    Booster getBooster(int round);
    int getMaxRound();
}
