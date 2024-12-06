package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class ResistanceModifier extends AbstractModifier {
    private final Evaluator evaluator;
    private final boolean nonCardTextModifier;

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        this(source, affectFilter, condition, new ConstantEvaluator(modifier));
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator) {
        this(source, affectFilter, condition, evaluator, false);
    }

    public ResistanceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Evaluator evaluator, boolean nonCardTextModifier) {
        super(source, null, affectFilter, condition, ModifierEffect.RESISTANCE_MODIFIER);
        this.evaluator = evaluator;
        this.nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public boolean isNonCardTextModifier() {
        return nonCardTextModifier;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        int modifier = evaluator.evaluateExpression(game, self);
        return "Resistance " + ((modifier < 0) ? modifier : ("+" + modifier));
    }

    @Override
    public int getResistanceModifier(LotroGame game, PhysicalCard physicalCard) {
        return evaluator.evaluateExpression(game, physicalCard);
    }
}
