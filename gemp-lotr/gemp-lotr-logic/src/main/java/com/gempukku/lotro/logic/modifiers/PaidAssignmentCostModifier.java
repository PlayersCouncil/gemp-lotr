package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class PaidAssignmentCostModifier extends AbstractModifier {
    public PaidAssignmentCostModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Assignment cost paid", affectFilter, condition, ModifierEffect.PAID_ASSIGNMENT_COST_MODIFIER);
    }

    @Override
    public boolean isAssignmentCostPaid(LotroGame game, PhysicalCard card) {
        return true;
    }
}
