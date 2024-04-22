package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantDiscardFromPlayModifier extends AbstractModifier {
    private final Filter _sourceFilter;

    public CantDiscardFromPlayModifier(PhysicalCard source, String text, Filterable affectFilter, Filterable sourceFilter) {
        this(source, text, null, affectFilter, sourceFilter);
    }

    public CantDiscardFromPlayModifier(PhysicalCard source, String text, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, text, affectFilter, condition, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER);
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    @Override
    public boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        if (_sourceFilter.accepts(game, source))
            return false;
        return true;
    }
}
