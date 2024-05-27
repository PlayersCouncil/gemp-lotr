package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class RemoveKeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;

    public RemoveKeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword) {
        super(physicalCard, "Loses " + keyword.getHumanReadable() + " keyword(s)", affectFilter, condition, ModifierEffect.REMOVE_KEYWORD_MODIFIER);
        _keyword = keyword;
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }

    @Override
    public boolean isKeywordRemoved(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return _keyword == keyword;
    }
}
