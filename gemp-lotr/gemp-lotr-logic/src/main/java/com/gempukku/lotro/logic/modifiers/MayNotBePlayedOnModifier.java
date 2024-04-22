package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class MayNotBePlayedOnModifier extends AbstractModifier {
    private final Filterable _unplayableCardFilter;

    public MayNotBePlayedOnModifier(PhysicalCard source, Filterable affectFilter, Filterable unplayableCardFilter) {
        this(source, null, affectFilter, unplayableCardFilter);
    }

    public MayNotBePlayedOnModifier(PhysicalCard source, Condition condition, Filterable affectFilter, Filterable unplayableCardFilter) {
        super(source, "Affected by \"may not be played on\" limitation", affectFilter, condition, ModifierEffect.TARGET_MODIFIER);
        _unplayableCardFilter = unplayableCardFilter;
    }

    @Override
    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !Filters.accepts(game, _unplayableCardFilter, playedCard);
    }
}
