package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;

import java.util.List;

public abstract class AddActionToCardModifier extends AbstractModifier {
    public AddActionToCardModifier(PhysicalCard source, Condition condition, Filterable... affectFilter) {
        super(source, "Has extra action from " + GameUtils.getFullName(source), Filters.and(affectFilter), condition, ModifierEffect.EXTRA_ACTION_MODIFIER);
    }

    @Override
    public List<? extends ActivateCardAction> getExtraPhaseAction(LotroGame game, PhysicalCard card) {
        return createExtraPhaseActions(game, card);
    }

    protected abstract List<? extends ActivateCardAction> createExtraPhaseActions(LotroGame game, PhysicalCard matchingCard);
}
