package com.gempukku.lotro.simulation;

public class SimpleBatchSimulationRunner implements SimulationRunner{
    private final Simulation simulation;
    private final BotPlayer bot1;
    private final BotPlayer bot2;
    private final int numGames;

    public SimpleBatchSimulationRunner(Simulation simulation, BotPlayer bot1, BotPlayer bot2, int numGames) {
        this.simulation = simulation;
        this.bot1 = bot1;
        this.bot2 = bot2;
        this.numGames = numGames;
    }

    @Override
    public SimulationStats run() {
        int bot1Wins = 0;

        for (int i = 0; i < numGames; i++) {
            GameResult result = simulation.simulateGame(bot1, bot2);
            if (result == GameResult.P1_WON) {
                bot1Wins++;
            }
        }

        return new SimulationStats(bot1Wins, numGames - bot1Wins, numGames);
    }
}
