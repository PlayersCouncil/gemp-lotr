package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class ShouldSkipPhaseModifier extends AbstractModifier {
    private final Phase _phase;

    public ShouldSkipPhaseModifier(PhysicalCard source, Condition condition, Phase phase) {
        super(source, "Skip " + phase.toString() + " phase", null, condition, ModifierEffect.ACTION_MODIFIER);
        _phase = phase;
    }

    @Override
    public boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId) {
        if (phase == _phase)
            return true;
        return false;
    }
}