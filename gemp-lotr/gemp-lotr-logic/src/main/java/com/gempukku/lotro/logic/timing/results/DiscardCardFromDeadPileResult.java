package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class DiscardCardFromDeadPileResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;
    private final String _deadPileId;

    public DiscardCardFromDeadPileResult(PhysicalCard source, PhysicalCard card, String deadPilePlayer) {
        super(Type.FOR_EACH_DISCARDED_FROM_HAND);
        _source = source;
        _card = card;
        _deadPileId = deadPilePlayer;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public String get_deadPileIdId() {
        return _deadPileId;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}