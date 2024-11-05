package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RemoveGameTextModifier extends AbstractModifier {
    public RemoveGameTextModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Has it's game text removed", affectFilter, condition, ModifierEffect.TEXT_MODIFIER);
    }

    @Override
    public boolean hasRemovedText(LotroGame game, PhysicalCard physicalCard) {
        return true;
    }
}
