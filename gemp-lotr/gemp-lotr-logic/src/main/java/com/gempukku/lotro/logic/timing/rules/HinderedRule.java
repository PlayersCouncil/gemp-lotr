package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public class HinderedRule {
    private final ModifiersLogic _modifiersLogic;

    public HinderedRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        Filter hinderedFilter = (game, physicalCard) -> physicalCard.isFlipped();

        _modifiersLogic.addAlwaysOnModifier(new AddKeywordModifier(null, hinderedFilter, null, Keyword.HINDERED));
    }
}
