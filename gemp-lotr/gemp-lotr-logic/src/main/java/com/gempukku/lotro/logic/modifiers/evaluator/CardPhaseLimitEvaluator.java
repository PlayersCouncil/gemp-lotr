package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.LimitCounter;

public class CardPhaseLimitEvaluator implements Evaluator {
    private Integer _evaluated;

    private final Evaluator _amount;

    private final PhysicalCard _source;
    private final Phase _phase;
    private final Evaluator _limit;

    public CardPhaseLimitEvaluator(PhysicalCard source, Phase phase, Evaluator limit, Evaluator amount) {
        _source = source;
        _phase = phase;
        _limit = limit;
        _amount = amount;
    }

    private int evaluateOnce(LotroGame game, PhysicalCard cardAffected) {
        LimitCounter limitCounter = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_source, _phase);
        int amountResult = _amount.evaluateExpression(game, cardAffected);
        int limitResult = _limit.evaluateExpression(game, cardAffected);
        return limitCounter.incrementToLimit(limitResult, amountResult);
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
        if (_evaluated == null)
            _evaluated = evaluateOnce(game, cardAffected);
        return _evaluated;
    }
}
