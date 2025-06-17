package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;

import java.io.IOException;
import java.sql.SQLException;

public class PlayerMadeQueue extends AbstractTournamentQueue implements TournamentQueue {
    public PlayerMadeQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, boolean startableEarly, int readyCheckTimeSecs,
                           TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        super(tournamentService, queueId, queueName, info, startableEarly, readyCheckTimeSecs, tournamentQueueCallback, collectionsManager, true);
    }

    @Override
    public String getStartCondition() {
        String playerLimit = "When " + getInfo()._params.maximumPlayers + " players join";
        String orRequest = " or when start is requested";
        if (isStartableEarly()) {
            return playerLimit + orRequest;
        } else {
            return playerLimit;
        }
    }

    @Override
    public boolean process() throws SQLException, IOException {
        if (shouldDestroy) {
            return true; // Tournament started by Ready Check already, destroy the queue
        }

        if (_players.isEmpty()) {
            return true; // All players left, destroy the queue
        }

        if (_players.size() < getInfo()._params.maximumPlayers)
            return false; // Do nothing if not enough players

        startTournament();
        return false; // Do not destroy the queue during possible ready check
    }

    @Override
    public boolean isJoinable() {
        return (getInfo()._params.maximumPlayers < 0 || _players.size() < getInfo()._params.maximumPlayers) && !isReadyCheckTimerRunning();
    }

    @Override
    public boolean shouldBeDisplayedAsWaiting() {
        return true; // Always display player made queues in waiting tables section
    }
}
