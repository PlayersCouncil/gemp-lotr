package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.ExtraPlayCost;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

public abstract class AbstractExtraPlayCostModifier extends AbstractModifier implements ExtraPlayCost {
    public AbstractExtraPlayCostModifier(PhysicalCard source, String text, Filterable affectFilter, Condition condition) {
        super(source, text, affectFilter, condition, ModifierEffect.EXTRA_COST_MODIFIER);
    }

    @Override
    public abstract void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card);

    @Override
    public abstract boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card);
}
