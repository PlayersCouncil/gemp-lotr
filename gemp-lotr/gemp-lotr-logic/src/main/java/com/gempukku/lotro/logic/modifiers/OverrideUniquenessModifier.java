package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class OverrideUniquenessModifier extends AbstractModifier {
    private final int _uniqueness;

    public OverrideUniquenessModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int uniqueness) {
        super(source, "Uniqueness overridden to " + uniqueness, affectFilter, condition, ModifierEffect.UNIQUENESS_MODIFIER);
        _uniqueness = uniqueness;
    }

    @Override
    public int getOverrideUniqueness(LotroGame game, PhysicalCard card) { return _uniqueness; }
}
