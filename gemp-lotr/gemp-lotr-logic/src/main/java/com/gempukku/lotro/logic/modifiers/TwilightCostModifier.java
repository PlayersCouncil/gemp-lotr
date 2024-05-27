package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class TwilightCostModifier extends AbstractModifier {
    private final Evaluator _evaluator;

    public TwilightCostModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(source, null, affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _evaluator = evaluator;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        final int value = _evaluator.evaluateExpression(game, self);
        if (value >= 0)
            return "Twilight cost +" + value;
        else
            return "Twilight cost " + value;
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return _evaluator.evaluateExpression(game, physicalCard);
    }
}
