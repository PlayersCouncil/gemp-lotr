package com.gempukku.lotro.competitive;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerStanding {
    public String playerName;
    public int points;
    public int gamesPlayed;
    public int playerWins;
    public int playerLosses;
    public int byeRound;
    public int medianScore;
    public int cumulativeScore;
    public float opponentWinRate;
    public int standing;

    public PlayerStanding(String playerName, int points, int gamesPlayed, int playerWins, int playerLosses,
            int byeRound) {
        this(playerName, points, gamesPlayed, playerWins, playerLosses,
        byeRound, 0, 0, 0, 0);
    }

    public PlayerStanding(String playerName, int points, int gamesPlayed, int playerWins, int playerLosses,
            int byeRound, float opponentWinRate, int standing) {
        this(playerName, points, gamesPlayed, playerWins, playerLosses,
                byeRound, 0, 0, opponentWinRate, standing);
    }

    public PlayerStanding(String playerName, int points, int gamesPlayed, int playerWins, int playerLosses,
            int byeRound, int medianScore, int cumulativeScore, float opponentWinRate, int standing) {
        this.playerName = playerName;
        this.points = points;
        this.gamesPlayed = gamesPlayed;
        this.playerWins = playerWins;
        this.playerLosses = playerLosses;
        this.byeRound = byeRound;
        this.medianScore = medianScore;
        this.cumulativeScore = cumulativeScore;
        this.opponentWinRate = opponentWinRate;
        this.standing = standing;
    }

    public static PlayerStanding CopyStanding(PlayerStanding original) {
        var copy = new PlayerStanding(original.playerName, original.points, original.gamesPlayed, original.playerWins,
                original.playerLosses, original.byeRound);
        copy.medianScore = original.medianScore;
        copy.cumulativeScore = original.cumulativeScore;
        copy.opponentWinRate = original.opponentWinRate;
        copy.standing = original.standing;

        return copy;
    }
}
