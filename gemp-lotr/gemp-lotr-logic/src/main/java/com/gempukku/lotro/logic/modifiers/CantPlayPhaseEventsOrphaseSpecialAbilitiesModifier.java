package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class CantPlayPhaseEventsOrphaseSpecialAbilitiesModifier extends AbstractModifier {
    private final Timeword phase;
    private final String bannedPlayer;

    public CantPlayPhaseEventsOrphaseSpecialAbilitiesModifier(PhysicalCard source, Condition condition, Phase phase, String bannedPlayer) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        this.phase = Timeword.findByPhase(phase);
        this.bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        if ((action.getType() == Action.Type.PLAY_CARD || action.getType() == Action.Type.SPECIAL_ABILITY)
                && action.getActionTimeword() == phase
                && (bannedPlayer == null || performingPlayer.equals(bannedPlayer)))
            return false;
        return true;
    }
}
