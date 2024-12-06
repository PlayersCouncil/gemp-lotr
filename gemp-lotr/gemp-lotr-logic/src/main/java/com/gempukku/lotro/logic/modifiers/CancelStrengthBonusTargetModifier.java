package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class CancelStrengthBonusTargetModifier extends AbstractModifier {
    private final Filter _sourceFilter;

    public CancelStrengthBonusTargetModifier(PhysicalCard source, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, "Has some strength bonuses cancelled", affectFilter, condition, ModifierEffect.STRENGTH_BONUS_MODIFIER);
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    @Override
    public boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget) {
        if (_sourceFilter == null || (modifierSource != null && _sourceFilter.accepts(game, modifierSource)))
            return false;
        return true;
    }
}
