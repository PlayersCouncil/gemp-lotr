package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class HinderedResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;

    public HinderedResult(PhysicalCard source, PhysicalCard card) {
        super(Type.FOR_EACH_HINDERED);
        _source = source;
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public PhysicalCard getHinderedCard() {
        return _card;
    }
}
