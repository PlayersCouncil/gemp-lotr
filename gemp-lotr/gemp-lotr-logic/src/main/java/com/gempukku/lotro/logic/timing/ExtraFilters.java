package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;

public class ExtraFilters {
    public static Filter attachableTo(final LotroGame game, final int twilightModifier, final Filterable... filters) {
        return Filters.and(Filters.playable(game, twilightModifier),
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        if (physicalCard.getBlueprint().getValidTargetFilter(physicalCard.getOwner(), game, physicalCard) == null)
                            return false;
                        return PlayUtils.checkPlayRequirements(game, physicalCard, Filters.and(filters), 0, twilightModifier, false, false, true);
                    }
                });
    }
}
