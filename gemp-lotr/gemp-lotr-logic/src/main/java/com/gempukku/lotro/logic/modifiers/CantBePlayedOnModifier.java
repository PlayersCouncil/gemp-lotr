package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantBePlayedOnModifier extends AbstractModifier {
    private final Filterable _unplayableCardFilter;

    public CantBePlayedOnModifier(PhysicalCard source, Condition condition, Filterable affectFilter, Filterable unplayableCardFilter) {
        super(source, "Affected by \"cannot be played on\" limitation", affectFilter, condition, ModifierEffect.TARGET_MODIFIER);
        _unplayableCardFilter = unplayableCardFilter;
    }

    @Override
    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !Filters.accepts(game, playedCard, _unplayableCardFilter);
    }
}
