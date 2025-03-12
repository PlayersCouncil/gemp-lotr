package com.gempukku.lotro.draft3.format.fotr;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.*;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.draft3.timer.DraftTimerProducer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.Map;

public class FotrTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final Map<String, Double> cardValues;


    public FotrTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                    LotroFormatLibrary formatLibrary) {
        FotrDraftCardEvaluator evaluator = new FotrDraftCardEvaluator(cardLibrary);
        cardValues = evaluator.getValuesMap();
        Map<String, Double> cardPlayRates = evaluator.getPlayRateMap();
        // Print the card values for manual check
//        cardValuesForBots.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    try {
//                        System.out.println(cardLibrary.getLotroCardBlueprint(entry.getKey()).getCardInfo().rarity + ": " + cardLibrary.getLotroCardBlueprint(entry.getKey()).getFullName() + ": " + entry.getValue());
//                    } catch (CardNotFoundException e) {
//                        System.out.println(entry.getKey() + ": " + entry.getValue());
//                    }
//                });

//        cardValuesForBots.entrySet()
//                .stream()
//                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort by values in descending order
//                .forEach(entry -> {
//                    System.out.println("tbr.put(\"" + entry.getKey() + "\", " + entry.getValue() + ");");
//                });

        startingCollectionProducer = new FotrTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new FotrTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary, cardPlayRates);
    }

    @Override
    public TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimerProducer draftTimerProducer) {
        DraftTimer draftTimer = draftTimerProducer == null ? null : draftTimerProducer.getDraftTimer();
        return new TableDraftClassic(collectionsManager, collectionType, startingCollectionProducer, boosterProducer, PLAYER_COUNT, DRAFT_ROUNDS, draftTimer, cardValues);
    }

    @Override
    public int getMaxPlayers() {
        return PLAYER_COUNT;
    }

    @Override
    public String getCode() {
        return "fotr_table_draft";
    }

    @Override
    public String getFormat() {
        return "limited_fotr";
    }
}
