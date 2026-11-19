package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;
import java.util.List;

public class DuplicateGameTextModifier extends AbstractModifier {
    private final Filter _duplicatingFilter;
    // The cards whose text was taken once, or null where they are looked for each time the text is read.
    private Collection<PhysicalCard> _snapshot;

    public DuplicateGameTextModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Filterable... duplicatingFilter) {
        super(source, "Duplicating game text from card", affectFilter, condition, ModifierEffect.GAME_TEXT_DUPLICATE);
        _duplicatingFilter = Filters.and(duplicatingFilter);
    }

    /**
     * Remembers now which cards the text is taken from, and gives the text of those from here on, whatever becomes
     * of them: "until the end of the turn, that Southron gains the game text of that stacked Man" keeps the text
     * "even if the Man is later lost" (Ketura, 2026-10-07, on War Howdah 103_62, F-0160).  A card's game text is
     * its blueprint's and a card keeps its blueprint wherever it goes, so remembering the cards is enough.
     * Without this the cards are looked for each time, and the text is lost when they leave play or their stack.
     */
    public DuplicateGameTextModifier takeSnapshot(LotroGame game) {
        _snapshot = List.copyOf(Filters.filterActive(game, SpotOverride.INCLUDE_STACKED, _duplicatingFilter));
        return this;
    }

    public Collection<PhysicalCard> getCardsToDuplicate(LotroGame game) {
        if (_snapshot != null)
            return _snapshot;
        return Filters.filterActive(game, SpotOverride.INCLUDE_STACKED, _duplicatingFilter);
    }

}
