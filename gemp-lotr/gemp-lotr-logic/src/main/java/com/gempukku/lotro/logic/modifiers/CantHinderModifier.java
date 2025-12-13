package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantHinderModifier extends AbstractModifier {
    private final String _bannedPlayer;
    private final Filter _sourceFilter;

    public CantHinderModifier(PhysicalCard source, String text, Condition condition, String bannedPlayer, Filterable affectFilter, Filterable sourceFilter) {
        super(source, text, affectFilter, condition, ModifierEffect.HINDERED_MODIFER);
        _bannedPlayer = bannedPlayer;
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    public boolean canBeHinderedBy(LotroGame game, String performingPlayer, PhysicalCard source) {

        if (_sourceFilter.accepts(game, source))
            return false;

        if (_bannedPlayer != null && _bannedPlayer.equals(performingPlayer))
            return false;

        return true;
    }
}
