package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedToSkirmishModifier;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

public class CunningRule {
    private final ModifiersLogic _modifiersLogic;

    public CunningRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new CantTakeWoundsModifier(null, new PhaseCondition(Phase.ARCHERY), Keyword.CUNNING));
        _modifiersLogic.addAlwaysOnModifier(
                new CantBeAssignedToSkirmishModifier(null, null, Keyword.CUNNING));
    }
}
