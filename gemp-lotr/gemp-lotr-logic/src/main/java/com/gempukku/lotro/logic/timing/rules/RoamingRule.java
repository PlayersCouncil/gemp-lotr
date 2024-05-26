package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public class RoamingRule {
    private final ModifiersLogic _modifiersLogic;

    public RoamingRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        Filter roamingFilter = Filters.and(CardType.MINION, new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return (game.getModifiersQuerying().getMinionSiteNumber(game, physicalCard) > game.getGameState().getCurrentSiteNumber());
            }
        });

        _modifiersLogic.addAlwaysOnModifier(
                new AddKeywordModifier(null, roamingFilter, Keyword.ROAMING));
    }
}
