package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;

public class DuplicateGameTextModifier extends AbstractModifier {
    private final Filter _duplicatingFilter;

    public DuplicateGameTextModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Filterable... duplicatingFilter) {
        super(source, "Duplicating game text from card", affectFilter, condition, ModifierEffect.GAME_TEXT_DUPLICATE);
        _duplicatingFilter = Filters.and(duplicatingFilter);
    }

   public Collection<PhysicalCard> getCardsToDuplicate(LotroGame game) {
        return Filters.filterActive(game, SpotOverride.INCLUDE_STACKED, _duplicatingFilter);
   }

}
