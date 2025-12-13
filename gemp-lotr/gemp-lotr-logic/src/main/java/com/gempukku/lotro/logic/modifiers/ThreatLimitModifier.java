package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class ThreatLimitModifier extends AbstractModifier {
    private final Evaluator _threatLimitModifier;

    public ThreatLimitModifier(PhysicalCard source, int moveLimitModifier) {
        this(source, moveLimitModifier, null);
    }

    public ThreatLimitModifier(PhysicalCard source, int moveLimitModifier, Condition condition) {
        super(source, null, null, condition, ModifierEffect.THREAT_LIMIT_MODIFIER);
        _threatLimitModifier = new ConstantEvaluator(moveLimitModifier);
    }

    @Override
    public int getThreatLimitModifier(LotroGame game) {
        return _threatLimitModifier.evaluateExpression(game, null);
    }
}
