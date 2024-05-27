package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class LoseAllKeywordsModifier extends AbstractModifier {
    public LoseAllKeywordsModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Loses all keywords", affectFilter, condition, ModifierEffect.LOSE_ALL_KEYWORDS_MODIFIER);
    }

    @Override
    public boolean lostAllKeywords(LotroGame game, PhysicalCard card) {
        return true;
    }
}
