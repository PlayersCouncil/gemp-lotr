package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RangeEvaluator implements Evaluator {
    private final int _min;
    private final int _max;

    public RangeEvaluator(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard self) {
        throw new RuntimeException("This is a range evaluator");
    }

    @Override
    public int getMaximum(ActionContext actionContext) {
        return _max;
    }

    @Override
    public int getMinimum(ActionContext actionContext) {
        return _min;
    }
}
