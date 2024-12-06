package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class SanctuaryHealModifier extends AbstractModifier {
    private final Evaluator evaluator;

    public SanctuaryHealModifier(PhysicalCard source, Condition condition, Evaluator amount) {
        super(source, "Sanctuary heal modifier "+amount, null, condition, ModifierEffect.SANCTUARY_HEAL_MODIFIER);
        evaluator = amount;
    }

    @Override
    public int getSanctuaryHealModifier(LotroGame game) {
        return evaluator.evaluateExpression(game, null);
    }
}
