package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class SpecialFlagModifier extends AbstractModifier {
    private final ModifierFlag _modifierFlag;

    public SpecialFlagModifier(PhysicalCard source, Condition condition, ModifierFlag modifierFlag) {
        super(source, "Special flag set", null, condition, ModifierEffect.SPECIAL_FLAG_MODIFIER);
        _modifierFlag = modifierFlag;
    }

    @Override
    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag) {
        return modifierFlag == _modifierFlag;
    }
}
