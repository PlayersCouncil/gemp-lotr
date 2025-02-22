package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.Booster;
import com.gempukku.lotro.draft3.BoosterProducer;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FotrTableDraftBoosterProducer implements BoosterProducer {

    private static final String RARE_FILTER = "rarity:R";
    private static final String UNCOMMON_FILTER = "rarity:U"; // No P rarity cards in boosters
    private static final String COMMON_FILTER = "rarity:C";
    private static final String FOTR_FILTER = "set:1";
    private static final String MOM_FILTER = "set:2";
    private static final String ROTEL_FILTER = "set:3";
    private static final String FP_FILTER = "side:FREE_PEOPLE";
    private static final String SHADOW_FILTER = "side:SHADOW";

    private final Map<String, List<String>> cardPools = new HashMap<>();

    public FotrTableDraftBoosterProducer(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                         LotroFormatLibrary formatLibrary) {
        SortAndFilterCards sortAndFilterCards = new SortAndFilterCards();

        List<String> rarities = List.of("rare", "uncommon", "common");
        List<String> sets = List.of("fotr", "mom", "rotel");
        List<String> types = List.of("fp", "shadow");

        Map<String, String> filters = Map.of(
                "fotr", FOTR_FILTER,
                "mom", MOM_FILTER,
                "rotel", ROTEL_FILTER,
                "fp", FP_FILTER,
                "shadow", SHADOW_FILTER,
                "rare", RARE_FILTER,
                "uncommon", UNCOMMON_FILTER,
                "common", COMMON_FILTER
        );

        // Make 18 different lists of cards for combinations of rarity, set and side
        for (String set : sets) {
            for (String rarity : rarities) {
                for (String type : types) {
                    String key = rarity + "-" + set + "-" + type;
                    List<String> filterSet = List.of(filters.get(rarity), filters.get(set), filters.get(type));

                    cardPools.put(key, sortAndFilterCards.process(
                            joinFilters(filterSet),
                            collectionsManager.getCompleteCardCollection().getAll(),
                            cardLibrary,
                            formatLibrary
                    ).stream().map(CardCollection.Item::getBlueprintId).collect(Collectors.toList()));
                    // Remove alternate versions from different sets
                    cardPools.get(key).removeIf(new Predicate<String>() {
                        @Override
                        public boolean test(String s) {
                            return !s.startsWith((sets.indexOf(set) + 1) + "_");
                        }
                    });
                }
            }
        }
    }

    @Override
    public Booster getBooster(int round) {
        if (round < 1 || round > 6) {
            return null;
        }
        String set = switch (round % 3) {
            case 1 -> "fotr";
            case 2 -> "mom";
            default -> "rotel";
        };
        String type = round <= 3 ? "fp" : "shadow";

        // Get the correct lists
        List<String> rareCards = cardPools.get("rare-" + set + "-" + type);
        List<String> uncommonCards = cardPools.get("uncommon-" + set + "-" + type);
        List<String> commonCards = cardPools.get("common-" + set + "-" + type);

        // Pick cards
        List<String> pickedCards = pickRandom(rareCards, 1);
        pickedCards.addAll(pickRandom(uncommonCards, 3));
        pickedCards.addAll(pickRandom(commonCards, 7));

        return new Booster(pickedCards);
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
