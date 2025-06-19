package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.timing.EffectResult;

public class AddTwilightResult extends EffectResult {
    private final PhysicalCard _source;
    private final AddTwilightEffect.Cause _cause;
    private final int _amount;

    public AddTwilightResult(PhysicalCard source, AddTwilightEffect.Cause cause, int amount) {
        super(Type.ADD_TWILIGHT);
        _source = source;
        _amount = amount;
        _cause = cause;
    }

    public PhysicalCard getSource() {
        return _source;
    }
    public int getAmount() {
        return _amount;
    }
    public AddTwilightEffect.Cause getCause() {
        return _cause;
    }
}
