package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft3.format.fotr.FotrTableDraftDefinition;
import com.gempukku.lotro.draft3.format.fotr_fusion.FotrFusionTableDraftDefinition;
import com.gempukku.lotro.draft3.format.fotr_power_max.FotrPowerMaxTableDraftDefinition;
import com.gempukku.lotro.draft3.format.ttt.TttTableDraftDefinition;
import com.gempukku.lotro.draft3.format.ttt_fusion.TttFusionTableDraftDefinition;
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

        FotrFusionTableDraftDefinition fotrFusionTableDraftDefinition = new FotrFusionTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(fotrFusionTableDraftDefinition.getCode(), fotrFusionTableDraftDefinition);

        TttTableDraftDefinition tttTableDraftDefinition = new TttTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(tttTableDraftDefinition.getCode(), tttTableDraftDefinition);

        TttFusionTableDraftDefinition tttFusionTableDraftDefinition = new TttFusionTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(tttFusionTableDraftDefinition.getCode(), tttFusionTableDraftDefinition);

        FotrPowerMaxTableDraftDefinition fotrPowerMaxTableDraftDefinition = new FotrPowerMaxTableDraftDefinition(collectionsManager, cardLibrary, formatLibrary);
        draftTypes.put(fotrPowerMaxTableDraftDefinition.getCode(), fotrPowerMaxTableDraftDefinition);
    }

    public TableDraftDefinition getTableDraftDefinition(String draftType) {
        return draftTypes.get(draftType);
    }

    public Map<String, TableDraftDefinition> getAllTableDrafts() {
        return Collections.unmodifiableMap(draftTypes);
    }
}
