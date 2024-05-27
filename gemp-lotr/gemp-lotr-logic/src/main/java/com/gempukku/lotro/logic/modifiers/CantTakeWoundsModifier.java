package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;

public class CantTakeWoundsModifier extends AbstractModifier {
    public CantTakeWoundsModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Can't take wounds", affectFilter, condition, ModifierEffect.WOUND_MODIFIER);
    }

    @Override
    public boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        return false;
    }
}
