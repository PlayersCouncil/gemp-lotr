package com.gempukku.lotro.tournament;

public class SoloDraftTournamentParams extends TournamentParams {
    public int deckbuildingDuration;
    public int turnInDuration;
    public String soloDraftFormatCode;

    @Override
    public Tournament.Stage getInitialStage() {
        return Tournament.Stage.STARTING;
    }
}
