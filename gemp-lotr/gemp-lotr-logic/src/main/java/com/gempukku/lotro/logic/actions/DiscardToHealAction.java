package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.effects.DiscardToHealEffect;

public class DiscardToHealAction extends ActivateCardAction {
    public DiscardToHealAction(PhysicalCard physicalCard, String performingPlayer) {
        super(physicalCard, performingPlayer);
        setText("Heal by discarding");
    }

    @Override
    public Type getType() {
        return Type.OTHER;
    }

    @Override
    protected void generateCardEffect() {
        _activateCardEffect = new DiscardToHealEffect(_physicalCard, _player, getActionTimeword());
    }
}
