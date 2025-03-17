package com.gempukku.lotro.draft3.format.ttt;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.*;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.Map;

public class TttTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final Map<String, Double> cardValues;


    public TttTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                   LotroFormatLibrary formatLibrary) {
        TttDraftCardEvaluator evaluator = new TttDraftCardEvaluator(cardLibrary);
        cardValues = evaluator.getCachedValuesMap();
        Map<String, Double> cardPlayRates = evaluator.getCachedPlayRateMap();
        // Print the card values for manual check
//        cardValues.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    try {
//                        System.out.println(cardLibrary.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity + ": " + cardLibrary.getLotroCardBlueprint(entry.getKey()).getFullName() + ": " + entry.getValue());
//                    } catch (CardNotFoundException e) {
//                        System.out.println(entry.getKey() + ": " + entry.getValue());
//                    }
//                });
//
//        cardValues.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    System.out.println("tbr.put(\"" + entry.getKey() + "\", " + entry.getValue() + ");");
//                });
//        cardPlayRates.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    try {
//                        System.out.println(cardLibrary.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity + ": " + cardLibrary.getLotroCardBlueprint(entry.getKey()).getFullName() + ": " + entry.getValue());
//                    } catch (CardNotFoundException e) {
//                        System.out.println(entry.getKey() + ": " + entry.getValue());
//                    }
//                });
//
//        cardPlayRates.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    System.out.println("tbr.put(\"" + entry.getKey() + "\", " + entry.getValue() + ");");
//                });

        startingCollectionProducer = new TttTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new TttTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary, cardPlayRates);
    }

    @Override
    public TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimer draftTimer) {
        return new TableDraftClassic(collectionsManager, collectionType, startingCollectionProducer, boosterProducer, PLAYER_COUNT, DRAFT_ROUNDS, draftTimer, cardValues);
    }

    @Override
    public int getMaxPlayers() {
        return PLAYER_COUNT;
    }

    @Override
    public String getCode() {
        return "ttt_table_draft";
    }

    @Override
    public String getFormat() {
        return "limited_ttt";
    }
}
