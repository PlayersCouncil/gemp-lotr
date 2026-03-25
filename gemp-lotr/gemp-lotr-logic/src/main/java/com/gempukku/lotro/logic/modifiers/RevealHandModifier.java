package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RevealHandModifier extends AbstractModifier {
    private final String playerId;

    public RevealHandModifier(PhysicalCard source, Condition condition, String playerId) {
        super(source, "Hand is revealed", null, condition, ModifierEffect.HAND_REVEAL_MODIFIER);
        this.playerId = playerId;
    }

    @Override
    public boolean isHandRevealed(LotroGame game, String forPlayerId) {
        return playerId == null || playerId.equals(forPlayerId);
    }
}
