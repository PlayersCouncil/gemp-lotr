package com.gempukku.lotro.simulation;

public class SimulationStats {
    private final int bot1Wins;
    private final int bot2Wins;
    private final int totalGames;

    public SimulationStats(int bot1Wins, int bot2Wins, int totalGames) {
        this.bot1Wins = bot1Wins;
        this.bot2Wins = bot2Wins;
        this.totalGames = totalGames;
    }

    public double getBot1WinRate() {
        return bot1Wins * 100.0 / totalGames;
    }

    public double getBot2WinRate() {
        return bot2Wins * 100.0 / totalGames;
    }

    @Override
    public String toString() {
        return String.format("Bot1: %.1f%%, Bot2: %.1f%%", getBot1WinRate(), getBot2WinRate());
    }
}
