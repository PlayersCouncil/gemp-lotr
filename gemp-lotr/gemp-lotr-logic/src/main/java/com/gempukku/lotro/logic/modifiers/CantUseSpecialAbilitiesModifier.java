package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class CantUseSpecialAbilitiesModifier extends AbstractModifier {
    private final Phase phase;
    private final String bannedPlayer;
    private final Filter sourceFilters;

    public CantUseSpecialAbilitiesModifier(PhysicalCard source, Condition condition, Phase phase, String bannedPlayer, Filterable... sourceFilters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        this.phase = phase;
        this.bannedPlayer = bannedPlayer;
        this.sourceFilters = Filters.and(sourceFilters);
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        if (action.getType() == Action.Type.SPECIAL_ABILITY
                && (phase == null || action.getActionTimeword() == phase)
                && (bannedPlayer == null || performingPlayer.equals(bannedPlayer))
                && sourceFilters.accepts(game, action.getActionSource()))
            return false;
        return true;
    }
}
