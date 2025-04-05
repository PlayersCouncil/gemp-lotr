package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CantBeOverwhelmedModifier extends AbstractModifier {

    public static int ImmuneToOverwhelmThreshold = 10_000;

    public CantBeOverwhelmedModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Cannot be overwhelmed", affectFilter, condition, ModifierEffect.OVERWHELM_MODIFIER);
    }

    @Override
    public int getOverwhelmMultiplier(LotroGame game, PhysicalCard physicalCard) {
        return ImmuneToOverwhelmThreshold;
    }
}
