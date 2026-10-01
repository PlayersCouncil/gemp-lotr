package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class SpecialFlagModifier extends AbstractModifier {
    private final ModifierFlag _modifierFlag;
    private final String _playerId;

    public SpecialFlagModifier(PhysicalCard source, Condition condition, ModifierFlag modifierFlag) {
        this(source, condition, modifierFlag, null);
    }

    /**
     * @param playerId when non-null, the flag only applies to that player; the flag is then invisible
     *                 to the player-agnostic {@link #hasFlagActive(LotroGame, ModifierFlag)} query.
     */
    public SpecialFlagModifier(PhysicalCard source, Condition condition, ModifierFlag modifierFlag, String playerId) {
        super(source, "Special flag set", null, condition, ModifierEffect.SPECIAL_FLAG_MODIFIER);
        _modifierFlag = modifierFlag;
        _playerId = playerId;
    }

    @Override
    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag) {
        return modifierFlag == _modifierFlag && _playerId == null;
    }

    @Override
    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag, String playerId) {
        return modifierFlag == _modifierFlag && (_playerId == null || _playerId.equals(playerId));
    }
}
