package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class VitalityModifier extends AbstractModifier {
    private final Evaluator _modifier;
    private final boolean _nonCardTextModifier;

    public VitalityModifier(PhysicalCard source, Filterable affectFilter, int modifier) {
        this(source, affectFilter, modifier, false);
    }

    public VitalityModifier(PhysicalCard source, Filterable affectFilter, int modifier, boolean nonCardTextModifier) {
        this(source, affectFilter, new ConstantEvaluator(modifier), nonCardTextModifier);
    }

    public VitalityModifier(PhysicalCard source, Filterable affectFilter, Evaluator modifier, boolean nonCardTextModifier) {
        super(source, "Vitality modifier", affectFilter, ModifierEffect.VITALITY_MODIFIER);
        _modifier = modifier;
        _nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public int getVitalityModifier(LotroGame game, PhysicalCard physicalCard) {
        return _modifier.evaluateExpression(game, physicalCard);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return _nonCardTextModifier;
    }
}
