package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RaceSpotModifier extends AbstractModifier {
    private final Race _race;

    public RaceSpotModifier(PhysicalCard source, Condition condition, Race race) {
        super(source, "Spotting modifier", null, condition, ModifierEffect.SPOT_MODIFIER);
        _race = race;
    }


    /**
     * "Add 1 to the number of ... you can spot": "you" is the owner of the card that says it, and no other player's
     * count is raised (F-0502).
     */
    @Override
    public int getSpotCountModifier(LotroGame game, Filterable filter, String spottingPlayer) {
        if (filter == _race && getSource().getOwner().equals(spottingPlayer))
            return 1;
        return 0;
    }
}