package com.gempukku.lotro.logic.modifiers.cost;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.AttachPermanentAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Condition;

public class ExertTargetExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    public ExertTargetExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition) {
        super(source, "Exert to play", Filters.changeToFilter(affects), condition);
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        ((AttachPermanentAction) action).setExertTarget(true);
    }
}
