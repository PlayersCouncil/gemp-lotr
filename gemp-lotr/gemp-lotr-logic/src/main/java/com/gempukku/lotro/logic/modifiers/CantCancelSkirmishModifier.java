package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantCancelSkirmishModifier extends AbstractModifier {
    public CantCancelSkirmishModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Can't cancel skirmish", affectFilter, condition, ModifierEffect.CANCEL_SKIRMISH_MODIFIER);
    }

    @Override
    public boolean canCancelSkirmish(LotroGame game, PhysicalCard card) {
        return false;
    }
}
