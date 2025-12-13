package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class RemoveKeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final HashSet<Keyword> _keywords =  new HashSet<>();

    public RemoveKeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword) {
        this(physicalCard, affectFilter, condition, Collections.singleton(keyword));
    }

    public RemoveKeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Collection<Keyword> keywords) {
        super(physicalCard,
                "Loses " + String.join(", ", keywords.stream().map(Keyword::getHumanReadable).toList().toArray(new String[0])) + " keyword(s)",
                affectFilter, condition, ModifierEffect.REMOVE_KEYWORD_MODIFIER);
        _keywords.addAll(keywords);
    }

    @Override
    public HashSet<Keyword> getKeywords() {
        return _keywords;
    }

    @Override
    public boolean isKeywordRemoved(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return _keywords.contains(keyword);
    }
}
