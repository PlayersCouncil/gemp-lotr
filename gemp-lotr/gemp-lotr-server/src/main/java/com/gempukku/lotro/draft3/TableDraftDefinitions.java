package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.format.fotr.FotrTableDraftDefinition;
import com.gempukku.lotro.draft3.format.fotr_mixed.FotrMixedTableDraftDefinition;
import com.gempukku.lotro.draft3.format.ttt.TttTableDraftDefinition;
import com.gempukku.lotro.draft3.format.ttt_mixed.TttMixedTableDraftDefinition;
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
        draftTypes.put(fotrTableDraftDefinition.getCode(), fotrTableDraftDefinition);

        FotrMixedTableDraftDefinition fotrMixedTableDraftDefinition = new FotrMixedTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(fotrMixedTableDraftDefinition.getCode(), fotrMixedTableDraftDefinition);

        TttTableDraftDefinition tttTableDraftDefinition = new TttTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(tttTableDraftDefinition.getCode(), tttTableDraftDefinition);

        TttMixedTableDraftDefinition tttMixedTableDraftDefinition = new TttMixedTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(tttMixedTableDraftDefinition.getCode(), tttMixedTableDraftDefinition);
    }

    public TableDraftDefinition getTableDraftDefinition(String draftType) {
        return draftTypes.get(draftType);
    }

    public Map<String, TableDraftDefinition> getAllTableDrafts() {
        return Collections.unmodifiableMap(draftTypes);
    }
}
