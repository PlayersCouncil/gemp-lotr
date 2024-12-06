package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class RemoveCultureTokenResult extends EffectResult {
    private final String _performingPlayerId;
    private final PhysicalCard _source;
    private final PhysicalCard _target;

    public RemoveCultureTokenResult(String performingPlayerId, PhysicalCard source, PhysicalCard target) {
        super(Type.REMOVE_CULTURE_TOKEN);
        _performingPlayerId = performingPlayerId;
        _source = source;
        _target = target;
    }

    public String getPerformingPlayer() {
        return _performingPlayerId;
    }

    public PhysicalCard getSource() {
        return _source;
    }
    public PhysicalCard getTarget() { return _target; }
}
