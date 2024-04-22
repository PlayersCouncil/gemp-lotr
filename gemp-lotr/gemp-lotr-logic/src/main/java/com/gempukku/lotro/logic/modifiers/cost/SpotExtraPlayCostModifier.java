package com.gempukku.lotro.logic.modifiers.cost;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.SpotEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.timing.PlayConditions;

public class SpotExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int count;
    private final Filterable[] spotFilter;

    public SpotExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, Filterable ...spotFilter) {
        this(source, affects, condition, 1, spotFilter);
    }

    public SpotExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, int count, Filterable ...spotFilter) {
        super(source, "Spot to play", Filters.changeToFilter(affects), condition);
        this.count = count;
        this.spotFilter = spotFilter;
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return PlayConditions.canSpot(game, count, spotFilter);
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        action.appendCost(
                new SpotEffect(count, spotFilter));
    }
}
