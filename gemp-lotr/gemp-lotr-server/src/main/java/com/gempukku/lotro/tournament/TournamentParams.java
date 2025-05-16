package com.gempukku.lotro.tournament;

import java.time.LocalDateTime;

public class TournamentParams {
    public String tournamentId;
    public String name;
    public String format;
    public LocalDateTime startTime;
    public Tournament.TournamentType type = Tournament.TournamentType.CONSTRUCTED;
    public Tournament.PairingType playoff = Tournament.PairingType.SWISS;
    public boolean manualKickoff;
    public boolean requiresDeck = true;
    public boolean customCollection;
    public int cost;
    public String tiebreaker;
    public int minimumPlayers = 2;
    public int maximumPlayers = -1; // Negative value = no maximum set
    public Tournament.PrizeType prizes = Tournament.PrizeType.NONE;

    public boolean wc = false;

    public Tournament.Stage getInitialStage() {
        if(manualKickoff)
            return Tournament.Stage.AWAITING_KICKOFF;

        return Tournament.Stage.PLAYING_GAMES;
    }

    public TournamentParams() {}
    public TournamentParams(String tid, String name, String format, int cost, int minPlayers,
            Tournament.PairingType pairing, Tournament.PrizeType prizes) {
        this.tournamentId = tid;
        this.name = name;
        this.format = format;
        this.cost = cost;
        this.minimumPlayers = minPlayers;
        this.playoff = pairing;
        this.prizes = prizes;
    }

}
