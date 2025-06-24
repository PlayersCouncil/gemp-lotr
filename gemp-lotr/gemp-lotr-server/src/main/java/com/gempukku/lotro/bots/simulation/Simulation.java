package com.gempukku.lotro.bots.simulation;

import com.gempukku.lotro.bots.BotPlayer;

public interface Simulation {
    GameResult simulateGame(BotPlayer bot1, BotPlayer bot2);
}
