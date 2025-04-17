package com.gempukku.lotro.tournament.action;

import com.gempukku.lotro.tournament.TournamentCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BroadcastAction implements TournamentProcessAction {
    private final String message;
    private List<String> toWhom;

    public BroadcastAction(String message, Collection<String> toWhom) {
        this.message = message;
        this.toWhom = new ArrayList<>(toWhom);
    }

    @Override
    public void process(TournamentCallback callback) {
        callback.broadcastMessage(message, toWhom);
    }
}
