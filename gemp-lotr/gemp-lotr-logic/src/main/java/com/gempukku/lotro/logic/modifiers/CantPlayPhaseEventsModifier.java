package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class CantPlayPhaseEventsModifier extends AbstractModifier {
    private final Phase phase;
    private final String bannedPlayer;

    public CantPlayPhaseEventsModifier(PhysicalCard source, Condition condition, Phase phase, String bannedPlayer) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        this.bannedPlayer = bannedPlayer;
        this.phase = phase;
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        if (action.getType() == Action.Type.PLAY_CARD
                && action.getActionTimeword() == phase
                && (bannedPlayer == null || performingPlayer.equals(bannedPlayer)))
            return false;
        return true;
    }
}
