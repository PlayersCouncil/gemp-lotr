package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class ArcheryTotalModifier extends AbstractModifier {
    private final Side _side;
    private final Evaluator _evaluator;

    public ArcheryTotalModifier(PhysicalCard source, Side side, int modifier) {
        this(source, side, null, modifier);
    }

    public ArcheryTotalModifier(PhysicalCard source, Side side, Condition condition, int modifier) {
        this(source, side, condition, new ConstantEvaluator(modifier));
    }

    public ArcheryTotalModifier(PhysicalCard source, Side side, Condition condition, Evaluator evaluator) {
        super(source, null, null, condition, ModifierEffect.ARCHERY_MODIFIER);
        _side = side;
        _evaluator = evaluator;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        int modifier = _evaluator.evaluateExpression(game, self);
        return ((_side == Side.FREE_PEOPLE) ? "Fellowship" : "Minion") + " archery total " + ((modifier < 0) ? modifier : ("+" + modifier));
    }

    @Override
    public int getArcheryTotalModifier(LotroGame game, Side side) {
        if (side == _side)
            return _evaluator.evaluateExpression(game, null);
        return 0;
    }
}
