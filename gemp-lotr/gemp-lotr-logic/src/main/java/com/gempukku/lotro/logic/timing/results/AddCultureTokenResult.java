package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class AddCultureTokenResult extends EffectResult {
    private final String _performingPlayer;
    private final PhysicalCard _source;
    private final PhysicalCard _target;

    public AddCultureTokenResult(String performingPlayer, PhysicalCard source, PhysicalCard target) {
        super(Type.ADD_CULTURE_TOKEN);
        _performingPlayer = performingPlayer;
        _source = source;
        _target = target;
    }

    public PhysicalCard getSource() {
        return _source;
    }
    public PhysicalCard getTarget() { return _target; }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }
}
