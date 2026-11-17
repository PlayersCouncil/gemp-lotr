package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.EffectResult;

public class ExertResult extends EffectResult implements ActionSource {
    private final Action _action;
    private final PhysicalCard _card;
    private final boolean _forToil;
    private final boolean _paidAsCost;

    public ExertResult(Action action, PhysicalCard card, boolean forToil, boolean paidAsCost) {
        super(Type.FOR_EACH_EXERTED);
        _action = action;
        _card = card;
        _forToil = forToil;
        _paidAsCost = paidAsCost;
    }

    public PhysicalCard getExertedCard() {
        return _card;
    }

    public Action getAction() {
        return _action;
    }

    public boolean isForToil() {
        return _forToil;
    }

    /**
     * Whether a definition's exertion was carried out as a cost: the `cost` flag its `Exert` or `Exhaust` was run
     * with, so true in an action's `cost` (through a `choice` or an `if` there as well) and in the cost of a
     * `costToEffect` or an `optional`, and false in an effect.  Its one reader is the `ExertsForSpecialAbility`
     * trigger (F-0478).  The exertions the engine makes for a play on its own account (toil, an exert discount,
     * "to play, exert bearer") do not say, and read false; `ExertsToPlay` knows them by the action's type.
     */
    public boolean isPaidAsCost() {
        return _paidAsCost;
    }
}
