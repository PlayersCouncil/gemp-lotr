package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantReplaceSiteModifier extends AbstractModifier {
    private final String bannedPlayer;

    public CantReplaceSiteModifier(PhysicalCard source, Condition condition, String bannedPlayer, Filterable affectFilter) {
        super(source, "Can't be replaced", affectFilter, condition, ModifierEffect.REPLACE_SITE_MODIFIER);
        this.bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean isSiteReplaceable(LotroGame game, String playerId) {
        if (bannedPlayer == null || bannedPlayer.equals(playerId))
            return false;
        return true;
    }
}
