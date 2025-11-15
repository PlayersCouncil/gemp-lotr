package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.*;

public class HinderedRule {
    private final ModifiersLogic _modifiersLogic;

    public HinderedRule(ModifiersLogic modifiersLogic) {
        _modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        Filter hinderedFilter = (game, physicalCard) -> physicalCard.isFlipped();

        Condition rbIsHindered = (LotroGame game) -> game.getGameState().isHindered(game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()));

        _modifiersLogic.addAlwaysOnModifier(new AddKeywordModifier(null, hinderedFilter, null, Keyword.HINDERED));
        //If the Ring-bearer is hindered, burdens cannot be removed
        _modifiersLogic.addAlwaysOnModifier(new CantRemoveBurdensModifier(null, rbIsHindered,Filters.any));
        //If the Ring-bearer is hindered, burdens cannot be added
        _modifiersLogic.addAlwaysOnModifier(new CantAddBurdensModifier(null, rbIsHindered, Filters.any));


    }
}
