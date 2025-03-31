package com.gempukku.lotro.draft3.evaluator;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.apache.commons.collections4.map.UnmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TttDraftCardEvaluator extends AbstractDraftCardEvaluator {
    private static final Map<String, Double> AVERAGE_STARTING_COUNT_MAP = new HashMap<>();

    static {
        List<String> allCards = new ArrayList<>();
        allCards.addAll(List.of("4_90", "4_105", "4_97", "4_98", "4_97", "4_105", "4_90", "4_109", "4_115", "4_112",
                "4_115", "4_109", "4_71", "4_83", "4_76", "4_78", "4_83", "4_71", "4_74", "4_87",
                "4_70", "4_87", "4_74", "4_117", "4_135", "4_112", "4_128", "4_130", "4_135", "4_117"));
        allCards.addAll(List.of("4_266", "4_278", "4_277", "4_273", "4_273", "4_281", "4_283", "4_266", "4_270",
                "4_287", "4_265", "4_297", "4_265", "4_287", "4_270", "4_49", "4_44", "4_42", "4_56",
                "4_42", "4_44", "4_49", "4_310", "4_322", "4_308", "4_314", "4_310", "4_308", "4_322", "4_314"));
        allCards.addAll(List.of("4_248", "4_221", "4_222", "4_227", "4_226", "4_224",
                "4_228", "4_258", "4_228", "4_224", "4_226", "4_227", "4_222", "4_221", "4_248", "4_165", "4_191", "4_184",
                "4_206", "4_204", "4_180", "4_198", "4_137", "4_198", "4_180", "4_204", "4_206", "4_184", "4_191", "4_165",
                "4_4", "4_17", "4_16", "4_25", "4_10", "4_14", "4_12", "4_21", "4_12", "4_14", "4_10", "4_25", "4_16",
                "4_17", "4_4", "4_207", "4_181", "4_193", "4_189", "4_187", "4_190", "4_178", "4_153", "4_178", "4_190",
                "4_187", "4_189", "4_193", "4_181", "4_207"));

        for (String card : allCards) {
            // Every single entry from draft pack wheels has 14/60 to be chosen - if multiple entries are the same card, the cards chance increases
            AVERAGE_STARTING_COUNT_MAP.merge(card, 14.0 / 60.0, Double::sum);
        }
        AVERAGE_STARTING_COUNT_MAP.put("4_302", 1.0); // Frodo
        AVERAGE_STARTING_COUNT_MAP.put("4_2", 1.0); // Ruling Ring
    }

    public TttDraftCardEvaluator(LotroCardBlueprintLibrary library) {
        super(library);
    }

    @Override
    public Map<String, Double> getAverageStartingCardMap() {
        return UnmodifiableMap.unmodifiableMap(AVERAGE_STARTING_COUNT_MAP);
    }
}
