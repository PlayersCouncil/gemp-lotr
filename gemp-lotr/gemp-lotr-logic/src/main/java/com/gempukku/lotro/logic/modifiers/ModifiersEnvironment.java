package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);

    void addedWound(PhysicalCard card);

    int getWoundsTakenInCurrentPhase(PhysicalCard card);

    /**
     * Works out the condition of every modifier in force that counts its own grant once it has held
     * (SelfSpottingCondition), so that what it remembers follows the board at every step of the game.
     */
    void checkSelfSpottingConditions(LotroGame game);
}
