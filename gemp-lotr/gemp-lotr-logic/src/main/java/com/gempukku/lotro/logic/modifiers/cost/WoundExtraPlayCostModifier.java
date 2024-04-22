package com.gempukku.lotro.logic.modifiers.cost;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.timing.PlayConditions;

public class WoundExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int count;
    private final Filterable[] wounding;

    public WoundExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, Filterable... wounding) {
        this(source, affects, condition, 1, wounding);
    }

    public WoundExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, int count, Filterable... wounding) {
        super(source, "Wound to play", Filters.changeToFilter(affects), condition);
        this.count = count;
        this.wounding = wounding;
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return PlayConditions.canWound(card, game, 1, count, wounding);
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        action.appendCost(
                new ChooseAndWoundCharactersEffect(action, card.getOwner(), count, count, wounding));
    }
}
