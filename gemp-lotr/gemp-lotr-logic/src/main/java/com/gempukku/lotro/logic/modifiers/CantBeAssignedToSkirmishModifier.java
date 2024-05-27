package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantBeAssignedToSkirmishModifier extends AbstractModifier {
    public CantBeAssignedToSkirmishModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Can't be assigned to skirmish", affectFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return true;
    }
}
