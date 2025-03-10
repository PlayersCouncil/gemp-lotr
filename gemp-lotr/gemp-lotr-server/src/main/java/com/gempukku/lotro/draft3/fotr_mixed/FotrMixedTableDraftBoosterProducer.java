package com.gempukku.lotro.draft3.fotr_mixed;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.Booster;
import com.gempukku.lotro.draft3.BoosterProducer;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.*;
import java.util.stream.Collectors;

public class FotrMixedTableDraftBoosterProducer implements BoosterProducer {
    private static final int MAX_ROUND = 6;

    private static final String RARE_FILTER = "rarity:R";
    private static final String UNCOMMON_FILTER = "rarity:U"; // No P rarity cards in boosters
    private static final String COMMON_FILTER = "rarity:C";
    private static final String FOTR_BLOCK_FILTER = "set:1,2,3";
    private static final String FP_FILTER = "side:FREE_PEOPLE";
    private static final String SHADOW_FILTER = "side:SHADOW";

    private final Map<String, List<String>> cardPools = new HashMap<>();

    public FotrMixedTableDraftBoosterProducer(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                              LotroFormatLibrary formatLibrary, Map<String, Double> cardPlayRates) {
        SortAndFilterCards sortAndFilterCards = new SortAndFilterCards();

        List<String> rarities = List.of("rare", "uncommon", "common");
        List<String> types = List.of("fp", "shadow");

        Map<String, String> filters = Map.of(
                "fp", FP_FILTER,
                "shadow", SHADOW_FILTER,
                "rare", RARE_FILTER,
                "uncommon", UNCOMMON_FILTER,
                "common", COMMON_FILTER
        );

        // Make 18 different lists of cards for combinations of rarity, set and side
            for (String rarity : rarities) {
                for (String type : types) {
                    String key = rarity + "-" + type;
                    List<String> filterSet = List.of(FOTR_BLOCK_FILTER, filters.get(rarity), filters.get(type));

                    cardPools.put(key, sortAndFilterCards.process(
                            joinFilters(filterSet),
                            collectionsManager.getCompleteCardCollection().getAll(),
                            cardLibrary,
                            formatLibrary
                    ).stream().map(CardCollection.Item::getBlueprintId).collect(Collectors.toList()));
                    // Remove alternate versions from different sets
                    cardPools.get(key).removeIf(cardId -> !cardId.startsWith("1_") && !cardId.startsWith("2_") && !cardId.startsWith("3_"));
                }
            }

        // Remove bad cards from pools to make boosters better
        cardPools.forEach((key, value) -> {
            if (key.startsWith("common") || key.startsWith("uncommon")) {
                value.removeIf(cardId -> !cardPlayRates.containsKey(cardId) || cardPlayRates.get(cardId) < 0.01); // (Un)common has really low play rate
            }
            if (key.startsWith("rare")) {
                value.removeIf(cardId -> !cardPlayRates.containsKey(cardId)); // Rare was never played
            }
        });

        // Add the rare ring to draft manually - it is neither fp nor shadow card
        cardPools.get("rare-fp").add("1_1");
    }

    @Override
    public Booster getBooster(int round) {
        if (round < 1 || round > MAX_ROUND) {
            return null;
        }
        String type = round <= 3 ? "fp" : "shadow";

        // Get the correct lists
        List<String> rareCards = cardPools.get("rare-" + type);
        List<String> uncommonCards = cardPools.get("uncommon-" + type);
        List<String> commonCards = cardPools.get("common-" + type);

        // Pick cards
        List<String> pickedCards = pickRandom(rareCards, 1);
        pickedCards.addAll(pickRandom(uncommonCards, 3));
        pickedCards.addAll(pickRandom(commonCards, 7));

        return new Booster(pickedCards);
    }

    @Override
    public int getMaxRound(int numberOfPlayers) {
        return MAX_ROUND;
    }

    private List<String> pickRandom(List<String> source, int count) {
        if (source.isEmpty() || count <= 0) return Collections.emptyList();

        int size = source.size();
        count = Math.min(count, size);

        Set<Integer> selectedIndexes = new HashSet<>();
        List<String> result = new ArrayList<>();

        Random rand = new Random();
        while (selectedIndexes.size() < count) {
            int randomIndex = rand.nextInt(size);
            if (selectedIndexes.add(randomIndex)) {
                result.add(source.get(randomIndex));
            }
        }
        return result;
    }

    private static String joinFilters(List<String> toJoin) {
        StringJoiner joiner = new StringJoiner(" ");
        toJoin.forEach(joiner::add);
        return joiner.toString();
    }
}
