package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantReturnToHandModifier extends AbstractModifier {
    private final Filter _sourceFilter;

    public CantReturnToHandModifier(PhysicalCard source, String text, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, text, affectFilter, condition, ModifierEffect.RETURN_TO_HAND_MODIFIER);
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    @Override
    public boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source) {
        if (_sourceFilter.accepts(game, source))
            return false;
        return true;
    }
}