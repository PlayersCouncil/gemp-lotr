package com.gempukku.lotro.draft3.fotr;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.BoosterProducer;
import com.gempukku.lotro.draft3.StartingCollectionProducer;
import com.gempukku.lotro.draft3.TableDraft;
import com.gempukku.lotro.draft3.TableDraftDefinition;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

public class FotrTableDraftDefinition implements TableDraftDefinition {
    private static final int DRAFT_ROUNDS = 6;
    private static final int PLAYER_COUNT = 6;

    private final StartingCollectionProducer startingCollectionProducer;
    private final BoosterProducer boosterProducer;

    public FotrTableDraftDefinition(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                    LotroFormatLibrary formatLibrary) {
        startingCollectionProducer = new FotrTableDraftStartingCollectionProducer(collectionsManager, cardLibrary, formatLibrary);
        boosterProducer = new FotrTableDraftBoosterProducer(collectionsManager, cardLibrary, formatLibrary);
    }

    @Override
    public TableDraft getTableDraft() {
        return new FotrTableDraft(startingCollectionProducer, boosterProducer, PLAYER_COUNT, DRAFT_ROUNDS);
    }

    @Override
    public String getFormatCode() {
        return "fotr_table_draft";
    }

    @Override
    public String getFormat() {
        return "limited_fotr";
    }
}
