package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class RestoredResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;

    public RestoredResult(PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_RESTORED);
        _source = source;
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getRestoredCard() {
        return _card;
    }
}
