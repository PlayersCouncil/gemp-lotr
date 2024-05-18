package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class PlayerCantUseCardSpecialAbilitiesModifier extends AbstractModifier {
    private final String playerId;
    private final Filter _sourceFilters;

    public PlayerCantUseCardSpecialAbilitiesModifier(PhysicalCard source, String playerId, Condition condition, Filterable... sourceFilters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        this.playerId = playerId;
        _sourceFilters = Filters.and(sourceFilters);
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        if (action.getType() == Action.Type.SPECIAL_ABILITY
                && _sourceFilters.accepts(game, action.getActionSource())
                && performingPlayer.equals(playerId))
            return false;
        return true;
    }
}
