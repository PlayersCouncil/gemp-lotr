package com.gempukku.lotro.tournament;

public class SoloTableDraftTournamentParams extends TournamentParams {
    public int deckbuildingDuration;
    public int turnInDuration;
    public String soloTableDraftFormatCode;

    @Override
    public Tournament.Stage getInitialStage() {
        return Tournament.Stage.STARTING;
    }
}
