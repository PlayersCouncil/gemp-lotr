package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.timer.DraftTimerProducer;

public interface TableDraftDefinition {
    TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimerProducer draftTimerProducer);
    int getMaxPlayers();

    String getCode();
    String getFormat();
}
