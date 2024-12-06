package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class CantPlayCardsModifier extends AbstractModifier {
    private final Filter _filters;

    public CantPlayCardsModifier(PhysicalCard source, Condition condition, Filterable... filters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _filters = Filters.and(filters);
    }

    @Override
    public boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card) {
        return !_filters.accepts(game, card);
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        final PhysicalCard actionSource = action.getActionSource();
        if (actionSource != null)
            if (action.getType() == Action.Type.PLAY_CARD)
                if (_filters.accepts(game, actionSource))
                    return false;
        return true;
    }
}
