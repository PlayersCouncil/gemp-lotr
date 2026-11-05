package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

/**
 * A bar on healing the cards it affects.  With no banned player it stops every heal of them, whoever makes it
 * ("bearer cannot heal").  With one it stops that player's heals alone ("you may heal no more than ..."), and the
 * other player heals the same cards as before.  The bar holds for a heal made by a card its source filter accepts
 * (Filters.any, every card, unless one is given) or by no card; a heal by any other card goes through: "wounds
 * cannot be healed ... (except by Wraiths)" is a source filter of not(wraith) (Spirit of Dread 16_5; Ketura's
 * ruling, Q-0357).
 */
public class CantHealModifier extends AbstractModifier {
    private final String _bannedPlayer;
    private final Filter _sourceFilter;

    public CantHealModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        this(source, condition, null, Filters.any, affectFilters);
    }

    public CantHealModifier(PhysicalCard source, Condition condition, String bannedPlayer, Filterable sourceFilter, Filterable... affectFilters) {
        super(source, "Can't be healed", Filters.and(affectFilters), condition, ModifierEffect.WOUND_MODIFIER);
        _bannedPlayer = bannedPlayer;
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    @Override
    public boolean canBeHealed(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard healSource) {
        if (healSource != null && !_sourceFilter.accepts(game, healSource))
            return true;
        return _bannedPlayer != null && !_bannedPlayer.equals(performingPlayer);
    }
}
