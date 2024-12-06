package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class OverwhelmedByMultiplierModifier extends AbstractModifier {
    private final int _multiplier;

    public OverwhelmedByMultiplierModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int multiplier) {
        super(source, "Cannot be overwhelmed unless his strength is *" + multiplier, affectFilter, condition, ModifierEffect.OVERWHELM_MODIFIER);
        _multiplier = multiplier;
    }

    @Override
    public int getOverwhelmMultiplier(LotroGame game, PhysicalCard physicalCard) {
        return _multiplier;
    }
}
