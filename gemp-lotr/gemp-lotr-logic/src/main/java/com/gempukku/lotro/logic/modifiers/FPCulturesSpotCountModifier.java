package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class FPCulturesSpotCountModifier extends AbstractModifier {
    private final Evaluator _valueEvaluator;

    public FPCulturesSpotCountModifier(PhysicalCard source, Condition condition, Evaluator value) {
        super(source, "Modifies FP culture count", null, condition, ModifierEffect.SPOT_MODIFIER);
        _valueEvaluator = value;
    }

    @Override
    public int getFPCulturesSpotCountModifier(LotroGame game, String playerId) {
        return _valueEvaluator.evaluateExpression(game, null);
    }
}
