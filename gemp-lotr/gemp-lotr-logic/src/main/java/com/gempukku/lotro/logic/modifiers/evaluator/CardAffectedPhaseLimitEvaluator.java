package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.LimitCounter;

import java.util.HashMap;
import java.util.Map;

public class CardAffectedPhaseLimitEvaluator implements Evaluator {
    private final Map<Integer, Integer> _evaluatedForCard = new HashMap<>();

    private final String prefix;
    private final Evaluator evaluator;

    private final PhysicalCard source;
    private final Phase phase;
    private final int limit;

    public CardAffectedPhaseLimitEvaluator(PhysicalCard source, Phase phase, int limit, String prefix, Evaluator evaluator) {
        this.source = source;
        this.phase = phase;
        this.limit = limit;
        this.prefix = prefix;
        this.evaluator = evaluator;
    }

    private int evaluateOnce(LotroGame game, PhysicalCard cardAffected) {
        LimitCounter limitCounter = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(source, prefix + cardAffected.getCardId() + "_", phase);
        int internalResult = evaluator.evaluateExpression(game, cardAffected);
        return limitCounter.incrementToLimit(limit, internalResult);
    }

    @Override
    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
        Integer value = _evaluatedForCard.get(cardAffected.getCardId());
        if (value == null) {
            value = evaluateOnce(game, cardAffected);
            _evaluatedForCard.put(cardAffected.getCardId(), value);
        }
        return value;
    }

    @Override
    public boolean isSpent(LotroGame game, PhysicalCard cardAffected) {
        // An amount already worked out by this evaluator has had its share of the limit.
        if (cardAffected == null || _evaluatedForCard.containsKey(cardAffected.getCardId()))
            return false;
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(source, prefix + cardAffected.getCardId() + "_", phase).getUsedLimit() >= limit;
    }
}
