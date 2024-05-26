package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.CantPlayCardsModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public class RingRelatedRule {
    private final ModifiersLogic _modifiersLogic;

    public RingRelatedRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        _modifiersLogic.addAlwaysOnModifier(
                new AddKeywordModifier(null, Filters.or(Filters.frodo, Filters.sam), Keyword.RING_BOUND));
        _modifiersLogic.addAlwaysOnModifier(
                new CantPlayCardsModifier(null, Filters.frodo, Keyword.CAN_START_WITH_RING));
        _modifiersLogic.addAlwaysOnModifier(
                new AddKeywordModifier(null, Filters.ringBearer, Keyword.RING_BOUND));
    }
}
