package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * "Unable to gain game text keywords" (Into the Caves 17_100; Q-0282: A): the cards keep the game text keywords
 * they have as the bar is laid, and gain no other and no more of one, from their own text or from any other card.
 * What each card has is taken when the modifier is made, so it is made at the moment the bar is laid (the effect
 * word `cantGainKeywords`), never as a card's standing text. ModifiersLogic holds hasKeyword and getKeywordCount
 * to it (getKeywordGainLimit); a keyword it had and has since lost may come back up to what it had. Only the game
 * text keywords are held (Keyword.isRealKeyword: roaming is not one), as for Éowyn 17_96's removeAllKeywords.
 */
public class CantGainKeywordsModifier extends AbstractModifier {
    private final Map<Integer, Map<Keyword, Integer>> _heldByCardId = new HashMap<>();

    public CantGainKeywordsModifier(PhysicalCard source, LotroGame game, Collection<? extends PhysicalCard> cards) {
        super(source, "Cannot gain keywords", Filters.in(cards), null, ModifierEffect.CANT_GAIN_KEYWORDS_MODIFIER);
        for (PhysicalCard card : cards) {
            Map<Keyword, Integer> held = new EnumMap<>(Keyword.class);
            for (Keyword keyword : Keyword.values()) {
                if (!keyword.isRealKeyword())
                    continue;
                int count = keyword.isMultiples()
                        ? game.getModifiersQuerying().getKeywordCount(game, card, keyword)
                        : (game.getModifiersQuerying().hasKeyword(game, card, keyword) ? 1 : 0);
                if (count > 0)
                    held.put(keyword, count);
            }
            _heldByCardId.put(card.getCardId(), held);
        }
    }

    @Override
    public int getKeywordGainLimit(LotroGame game, PhysicalCard card, Keyword keyword) {
        Map<Keyword, Integer> held = _heldByCardId.get(card.getCardId());
        if (held == null)
            return Integer.MAX_VALUE;
        return held.getOrDefault(keyword, 0);
    }
}
