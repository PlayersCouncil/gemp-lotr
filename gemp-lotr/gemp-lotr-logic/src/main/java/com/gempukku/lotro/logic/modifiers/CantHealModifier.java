package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantHealModifier extends AbstractModifier {
    public CantHealModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        super(source, "Can't be healed", Filters.and(affectFilters), condition, ModifierEffect.WOUND_MODIFIER);
    }

    @Override
    public boolean canBeHealed(LotroGame game, PhysicalCard card) {
        return false;
    }
}
