package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class DeadPileGoesToDiscardModifier extends AbstractModifier {
    public DeadPileGoesToDiscardModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Goes to discard pile instead of dead pile", affectFilter, condition, ModifierEffect.DEAD_PILE_MODIFIER);
    }

    @Override
    public boolean deadPileGoesToDiscard(LotroGame game, PhysicalCard card) {
        return true;
    }
}
