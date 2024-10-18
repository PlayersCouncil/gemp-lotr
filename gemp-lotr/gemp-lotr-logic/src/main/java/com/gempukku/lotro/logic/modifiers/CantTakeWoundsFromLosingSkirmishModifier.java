package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantTakeWoundsFromLosingSkirmishModifier extends AbstractModifier {
    public CantTakeWoundsFromLosingSkirmishModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Can't take wounds", affectFilter, condition, ModifierEffect.WOUND_MODIFIER);
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard physicalCard) {
        return false;
    }
}
