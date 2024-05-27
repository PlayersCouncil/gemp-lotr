package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantDiscardFromPlayByPlayerModifier extends AbstractModifier {
    private final String _notPlayer;

    public CantDiscardFromPlayByPlayerModifier(PhysicalCard source, String text, Condition condition, Filterable affectFilter, String notPlayer) {
        super(source, text, affectFilter, condition, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER);
        _notPlayer = notPlayer;
    }

    @Override
    public boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        if (!_notPlayer.equals(performingPlayer))
            return false;
        return true;
    }
}
