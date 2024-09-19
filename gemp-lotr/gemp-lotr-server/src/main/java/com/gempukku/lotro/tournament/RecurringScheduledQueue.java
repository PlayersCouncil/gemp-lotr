package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RecurringScheduledQueue extends AbstractTournamentQueue implements TournamentQueue {
    private static final Duration _signupTimeBeforeStart = Duration.ofMinutes(60);

    private final Duration _repeatEvery;
    private ZonedDateTime _nextStart;
    private String _nextStartText;

    private final int _minimumPlayers;

    public RecurringScheduledQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, Duration repeatEvery, int minPlayers) {
        super(tournamentService, queueId, queueName, info);
        _minimumPlayers = minPlayers;

        _repeatEvery = repeatEvery;
        var sinceOriginal = Duration.between(info.StartTime, ZonedDateTime.now());
        long intervals = (sinceOriginal.getSeconds() / repeatEvery.getSeconds()) + 1;

        _nextStart = info.StartTime.plus(intervals * repeatEvery.getSeconds(), ChronoUnit.SECONDS);
        _nextStartText = DateUtils.FormatDateTime(_nextStart);

        _tournamentInfo.StartTime = _nextStart;
    }

    @Override
    public String getStartCondition() {
        return _nextStartText;
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentQueueName;
    }

    @Override
    public String getPairingDescription() {
        return _tournamentInfo.PairingMechanism.getPlayOffSystem() + ", minimum players: " + _minimumPlayers;
    }

    @Override
    public boolean isJoinable() {
        return ZonedDateTime.now().isAfter(_nextStart.minus(_signupTimeBeforeStart));
    }

    @Override
    public boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) throws SQLException, IOException {
        if (ZonedDateTime.now().isAfter(_nextStart)) {
            if (_players.size() >= _minimumPlayers) {
                String tid = _tournamentInfo.generateTimestampId();
                String tournamentName = _tournamentQueueName + " - " + DateUtils.getStringDateWithHour();

                for (String player : _players) {
                    _tournamentService.recordTournamentPlayer(tid, player, _playerDecks.get(player));
                }

                var params = new TournamentParams() {{
                    this.tournamentId = tid;
                    this.name = tournamentName;
                    this.format = getFormatCode();
                    this.startTime = DateUtils.Now().toLocalDateTime();
                    this.type = Tournament.TournamentType.CONSTRUCTED;
                    this.playoff = Tournament.PairingType.SINGLE_ELIMINATION;
                    this.manualKickoff = false;
                    this.cost = getCost();
                    this.minimumPlayers = _minimumPlayers;
                }};

                var newInfo = new TournamentInfo(_tournamentInfo, params);
                var tournament = _tournamentService.addTournament(newInfo);
                tournamentQueueCallback.createTournament(tournament);

                _players.clear();
                _playerDecks.clear();
            } else {
                leaveAllPlayers(collectionsManager);
            }
            _nextStart = _nextStart.plus(_repeatEvery);
            _nextStartText = DateUtils.FormatDateTime(_nextStart);
        }
        return false;
    }
}