package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class RoamingPenaltyModifier extends AbstractModifier {
    private final Evaluator _evaluator;

    public RoamingPenaltyModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public RoamingPenaltyModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        super(source, null, affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _evaluator = evaluator;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        int modifier = _evaluator.evaluateExpression(game, self);
        return "Roaming penalty " + ((modifier > 0) ? ("+" + modifier) : modifier);
    }

    @Override
    public int getRoamingPenaltyModifier(LotroGame game, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(game, physicalCard);
    }
}
