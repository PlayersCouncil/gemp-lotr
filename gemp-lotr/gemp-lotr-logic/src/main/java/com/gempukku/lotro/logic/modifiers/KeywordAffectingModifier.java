package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Keyword;

import java.util.HashSet;

public interface KeywordAffectingModifier {
    HashSet<Keyword> getKeywords();
}
