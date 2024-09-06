package com.gempukku.lotro.tournament.action;

import com.gempukku.lotro.tournament.TournamentCallback;

public class BroadcastAction implements TournamentProcessAction {
    private final String message;

    public BroadcastAction(String message) {
        this.message = message;
    }

    @Override
    public void process(TournamentCallback callback) {
        callback.broadcastMessage(message);
    }
}
