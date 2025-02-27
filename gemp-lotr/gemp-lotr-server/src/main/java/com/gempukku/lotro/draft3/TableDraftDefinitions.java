package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.fotr.FotrTableDraftDefinition;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TableDraftDefinitions {
    private final Map<String, TableDraftDefinition> draftTypes = new HashMap<>();

    public TableDraftDefinitions(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                 LotroFormatLibrary formatLibrary) {
        FotrTableDraftDefinition fotrTableDraftDefinition = new FotrTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(fotrTableDraftDefinition.getFormatCode(), fotrTableDraftDefinition);
    }

    public TableDraftDefinition getTableDraftDefinition(String draftType) {
        return draftTypes.get(draftType);
    }

    public Map<String, TableDraftDefinition> getAllTableDrafts() {
        return Collections.unmodifiableMap(draftTypes);
    }
}
