package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

public class AddKeywordFromModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Filterable from;
    private final boolean terrainOnly;
    private final boolean unloadedOnly;
    private boolean counting;
    // The keywords taken once, with their numbers, or null where they are read from the cards as they stand.
    private Map<Keyword, Integer> snapshot;

    public AddKeywordFromModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Filterable from, boolean terrainOnly, boolean unloadedOnly) {
        super(physicalCard, "Has added keywords from other cards", affectFilter, condition, ModifierEffect.GIVE_KEYWORD_MODIFIER);
        this.from = from;
        this.terrainOnly = terrainOnly;
        this.unloadedOnly = unloadedOnly;
    }

    /**
     * Takes the keywords now, numbers included, and gives those from here on, whatever becomes of the cards they
     * were read from: "until the regroup phase, each of your other Men gain each of the unloaded keywords on
     * that Man" memorizes what was on that Man when the ability was used (Ketura on Q-0096), so the others keep
     * them when he is killed or hindered.  Without this the cards are read as they stand each time, which is what
     * a standing "while ..., each site gains each terrain keyword from each site you control" wants.
     */
    public AddKeywordFromModifier takeSnapshot(LotroGame game) {
        final Map<Keyword, Integer> taken = new EnumMap<>(Keyword.class);
        for (Keyword keyword : Keyword.values()) {
            if (hasKeyword(game, null, keyword))
                taken.put(keyword, getKeywordCountModifier(game, null, keyword));
        }
        snapshot = taken;
        return this;
    }

    @Override
    public HashSet<Keyword> getKeywords() {
        return snapshot != null ? new HashSet<>(snapshot.keySet()) : null;
    }

    @Override
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (snapshot != null)
            return snapshot.containsKey(keyword);

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
        if (snapshot != null)
            return snapshot.getOrDefault(keyword, 0);

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
