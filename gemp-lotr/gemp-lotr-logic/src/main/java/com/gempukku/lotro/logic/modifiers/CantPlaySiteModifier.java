package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantPlaySiteModifier extends AbstractModifier {
    private final String bannedPlayer;

    public CantPlaySiteModifier(PhysicalCard source, Condition condition, String bannedPlayer) {
        super(source, "Can't be replaced by Free Peoples player", null, condition, ModifierEffect.PLAY_SITE_MODIFIER);
        this.bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean canPlaySite(LotroGame game, String playerId) {
        if (playerId.equals(bannedPlayer))
            return false;
        return true;
    }
}