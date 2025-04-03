package com.gempukku.lotro.draft3.evaluator;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractDraftCardEvaluator {
    private static final int RARES_IN_PACK = 1;
    private static final int UNCOMMONS_IN_PACK = 3;
    private static final int COMMONS_IN_PACK = 7;

    private final LotroCardBlueprintLibrary library;

    public AbstractDraftCardEvaluator(LotroCardBlueprintLibrary library) {
        this.library = library;
    }

    public abstract Map<String, Double> getAverageStartingCardMap();

    // This is based on what I cut cards from draft pool
    public final Map<String, Double> getPlayRateMap(Map<String, Integer> countMap, int gamesAnalyzed) {
        return countMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().doubleValue() / (gamesAnalyzed * 2))); // Every game has two decks
    }


    // This is what bots pick according to
    public final Map<String, Double> getValuesMap(Map<String, Integer> winningMap, Map<String, Integer> losingMap, int gamesAnalyzed) {
        // Merge win and lose maps (they are kept separate for potential wr info)
        Map<String, Integer> mergedMap = mergeMaps(winningMap, losingMap);

        // Lower count of cards from Draft Packs
        Map<String, Double> startingCollectionNormalizedCardCountMap = normalizeCountByDraftPackChance(mergedMap, gamesAnalyzed);

        // Normalize for uneven set size
        Map<String, Double> setNormalizedCardCountMap = normalizeCountBySetSize(startingCollectionNormalizedCardCountMap);

        // Normalize count based on rarities
        Map<String, Double> normalizedCardCountMap = normalizeCountByRarity(setNormalizedCardCountMap, library);

        // Boost rarities (more likely to pick rares and uncommons)
        Map<String, Double> rareInflatedMap = inflateRarity(normalizedCardCountMap, library);

        // Boost FP cards (more likely to pick FP card in mixed draft)
        Map<String, Double> fpInflatedMap = inflateFp(rareInflatedMap, library);

        // Make sure all values are positive and in 0-1 range
        Map<String, Double> shiftedMap = shift(fpInflatedMap);

        return shiftedMap;
    }

    private static Map<String, Integer> mergeMaps(Map<String, Integer> map1, Map<String, Integer> map2) {
        Map<String, Integer> result = new HashMap<>(map1);

        map2.forEach((key, value) -> result.merge(key, value, Integer::sum));

        return result;
    }

    private Map<String, Double> normalizeCountByDraftPackChance(Map<String, Integer> map, int gamesAnalyzed) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> {
            double cardsGiven = gamesAnalyzed * 2 * getAverageStartingCardMap().getOrDefault(key, 0.0);
            normalizedMap.put(key, value.doubleValue() - (cardsGiven / 1.5)); // Assume 75 % of assigned cards are played, rest is drafted
        });

        return normalizedMap;
    }

    private Map<String, Double> normalizeCountBySetSize(Map<String, Double> map) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> {
            if (key.startsWith("2_") || key.startsWith("3_") || key.startsWith("5_") || key.startsWith("6_")) {
                normalizedMap.put(key, value / 3.0); // MoM and RotEL are 1/3 of FotR set
            } else {
                normalizedMap.put(key, value);
            }
        });

        return normalizedMap;
    }

    private Map<String, Double> normalizeCountByRarity(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.entrySet().forEach(entry -> {
            // Do not add P cards
            try {
                if (library.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity.equals("P")) {
                    return;
                }
            } catch (CardNotFoundException ignored) {

            }
            normalizedMap.put(entry.getKey(), getRarityNormalizedValue(library, entry));
        });

        return normalizedMap;
    }

    private double getRarityNormalizedValue(LotroCardBlueprintLibrary library, Map.Entry<String, Double> entry) {
        double rarityInPack = 1.0;
        try {
            rarityInPack = switch (library.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity) {
                case "R" -> RARES_IN_PACK;
                case "U" -> UNCOMMONS_IN_PACK;
                case "C" -> COMMONS_IN_PACK;
                default -> 1.0; // Fallback for unknown cards and promos
            };
        } catch (CardNotFoundException ignore) {

        }

        // Normalize by expected rarity frequency
        return entry.getValue() / rarityInPack;
    }

    private Map<String, Double> inflateRarity(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> rarityInflatedMap = new HashMap<>();

        map.forEach((key, value) -> {
            try {
                int rarityInPack = switch (library.getLotroCardBlueprint(key).getCardInfo().rarity) {
                    case "R" -> RARES_IN_PACK;
                    case "U" -> UNCOMMONS_IN_PACK;
                    case "C" -> COMMONS_IN_PACK;
                    default -> 1; // Fallback for unknown cards and promos
                };
                rarityInflatedMap.put(key, value / rarityInPack);
            } catch (CardNotFoundException e) {
                rarityInflatedMap.put(key, value);
            }
        });
        return rarityInflatedMap;
    }

    private Map<String, Double> inflateFp(Map<String, Double> map, LotroCardBlueprintLibrary library) {
        Map<String, Double> fpInflatedMap = new HashMap<>();

        map.forEach((key, value) -> {
            try {
                double sideFactor = Side.FREE_PEOPLE.equals(library.getLotroCardBlueprint(key).getSide()) ? 1.1 : 1.0;
                fpInflatedMap.put(key, value * sideFactor);
            } catch (CardNotFoundException e) {
                fpInflatedMap.put(key, value);
            }
        });
        return fpInflatedMap;
    }

    private Map<String, Double> shift(Map<String, Double> map) {
        double min = map.values().stream().min(Double::compareTo).orElse(0.0);

        double positiveShift;
        if (min < 0) {
            positiveShift = -min + 5;
        } else {
            positiveShift = 5;
        }

        Map<String, Double> positiveMap = new HashMap<>();
        map.forEach((key, value) -> positiveMap.put(key, value + positiveShift));

        double max = positiveMap.values().stream().max(Double::compareTo).orElse(1.0);
        Map<String, Double> tbr = new HashMap<>();
        positiveMap.forEach((key, value) -> tbr.put(key, value / max));

        return tbr;
    }
}
