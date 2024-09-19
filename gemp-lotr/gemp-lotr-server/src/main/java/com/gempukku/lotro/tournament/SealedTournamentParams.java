package com.gempukku.lotro.tournament;

import com.gempukku.lotro.db.vo.CollectionType;

public class SealedTournamentParams extends TournamentParams {
    public int deckbuildingDuration;
    public int turnInDuration;
    public String sealedFormatCode;

    @Override
    public Tournament.Stage getInitialStage() {
        return Tournament.Stage.STARTING;
    }
}
