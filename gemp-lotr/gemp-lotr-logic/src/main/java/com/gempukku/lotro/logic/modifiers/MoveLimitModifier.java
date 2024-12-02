package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class MoveLimitModifier extends AbstractModifier {
    private final Evaluator _moveLimitModifier;

    public MoveLimitModifier(PhysicalCard source, int moveLimitModifier) {
        this(source, moveLimitModifier, null);
    }

    public MoveLimitModifier(PhysicalCard source, int moveLimitModifier, Condition condition) {
        super(source, null, null, condition, ModifierEffect.MOVE_LIMIT_MODIFIER);
        _moveLimitModifier = new ConstantEvaluator(moveLimitModifier);
    }

    @Override
    public int getMoveLimitModifier(LotroGame game) {
        return _moveLimitModifier.evaluateExpression(game, null);
    }
}
