package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
    public static Map<String, Integer> getTotalCardCountForDeck(LotroDeck deck) {
        Map<String, Integer> counts = new HashMap<>();
        String ring = deck.getRing();
        if (ring != null)
            incrementCardCount(counts, ring, 1);
        String ringBearer = deck.getRingBearer();
        if (ringBearer != null)
            incrementCardCount(counts, ringBearer, 1);
        for (String site : deck.getSites())
            incrementCardCount(counts, site, 1);
        for (String adventureCard : deck.getDrawDeckCards())
            incrementCardCount(counts, adventureCard, 1);
        return counts;
    }

    public static Map<String, Integer> getTotalCardCount(List<String> cards) {
        Map<String, Integer> counts = new HashMap<>();
        for (String card : cards)
            incrementCardCount(counts, card, 1);
        return counts;
    }

    private static void incrementCardCount(Map<String, Integer> map, String blueprintId, int incrementBy) {
        final Integer oldCount = map.get(blueprintId);
        if (oldCount == null)
            map.put(blueprintId, incrementBy);
        else
            map.put(blueprintId, oldCount + incrementBy);
    }
}
