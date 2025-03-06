package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.*;
import com.gempukku.lotro.draft3.timer.DraftTimer;
import com.gempukku.lotro.draft3.timer.DraftTimerProducer;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.HashMap;
import java.util.Map;

public class FotrTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;
    private static Map<String, Double> cardValuesForBots = new HashMap<>();

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;


    public FotrTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                    LotroFormatLibrary formatLibrary) {
        startingCollectionProducer = new FotrTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new FotrTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary);
        cardValuesForBots = new FotrDraftBotsInitializer(cardLibrary).getValuesMap();
    }

    @Override
    public TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimerProducer draftTimerProducer) {
        DraftTimer draftTimer = draftTimerProducer == null ? null : draftTimerProducer.getDraftTimer();
        return new TableDraftClassic(collectionsManager, collectionType, startingCollectionProducer, boosterProducer, PLAYER_COUNT, DRAFT_ROUNDS, draftTimer, cardValuesForBots);
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
