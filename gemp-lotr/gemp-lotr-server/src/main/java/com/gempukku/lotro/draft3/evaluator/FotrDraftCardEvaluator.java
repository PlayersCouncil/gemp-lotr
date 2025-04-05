package com.gempukku.lotro.draft3.evaluator;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.apache.commons.collections4.map.UnmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FotrDraftCardEvaluator extends AbstractDraftCardEvaluator {
    private static final Map<String, Double> AVERAGE_STARTING_COUNT_MAP = new HashMap<>();

    static {
        List<String> allCards = new ArrayList<>();
        allCards.addAll(List.of("1_296", "2_110", "1_12", "1_365", "1_112", "1_365", "1_311", "1_317",
                "2_104", "3_122", "1_364", "3_35", "1_78", "1_364", "2_114", "1_299", "1_303", "3_121", "1_365", "1_112",
                "1_365", "2_110", "1_298", "2_104", "1_51", "1_364", "3_35", "1_76", "1_364", "2_114"));
        allCards.addAll(List.of("1_26", "1_7", "1_9", "1_11", "1_5", "2_121", "1_97", "2_37", "1_97",
                "1_51", "1_37", "1_48", "1_32", "1_51", "1_97", "2_121", "1_5", "2_6", "1_9", "1_7", "1_6", "2_121", "1_97",
                "3_7", "1_37", "1_48", "2_18", "3_7", "1_97", "2_121"));
        allCards.addAll(List.of("3_96", "1_270", "2_89", "1_262", "1_262", "2_89", "1_270", "3_96",
                "1_231", "2_61", "1_179", "2_63", "1_184", "2_62", "2_62", "1_184", "2_63", "1_179", "2_61", "1_234", "1_158",
                "1_152", "1_154", "1_151", "1_145", "1_151", "1_154", "1_152", "1_158", "1_231", "3_100", "1_267", "1_270",
                "1_271", "1_271", "1_270", "1_267", "3_100", "1_234", "1_177", "2_67", "1_178", "1_176", "2_60", "2_60", "1_176",
                "1_178", "2_67", "1_177", "1_231", "3_57", "3_58", "3_59", "3_62", "3_69", "3_62", "3_59", "3_58", "3_57", "1_234"));

        for (String card : allCards) {
            // Every single entry from draft pack wheels has 14/60 to be chosen - if multiple entries are the same card, the cards chance increases
            AVERAGE_STARTING_COUNT_MAP.merge(card, 14.0 / 60.0, Double::sum);
        }
        AVERAGE_STARTING_COUNT_MAP.put("1_290", 1.0); // Frodo SoD
        AVERAGE_STARTING_COUNT_MAP.put("1_2", 1.0); // Ruling Ring
    }

    public FotrDraftCardEvaluator(LotroCardBlueprintLibrary library) {
        super(library);
    }

    @Override
    public Map<String, Double> getAverageStartingCardMap() {
        return UnmodifiableMap.unmodifiableMap(AVERAGE_STARTING_COUNT_MAP);
    }
}
