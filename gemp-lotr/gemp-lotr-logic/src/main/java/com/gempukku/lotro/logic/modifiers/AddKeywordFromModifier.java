package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.HashSet;

public class AddKeywordFromModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Filterable from;
    private final boolean terrainOnly;
    private final boolean unloadedOnly;
    private boolean counting;

    public AddKeywordFromModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Filterable from, boolean terrainOnly, boolean unloadedOnly) {
        super(physicalCard, "Has added keywords from other cards", affectFilter, condition, ModifierEffect.GIVE_KEYWORD_MODIFIER);
        this.from = from;
        this.terrainOnly = terrainOnly;
        this.unloadedOnly = unloadedOnly;
    }

    @Override
    public HashSet<Keyword> getKeywords() {
        return null;
    }

    @Override
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (terrainOnly && !keyword.isTerrain())
            return false;
        if (unloadedOnly && !keyword.isUnloaded())
            return false;

        //If the card we are copying is active then we use the normal active checking
        if(Filters.hasActive(game, from, keyword))
            return true;

        //If the card we are copying is inactive (from a stack or other source), we do not want
        // to actually copy modifiers, only printed blueprint keywords.
        var fromCards = Filters.filterActive(game, SpotOverride.INCLUDE_STACKED, from, keyword);

        for(var card : fromCards) {
            if(card.getBlueprint().hasKeyword(keyword))
                return true;
        }

        return false;
    }

    // A keyword with a number is gained with its number (CR 4.2 "game text": a boldfaced keyword in the text box,
    // number included, is game text; Ketura on F-0126): a copied "Ambush (2)" counts 2 and a copied
    // "Damage +1" counts 1 beside whatever the card has of its own.  A keyword without a number counts 1.
    @Override
    public int getKeywordCountModifier(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (!hasKeyword(game, physicalCard, keyword))
            return 0;
        // The second half of the test stops two cards that copy each other from asking each other for ever.
        if (!keyword.isMultiples() || counting)
            return 1;

        counting = true;
        try {
            int total = 0;
            for (var card : Filters.filterActive(game, SpotOverride.INCLUDE_STACKED, from)) {
                if (card == physicalCard)
                    continue;
                // As in hasKeyword: an active card is copied as it stands, an inactive one (stacked) as printed.
                if (card.getZone() == Zone.STACKED)
                    total += card.getBlueprint().getKeywordCount(keyword);
                else
                    total += game.getModifiersQuerying().getKeywordCount(game, card, keyword);
            }
            return Math.max(1, total);
        } finally {
            counting = false;
        }
    }
}
