package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class StrengthModifier extends AbstractModifier {
    private final Evaluator _evaluator;
    private final boolean _nonCardTextModifier;

    public StrengthModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public StrengthModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(source, affectFilter, condition, evaluator, false);
    }

    public StrengthModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator, boolean nonCardTextModifier) {
        super(source, null, affectFilter, condition, ModifierEffect.STRENGTH_MODIFIER);
        _evaluator = evaluator;
        _nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        final int value = _evaluator.evaluateExpression(game, self);
        if (value >= 0)
            return "Strength +" + value;
        else
            return "Strength " + value;
    }

    @Override
    public int getStrengthModifier(LotroGame game, PhysicalCard physicalCard) {
        return _evaluator.evaluateExpression(game, physicalCard);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return _nonCardTextModifier;
    }
}
