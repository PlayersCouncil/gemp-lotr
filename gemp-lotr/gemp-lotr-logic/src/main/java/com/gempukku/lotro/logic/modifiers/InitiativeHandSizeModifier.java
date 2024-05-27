package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class InitiativeHandSizeModifier extends AbstractModifier {
    private final Evaluator _evaluator;

    public InitiativeHandSizeModifier(PhysicalCard source, Condition condition, Evaluator evaluator) {
        super(source, null, null, condition, ModifierEffect.INITIATIVE_MODIFIER);
        _evaluator = evaluator;
    }

    @Override
    public int getInitiativeHandSizeModifier(LotroGame game) {
        return _evaluator.evaluateExpression(game, null);
    }
}
