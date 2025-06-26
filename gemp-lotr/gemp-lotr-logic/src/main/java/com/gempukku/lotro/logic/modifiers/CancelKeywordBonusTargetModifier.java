package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.HashSet;

public class CancelKeywordBonusTargetModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;
    private final Filter _sourceFilter;

    public CancelKeywordBonusTargetModifier(PhysicalCard source, Keyword keyword, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, "Cancel " + keyword.getHumanReadable() + " keyword", affectFilter, condition, ModifierEffect.CANCEL_KEYWORD_BONUS_TARGET_MODIFIER);
        _keyword = keyword;
        _sourceFilter = Filters.changeToFilter(sourceFilter);
    }

    @Override
    public boolean appliesKeywordModifier(LotroGame game, PhysicalCard modifierSource, Keyword keyword) {
        if ((_keyword == null || keyword == _keyword)
                && (_sourceFilter == null || (modifierSource != null && _sourceFilter.accepts(game, modifierSource))))
            return false;
        return true;
    }

    @Override
    public HashSet<Keyword> getKeywords() {
        return new HashSet<>(){{ add(_keyword); }};
    }
}