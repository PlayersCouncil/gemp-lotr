package com.gempukku.lotro.tournament;

import com.gempukku.lotro.draft3.timer.DraftTimerProducer;

public class TableDraftTournamentParams extends TournamentParams {
    public DraftTimerProducer.Type draftTimerProducerType;
    public int deckbuildingDuration;
    public int turnInDuration;
    public String tableDraftFormatCode;

    @Override
    public Tournament.Stage getInitialStage() {
        return Tournament.Stage.STARTING;
    }
}
