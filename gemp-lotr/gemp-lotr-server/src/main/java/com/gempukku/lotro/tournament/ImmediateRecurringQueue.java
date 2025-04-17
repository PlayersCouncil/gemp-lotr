package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;

public class ImmediateRecurringQueue extends AbstractTournamentQueue implements TournamentQueue {
    private final int _playerCap;
    private final int maxPlayers;


    public ImmediateRecurringQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info,
                                   TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        this(tournamentService, queueId, queueName, info, false, -1, tournamentQueueCallback, collectionsManager);
    }

    public ImmediateRecurringQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, boolean startableEarly, int readyCheckTimeSecs,
                                   TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        super(tournamentService, queueId, queueName, info, startableEarly, readyCheckTimeSecs, tournamentQueueCallback, collectionsManager, false);
        _playerCap = info.Parameters().minimumPlayers;
        maxPlayers = info.Parameters().maximumPlayers;
    }

    @Override
    public String getStartCondition() {
        String playerLimit = "When " + _playerCap + " players join";
        String orRequest = " or when start is requested";
        if (isStartableEarly()) {
            return playerLimit + orRequest;
        } else {
            return playerLimit;
        }
    }

    @Override
    public synchronized boolean process() {
        if (_players.size() < _playerCap)
            return false;

        startTournament();
        return false;
    }

    @Override
    public boolean isJoinable() {
        return (maxPlayers < 0 || _players.size() < maxPlayers);
    }
}
