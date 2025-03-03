package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;

public interface TableDraftDefinition {
    TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimerProducer draftTimerProducer);

    String getFormatCode();
    String getFormat();
}
