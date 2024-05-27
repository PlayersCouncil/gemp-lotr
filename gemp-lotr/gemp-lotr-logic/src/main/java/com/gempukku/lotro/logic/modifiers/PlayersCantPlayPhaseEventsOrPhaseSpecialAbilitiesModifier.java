package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier extends AbstractModifier {
    private final Phase _phase;

    public PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier(PhysicalCard source, Condition condition, Phase phase) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _phase = phase;
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        if ((action.getType() == Action.Type.PLAY_CARD || action.getType() == Action.Type.SPECIAL_ABILITY)
                && action.getActionTimeword() == _phase)
            return false;
        return true;
    }
}
