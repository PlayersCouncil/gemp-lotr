package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class SanctuaryMayRemoveBurdensModifier extends AbstractModifier {
    private final String playerId;

    public SanctuaryMayRemoveBurdensModifier(PhysicalCard source, Condition condition, String playerId) {
        super(source, "May remove a burden instead of healing during Sanctuary healing", null, condition, ModifierEffect.SANCTUARY_REMOVE_BURDEN_MODIFIER);
        this.playerId = playerId;
    }

    @Override
    public boolean sanctuaryMayRemoveBurdens(LotroGame game, String forPlayerId) {
        return playerId == null || playerId.equals(forPlayerId);
    }
}
