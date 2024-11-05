package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class AddKeywordFromModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Filterable from;
    private final boolean terrainOnly;

    public AddKeywordFromModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Filterable from, boolean terrainOnly) {
        super(physicalCard, "Has added keywords from other cards", affectFilter, condition, ModifierEffect.GIVE_KEYWORD_MODIFIER);
        this.from = from;
        this.terrainOnly = terrainOnly;
    }

    @Override
    public Keyword getKeyword() {
        return null;
    }

    @Override
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (terrainOnly && !keyword.isTerrain())
            return false;
        return Filters.hasActive(game, from, keyword);
    }

    @Override
    public int getKeywordCountModifier(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (hasKeyword(game, physicalCard, keyword))
            return 1;
        else
            return 0;
    }
}
