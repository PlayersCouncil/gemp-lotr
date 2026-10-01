package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class OverrideUniquenessModifier extends AbstractModifier {
    private final int _uniqueness;
    private final boolean _loosen;

    public OverrideUniquenessModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int uniqueness) {
        this(source, affectFilter, condition, uniqueness, false);
    }

    public OverrideUniquenessModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int uniqueness, boolean loosen) {
        super(source, "Uniqueness " + (loosen ? "loosened" : "restricted") + " to " + uniqueness, affectFilter, condition, ModifierEffect.UNIQUENESS_MODIFIER);
        _uniqueness = uniqueness;
        _loosen = loosen;
    }

    @Override
    public int getOverrideUniqueness(LotroGame game, PhysicalCard card) { return _uniqueness; }

    @Override
    public boolean loosensUniqueness(LotroGame game, PhysicalCard card) { return _loosen; }
}
