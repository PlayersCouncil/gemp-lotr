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
    private final int _maximumPlayers;

    public RecurringScheduledQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, Duration repeatEvery,
                                   TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        super(tournamentService, queueId, queueName, info, tournamentQueueCallback, collectionsManager, false);
        _minimumPlayers = info._params.minimumPlayers;
        _maximumPlayers = info._params.maximumPlayers;

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
        return ZonedDateTime.now().isAfter(_nextStart.minus(_signupTimeBeforeStart)) && (_maximumPlayers < 0 || _players.size() < _maximumPlayers);
    }

    @Override
    public boolean shouldBeDisplayedAsWaiting() {
        // Display in waiting tables section 1 hour before start
        return ZonedDateTime.now().isAfter(_nextStart.minus(_signupTimeBeforeStart));
    }

    @Override
    public boolean process() throws SQLException, IOException {
        if (ZonedDateTime.now().isAfter(_nextStart)) {
            if (_players.size() >= _minimumPlayers) {
                startTournament();
            } else {
                leaveAllPlayers();
            }
            _nextStart = _nextStart.plus(_repeatEvery);
            _nextStartText = DateUtils.FormatDateTime(_nextStart);
        }
        return false;
    }
}