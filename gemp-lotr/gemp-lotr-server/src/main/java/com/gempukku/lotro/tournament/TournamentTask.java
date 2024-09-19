package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;

import java.util.List;

public interface TournamentTask {
    public void executeTask(List<TournamentProcessAction> actions, CollectionsManager collectionsManager);

    public long getExecuteAfter();
}
