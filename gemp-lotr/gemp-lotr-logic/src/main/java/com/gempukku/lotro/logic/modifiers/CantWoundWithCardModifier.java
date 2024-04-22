package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;

public class CantWoundWithCardModifier extends AbstractModifier {
    private final Filter _preventWoundWithFilter;

    public CantWoundWithCardModifier(PhysicalCard source, Filterable affectFilter, Filterable preventWoundWithFilter) {
        this(source, affectFilter, null, preventWoundWithFilter);
    }

    public CantWoundWithCardModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Filterable preventWoundWithFilter) {
        super(source, "Affected by wound preventing effect", affectFilter, condition, ModifierEffect.WOUND_MODIFIER);
        _preventWoundWithFilter = Filters.changeToFilter(preventWoundWithFilter);
    }

    @Override
    public boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        for (PhysicalCard woundSource : woundSources) {
            if (_preventWoundWithFilter.accepts(game, woundSource))
                return false;
        }

        return true;
    }
}
