package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.timer.DraftTimer;

public interface TableDraftDefinition {
    TableDraft getTableDraft(CollectionsManager collectionsManager, CollectionType collectionType, DraftTimer draftTimer);
    int getMaxPlayers();

    String getName();
    String getCode();
    String getFormat();

    DraftTimer.Type getRecommendedTimer();
}
