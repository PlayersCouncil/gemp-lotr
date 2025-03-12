package com.gempukku.lotro.draft3.format.fotr_mixed;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.*;
import com.gempukku.lotro.draft3.format.fotr.FotrDraftCardEvaluator;
import com.gempukku.lotro.draft3.format.fotr.FotrTableDraftStartingCollectionProducer;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.draft3.timer.DraftTimerProducer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.Map;

public class FotrMixedTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;
    private final Map<String, Double> cardValues;


    public FotrMixedTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                         LotroFormatLibrary formatLibrary) {
        FotrDraftCardEvaluator evaluator = new FotrDraftCardEvaluator(cardLibrary);
        cardValues = evaluator.getValuesMap();
        Map<String, Double> cardPlayRates = evaluator.getPlayRateMap();

        startingCollectionProducer = new FotrTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new FotrMixedTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary, cardPlayRates);
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
        return "fotr_mixed_table_draft";
    }

    @Override
    public String getFormat() {
        return "limited_fotr";
    }
}
