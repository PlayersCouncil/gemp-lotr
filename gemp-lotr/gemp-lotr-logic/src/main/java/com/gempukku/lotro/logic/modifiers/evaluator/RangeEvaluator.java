package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RangeEvaluator implements Evaluator {
    private final Evaluator _min;
    private final Evaluator _max;

    public RangeEvaluator(int min, int max) {
        this(new ConstantEvaluator(min), new ConstantEvaluator(max));
    }

    public RangeEvaluator(Evaluator min, Evaluator max) {
        _min = min;
        _max = max;
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard self) {
        throw new RuntimeException("This is a range evaluator");
    }

    @Override
    public int getMinimum(LotroGame game, PhysicalCard cardAffected) {
        return _min.evaluateExpression(game, cardAffected);
    }

    @Override
    public int getMaximum(LotroGame game, PhysicalCard cardAffected) {
        return _max.evaluateExpression(game, cardAffected);
    }
}
