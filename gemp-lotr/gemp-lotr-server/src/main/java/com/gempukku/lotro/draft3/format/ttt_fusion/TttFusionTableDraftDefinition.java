package com.gempukku.lotro.draft3.format.ttt_fusion;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.*;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.draft3.format.ttt.TttDraftCardEvaluator;
import com.gempukku.lotro.draft3.format.ttt.TttTableDraftStartingCollectionProducer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.Map;

public class TttFusionTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final Map<String, Double> cardValues;


    public TttFusionTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                         LotroFormatLibrary formatLibrary) {
        TttDraftCardEvaluator evaluator = new TttDraftCardEvaluator(cardLibrary);
        cardValues = evaluator.getCachedValuesMap();
        Map<String, Double> cardPlayRates = evaluator.getCachedPlayRateMap();

        startingCollectionProducer = new TttTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new TttFusionTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary, cardPlayRates);
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
    public String getName() {
        return "TTT Block Fusion Booster Draft";
    }

    @Override
    public String getCode() {
        return "ttt_fusion_table_draft";
    }

    @Override
    public String getFormat() {
        return "limited_ttt";
    }
}
