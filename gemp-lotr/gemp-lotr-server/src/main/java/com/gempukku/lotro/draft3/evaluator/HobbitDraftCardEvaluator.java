package com.gempukku.lotro.draft3.evaluator;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.apache.commons.collections4.map.UnmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HobbitDraftCardEvaluator extends AbstractDraftCardEvaluator {
    private static final Map<String, Double> AVERAGE_STARTING_COUNT_MAP = new HashMap<>();

    private static final List<String> freePeoplesPack = List.of(
            "30_1", "30_1", "30_3", "30_3", "30_4", "30_4", "30_13", "30_13",
            "30_14", "30_14", "30_20", "30_20", "30_23", "30_23", "30_24", "30_24",
            "30_30", "30_30", "30_31", "30_31", "30_58", "30_59", "30_60", "30_61",
            "30_62", "30_63", "30_64", "30_65");
    private static final List<String> swarmPack1 = List.of(
            "31_30", "31_30", "31_30", "31_30", "31_31", "31_31", "31_31", "31_31",
            "31_32", "31_32", "31_32", "31_32", "31_34", "31_34", "31_35", "31_35",
            "31_35", "31_35", "31_36", "31_36", "31_49", "31_49", "31_49", "31_51",
            "31_51", "31_51", "31_51", "31_52", "31_52", "31_52", "31_53", "31_53",
            "31_53", "31_53", "31_54", "31_54", "31_55", "31_55", "31_55", "31_55");
    private static final List<String> swarmPack2 = List.of(
            "32_27", "32_27", "32_27", "32_27", "32_28", "32_28", "32_28", "32_28",
            "32_31", "32_31", "32_31", "32_32", "32_32", "32_32", "32_33", "32_33",
            "32_33", "32_34", "32_34", "32_34", "32_36", "32_36", "32_36", "32_38",
            "32_38", "32_40", "32_40", "32_40", "32_41", "32_41", "32_41", "32_41",
            "32_42", "32_42", "32_42", "32_42", "32_43", "32_43", "32_43", "32_43");
    private static final List<String> swarmPack3 = List.of(
            "33_38", "33_38", "33_38", "33_38", "33_39", "33_39", "33_39", "33_39",
            "33_40", "33_40", "33_40", "33_41", "33_41", "33_41", "33_43", "33_43",
            "33_45", "33_45", "33_45", "33_45", "33_46", "33_46", "33_46", "33_47",
            "33_47", "33_47", "33_47", "33_48", "33_48", "33_48", "33_49", "33_49",
            "33_49", "33_49", "33_50", "33_50", "33_50", "33_51", "33_51", "33_51");
    private static final List<String> beatDownPack1 = List.of(
            "31_19", "31_19", "31_19", "31_19", "31_20", "31_20", "31_20", "31_21",
            "31_21", "31_21", "31_23", "31_23", "31_23", "31_24", "31_24", "31_26",
            "31_26", "31_26", "31_27", "31_27", "31_27", "31_27", "31_28", "31_28",
            "31_28", "31_56", "31_56", "31_56", "31_58", "31_58", "31_58", "31_59",
            "31_59", "31_59", "31_60", "31_60", "31_60", "31_61", "31_61", "31_61");
    private static final List<String> beatDownPack2 = List.of(
            "32_37", "32_37", "32_37", "32_37", "32_44", "32_44", "32_45", "32_45",
            "32_45", "32_45", "32_50", "32_50", "32_51", "32_51", "32_51", "32_51",
            "32_52", "32_52", "32_52", "32_52", "32_54", "32_54", "32_54", "32_55",
            "32_55", "32_56", "32_56", "32_56", "32_58", "32_58", "32_58", "32_59",
            "32_60", "32_61", "32_62", "32_63", "32_64", "32_65", "32_66", "32_67");
    private static final List<String> beatDownPack3 = List.of(
            "33_24", "33_24", "33_24", "33_25", "33_25", "33_25", "33_26", "33_26",
            "33_26", "33_27", "33_27", "33_27", "33_28", "33_28", "33_28", "33_29",
            "33_29", "33_29", "33_30", "33_30", "33_30", "33_31", "33_31", "33_31",
            "33_32", "33_32", "33_32", "33_34", "33_34", "33_34", "33_35", "33_35",
            "33_35", "33_42", "33_42", "33_42", "33_42", "33_44", "33_44", "33_44");
    private static final List<String> packA = List.of(
            "31_38", "31_38", "31_38", "31_39", "31_39", "31_40", "31_40", "31_41",
            "31_41", "31_41", "31_42", "31_42", "31_42", "31_43", "31_43", "31_22",
            "31_22", "31_22", "31_57", "31_57", "31_45", "31_45");
    private static final List<String> packB = List.of(
            "32_1", "32_1", "32_1", "32_2", "32_2", "32_2", "32_4", "32_4",
            "32_5", "32_5", "32_6", "32_6", "32_8", "32_8", "32_8", "32_26",
            "32_26", "32_30", "32_30", "32_30", "32_49", "32_49");
    private static final List<String> packC = List.of(
            "32_15", "32_15", "32_15", "32_16", "32_16", "32_17", "32_17", "32_18",
            "32_18", "32_19", "32_19", "32_19", "32_20", "32_20", "32_20", "32_25",
            "32_25", "32_25", "32_53", "32_53", "32_47", "32_47");
    private static final List<String> packD = List.of(
            "31_3", "31_3", "31_3", "31_8", "31_8", "31_9", "31_9", "31_10",
            "31_10", "31_11", "31_11", "31_11", "31_12", "31_12", "31_12", "31_29",
            "31_29", "31_37", "31_37", "31_37", "31_46", "31_46");
    private static final List<String> packE = List.of(
            "31_1", "31_1", "31_2", "31_2", "31_2", "31_4", "31_4", "31_5",
            "31_5", "31_5", "31_6", "31_6", "31_7", "31_7", "31_7", "31_33",
            "31_33", "31_33", "31_48", "31_48", "31_47", "31_47");
    private static final List<String> packF = List.of(
            "32_3", "32_3", "32_3", "32_7", "32_7", "32_9", "32_9", "32_9",
            "32_10", "32_10", "32_11", "32_11", "32_11", "32_12", "32_12", "32_29",
            "32_29", "32_35", "32_35", "32_35", "32_48", "32_48");
    private static final List<String> packG = List.of(
            "32_13", "32_13", "32_13", "32_14", "32_14", "32_21", "32_21", "32_22",
            "32_22", "32_23", "32_23", "32_23", "32_24", "32_24", "32_24", "32_39",
            "32_39", "32_57", "32_57", "32_57", "32_46", "32_46");
    private static final List<String> packH = List.of(
            "31_13", "31_13", "31_13", "31_14", "31_14", "31_15", "31_15", "31_16",
            "31_16", "31_17", "31_17", "31_17", "31_18", "31_18", "31_18", "31_25",
            "31_25", "31_25", "31_50", "31_50", "31_44", "31_44");
    private static final List<String> packI = List.of(
            "33_1", "33_1", "33_3", "33_3", "33_3", "33_4", "33_4", "33_10",
            "33_10", "33_10", "33_11", "33_11", "33_11", "33_12", "33_12", "33_36",
            "33_36", "33_36", "33_59", "33_59", "33_56", "33_56");
    private static final List<String> packJ = List.of(
            "33_2", "33_2", "33_5", "33_5", "33_5", "33_6", "33_6", "33_6",
            "33_7", "33_7", "33_8", "33_8", "33_9", "33_9", "33_9", "33_37",
            "33_37", "33_37", "33_60", "33_60", "33_58", "33_58");
    private static final List<String> packK = List.of(
            "33_13", "33_13", "33_13", "33_14", "33_14", "33_14", "33_15", "33_15",
            "33_16", "33_16", "33_17", "33_17", "33_17", "33_18", "33_18", "33_33",
            "33_33", "33_33", "33_61", "33_61", "33_57", "33_57");
    private static final List<String> packL = List.of(
            "33_19", "33_19", "33_20", "33_20", "33_21", "33_21", "33_52", "33_52",
            "33_52", "33_53", "33_53", "33_53", "33_54", "33_54", "33_54", "33_22",
            "33_22", "33_23", "33_23", "33_23", "33_55", "33_55");
    private static final List<String> shadowPacks = new ArrayList<>();
    private static final List<String> supplementaryPacks = new ArrayList<>();
    private static final List<String> everything = new ArrayList<>();


    static {
        List<String> startingDeck = List.of(
                "30_1", "30_2", "30_3", "30_4", "30_5", "30_6", "30_7", "30_8",
                "30_9", "30_10", "30_11", "30_12", "30_13", "30_14", "30_15", "30_16",
                "30_17", "30_18", "30_19", "30_20", "30_21", "30_22", "30_23", "30_24",
                "30_29", "30_30", "30_31", "30_47", "30_48", "30_32", "30_32", "30_32",
                "30_33", "30_33", "30_34", "30_34", "30_35", "30_35", "30_35", "30_35",
                "30_36", "30_36", "30_37", "30_37", "30_38", "30_38", "30_39", "30_39",
                "30_39", "30_40", "30_40", "30_40", "30_41", "30_41", "30_42", "30_42",
                "30_42", "30_42", "30_66", "30_49", "30_50", "30_51", "30_52", "30_53",
                "30_54", "30_55", "30_56", "30_57");

        for (String card : startingDeck) {
            AVERAGE_STARTING_COUNT_MAP.merge(card, 1.0, Double::sum);
        }
        List.of("30_25", "30_26", "30_27", "30_28").forEach(gandalf -> AVERAGE_STARTING_COUNT_MAP.put(gandalf, 0.25));
        List.of("30_43", "30_44", "30_45", "30_46").forEach(bilbo -> AVERAGE_STARTING_COUNT_MAP.put(bilbo, 0.25));

        shadowPacks.addAll(swarmPack1);
        shadowPacks.addAll(swarmPack2);
        shadowPacks.addAll(swarmPack3);
        shadowPacks.addAll(beatDownPack1);
        shadowPacks.addAll(beatDownPack2);
        shadowPacks.addAll(beatDownPack3);

        supplementaryPacks.addAll(packA);
        supplementaryPacks.addAll(packB);
        supplementaryPacks.addAll(packC);
        supplementaryPacks.addAll(packD);
        supplementaryPacks.addAll(packE);
        supplementaryPacks.addAll(packF);
        supplementaryPacks.addAll(packG);
        supplementaryPacks.addAll(packH);
        supplementaryPacks.addAll(packI);
        supplementaryPacks.addAll(packJ);
        supplementaryPacks.addAll(packK);
        supplementaryPacks.addAll(packL);

        everything.addAll(freePeoplesPack);
        everything.addAll(freePeoplesPack);
        everything.addAll(freePeoplesPack);
        everything.addAll(freePeoplesPack);
        everything.addAll(shadowPacks);
        everything.addAll(shadowPacks);
        everything.addAll(supplementaryPacks);
    }

    public HobbitDraftCardEvaluator(LotroCardBlueprintLibrary library) {
        super(library);
    }

    @Override
    public Map<String, Double> getValuesMap(Map<String, Integer> winningMap, Map<String, Integer> losingMap, int gamesAnalyzed) {
        // Merge win and lose maps (they are kept separate for potential wr info)
        Map<String, Integer> mergedMap = mergeMaps(winningMap, losingMap);
        // Lower count of cards from Draft Packs
        Map<String, Double> startingCollectionNormalizedCardCountMap = normalizeCountByDraftPackChance(mergedMap, gamesAnalyzed);

        // Normalize for uneven set size
        Map<String, Double> packNormalizedCardCountMap = normalizeCountByPackType(startingCollectionNormalizedCardCountMap);

        // Normalize count based on rarities
        Map<String, Double> normalizedCardCountMap = normalizeCountByRarity(packNormalizedCardCountMap);

        // Make sure all values are positive and in 0-1 range
        Map<String, Double> shiftedMap = shift(normalizedCardCountMap);

        return shiftedMap;
    }

    private Map<String, Double> normalizeCountByRarity(Map<String, Double> map) {
        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> normalizedMap.put(key, value / everything.stream().filter(s -> s.equals(key)).count()));

        return normalizedMap;

    }

    private Map<String, Double> normalizeCountByPackType(Map<String, Double> map) {

        Map<String, Double> normalizedMap = new HashMap<>();

        map.forEach((key, value) -> {
            if (shadowPacks.contains(key)) {
                normalizedMap.put(key, value * 3.0); // appear in 1/3 of drafts
            } else if (freePeoplesPack.contains(key)){
                normalizedMap.put(key, value); // appear in all drafts
            } else if (supplementaryPacks.contains(key)) {
                normalizedMap.put(key, value * 3.0); // appear in 1/3 of drafts (solo draft is using 4 packs out of 12)
            }
        });

        return normalizedMap;
    }

    @Override
    public Map<String, Double> getAverageStartingCardMap() {
        return UnmodifiableMap.unmodifiableMap(AVERAGE_STARTING_COUNT_MAP);
    }
}
