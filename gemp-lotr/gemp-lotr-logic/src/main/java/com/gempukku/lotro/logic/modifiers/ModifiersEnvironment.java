package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);

    void addedWound(PhysicalCard card);

    int getWoundsTakenInCurrentPhase(PhysicalCard card);
}
