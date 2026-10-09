package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

/**
 * A bar on healing the cards it affects.  With no banned player it stops every heal of them, whoever makes it
 * ("bearer cannot heal").  With one it stops that player's heals alone ("you may heal no more than ..."), and the
 * other player heals the same cards as before.
 */
public class CantHealModifier extends AbstractModifier {
    private final String _bannedPlayer;

    public CantHealModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        this(source, condition, null, affectFilters);
    }

    public CantHealModifier(PhysicalCard source, Condition condition, String bannedPlayer, Filterable... affectFilters) {
        super(source, "Can't be healed", Filters.and(affectFilters), condition, ModifierEffect.WOUND_MODIFIER);
        _bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean canBeHealed(LotroGame game, String performingPlayer, PhysicalCard card) {
        return _bannedPlayer != null && !_bannedPlayer.equals(performingPlayer);
    }
}
