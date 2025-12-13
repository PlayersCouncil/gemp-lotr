package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantBearModifier extends AbstractModifier {
    private final Filter _unbearableCardFilter;

    public CantBearModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Filterable... unbearableCardFilter) {
        super(source, "Affected by \"cannot bear\" limitation", affectFilter, condition, ModifierEffect.TARGET_MODIFIER);
        _unbearableCardFilter = Filters.and(unbearableCardFilter);
    }

    @Override
    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !_unbearableCardFilter.accepts(game, playedCard);
    }

    @Override
    public boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !_unbearableCardFilter.accepts(game, playedCard);
    }
}
