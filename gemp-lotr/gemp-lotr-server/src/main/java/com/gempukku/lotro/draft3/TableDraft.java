package com.gempukku.lotro.draft3;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;

public interface TableDraft {
    DraftPlayer registerPlayer(String name);
    DraftPlayer getPlayer(String name);
    boolean removePlayer(String name);
    void startDraft();
    boolean readyToPick(DraftPlayer who);
    boolean passBooster(DraftPlayer from, Booster booster);
    boolean isFinished();

    CollectionType getCollectionType();
    CollectionsManager getCollectionsManager();
}
