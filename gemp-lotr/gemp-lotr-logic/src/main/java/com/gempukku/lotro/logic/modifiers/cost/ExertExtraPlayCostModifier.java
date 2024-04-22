package com.gempukku.lotro.logic.modifiers.cost;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.timing.PlayConditions;

public class ExertExtraPlayCostModifier extends AbstractExtraPlayCostModifier {
    private final int count;
    private final Filterable[] exerting;

    public ExertExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, Filterable ...exerting) {
        this(source, affects, condition, 1, exerting);
    }

    public ExertExtraPlayCostModifier(PhysicalCard source, Filterable affects, Condition condition, int count, Filterable ...exerting) {
        super(source, "Exert to play", Filters.changeToFilter(affects), condition);
        this.count = count;
        this.exerting = exerting;
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return PlayConditions.canExert(card, game, 1, count, exerting);
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, card.getOwner(), count, count, exerting));
    }
}
