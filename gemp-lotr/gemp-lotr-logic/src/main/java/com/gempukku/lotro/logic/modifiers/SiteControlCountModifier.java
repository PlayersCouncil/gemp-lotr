package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class SiteControlCountModifier extends AbstractModifier {
    private final Evaluator _valueEvaluator;

    public SiteControlCountModifier(PhysicalCard source, Condition condition, Evaluator value) {
        super(source, "Modifies Shadow controlled site count", null, condition, ModifierEffect.SPOT_MODIFIER);
        _valueEvaluator = value;
    }

    @Override
    public int getSiteControlledSpotCountModifier(LotroGame game, String playerId) {
        return _valueEvaluator.evaluateExpression(game, null);
    }
}
