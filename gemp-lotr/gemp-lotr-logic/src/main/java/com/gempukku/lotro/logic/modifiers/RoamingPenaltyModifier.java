package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RoamingPenaltyModifier extends AbstractModifier {
    private final int _modifier;

    public RoamingPenaltyModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        super(source, "Roaming penalty " + ((modifier > 0) ? ("+" + modifier) : modifier), affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _modifier = modifier;
    }

    @Override
    public int getRoamingPenaltyModifier(LotroGame game, PhysicalCard physicalCard) {
        return _modifier;
    }
}
