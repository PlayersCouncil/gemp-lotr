package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class NewRingBearerResult extends EffectResult {
    private final PhysicalCard _newRB;
    private final PhysicalCard _oldRB;

    public NewRingBearerResult(PhysicalCard oldRB, PhysicalCard newRB) {
        super(Type.NEW_RING_BEARER);
        _newRB = newRB;
        _oldRB = oldRB;
    }

    public PhysicalCard getNewRingBearer() {
        return _newRB;
    }

    public PhysicalCard getOldRingBearer() {
        return _oldRB;
    }
}
