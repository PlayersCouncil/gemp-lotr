package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;

public class ImmediateRecurringQueue extends AbstractTournamentQueue implements TournamentQueue {
    private final int _playerCap;

    public ImmediateRecurringQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info) {
        super(tournamentService, queueId, queueName, info);
        _playerCap = info.Parameters().minimumPlayers;
    }

    @Override
    public String getStartCondition() {
        return "When " + _playerCap + " players join";
    }

    @Override
    public synchronized boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        if (_players.size() < _playerCap)
            return false;

        String tid = _tournamentInfo.generateTimestampId();

        String tournamentName = _tournamentQueueName + " - " + DateUtils.getStringDateWithHour();

        for (int i=0; i<_playerCap; i++) {
            String player = _players.poll();
            _tournamentService.recordTournamentPlayer(tid, player, _playerDecks.get(player));
            _playerDecks.remove(player);
        }

        var params = new TournamentParams() {{
            this.tournamentId = tid;
            this.name = tournamentName;
            this.format = getFormatCode();
            this.startTime = DateUtils.Now().toLocalDateTime();
            this.type = Tournament.TournamentType.CONSTRUCTED;
            this.playoff = Tournament.PairingType.SINGLE_ELIMINATION;
            this.manualKickoff = false;
            this.requiresDeck = true;
            this.cost = getCost();
            this.minimumPlayers = _playerCap;
        }};

        var newInfo = new TournamentInfo(_tournamentInfo, params);
        var tournament = _tournamentService.addTournament(newInfo);
        tournamentQueueCallback.createTournament(tournament);

        //We never want the recurring queues to be removed, so we always return false.
        return false;
    }

    @Override
    public boolean isJoinable() {
        return true;
    }
}
