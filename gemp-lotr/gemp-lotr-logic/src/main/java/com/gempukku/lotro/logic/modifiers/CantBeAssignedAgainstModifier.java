package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Set;

public class CantBeAssignedAgainstModifier extends AbstractModifier {
    private final Side _side;
    private final Filter _minionFilter;

    public CantBeAssignedAgainstModifier(PhysicalCard source, Side side, Filterable characterFilter, Condition condition, Filterable minionFilter) {
        super(source, "Is affected by assignment restriction", characterFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
        _side = side;
        _minionFilter = Filters.changeToFilter(minionFilter);
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side side, PhysicalCard companion, Set<PhysicalCard> minions) {
        if ((_side == null || side == _side) && Filters.filter(minions, game, _minionFilter).size() > 0)
            return false;

        return true;
    }
}
