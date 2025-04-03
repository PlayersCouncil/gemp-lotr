package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZonedDateTime;

public class ScheduledTournamentQueue extends AbstractTournamentQueue implements TournamentQueue {
    private static final Duration _signupTimeBeforeStart = Duration.ofMinutes(60);
    private static final Duration _wcSignupTimeBeforeStart = Duration.ofHours(30);
    private final ZonedDateTime _startTime;
    private final int maximumPlayers;

    public ScheduledTournamentQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info,
                                    TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        super(tournamentService, queueId, queueName, info, tournamentQueueCallback, collectionsManager, true);
        _startTime = DateUtils.ParseDate(info.Parameters().startTime);
        maximumPlayers = info.Parameters().maximumPlayers;
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentInfo.Parameters().name;
    }

    @Override
    public String getPairingDescription() {
        return _tournamentInfo.PairingMechanism.getPlayOffSystem() + ", minimum players: " + _tournamentInfo.Parameters().minimumPlayers;
    }

    @Override
    public String getStartCondition() {
        return  "at " + DateUtils.FormatDateTime(_startTime);
    }

    @Override
    public synchronized boolean process() throws SQLException, IOException  {
        if (shouldDestroy) {
            return true; // Tournament started by Ready Check already, destroy the queue
        }

        var now = ZonedDateTime.now();
        if (now.isAfter(_startTime)) {
            if (_players.size() >= _tournamentInfo.Parameters().minimumPlayers) {
                startTournament();
            } else {
                _tournamentService.recordScheduledTournamentStarted(_tournamentInfo.Parameters().tournamentId);
                leaveAllPlayers();
            }
            return true; //destroy the queue now that the tournament has started
        }
        return false; //keep the queue as it hasn't started yet
    }

    @Override
    public boolean isJoinable() {
        var window = _signupTimeBeforeStart;
        if (_tournamentInfo.Parameters().tournamentId.toLowerCase().contains("wc")) {
            window = _wcSignupTimeBeforeStart;
        }
        return DateUtils.Now().isAfter(_startTime.minus(window)) && (maximumPlayers < 0 || _players.size() < maximumPlayers);
    }
}
