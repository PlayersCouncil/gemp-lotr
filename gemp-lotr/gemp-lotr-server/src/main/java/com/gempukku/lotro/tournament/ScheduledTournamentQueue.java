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
    /** Set once an admin edit has replaced this queue: it takes no more players and never starts. */
    private boolean _retired = false;

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

    /**
     * Retires this queue so an edited copy of the tournament can replace it, but only while nobody is signed up
     * (signing up takes the entry cost).  Synchronized with {@link #joinPlayer}, so a join either lands before this
     * (and the queue is kept) or finds the queue closed and takes nothing.
     * @return true when the queue was empty and is now retired
     */
    public synchronized boolean retireIfEmpty() {
        if (!_players.isEmpty())
            return false;
        _retired = true;
        return true;
    }

    public synchronized boolean isRetired() {
        return _retired;
    }

    @Override
    public synchronized boolean process() throws SQLException, IOException  {
        if (_retired) {
            // Replaced by an edited queue under the same id: never start, and never report "finished", since the
            // caller removes finished queues by id and would drop the replacement.
            return false;
        }
        if (shouldDestroy) {
            return true; // Tournament started by Ready Check already, destroy the queue
        }

        var now = ZonedDateTime.now();
        if (now.isAfter(_startTime)) {
            if (_players.size() >= _tournamentInfo.Parameters().minimumPlayers) {
                startTournament();
                _tournamentService.recordScheduledTournamentStarted(_tournamentInfo.Parameters().tournamentId);
            } else {
                leaveAllPlayers();
            }
            return true; //destroy the queue now that the tournament has started
        }
        return false; //keep the queue as it hasn't started yet
    }

    @Override
    public boolean isJoinable() {
        if (isRetired())
            return false;
        var window = _signupTimeBeforeStart;
        if (isWC()) {
            window = _wcSignupTimeBeforeStart;
        }
        return DateUtils.Now().isAfter(_startTime.minus(window)) && (maximumPlayers < 0 || _players.size() < maximumPlayers);
    }

    @Override
    protected String getJoinRefusal() {
        if (isRetired())
            return "This tournament was changed by an administrator; refresh the hall and join the updated one.";
        var window = isWC() ? _wcSignupTimeBeforeStart : _signupTimeBeforeStart;
        var opens = _startTime.minus(window);
        if (!DateUtils.Now().isAfter(opens))
            return "Sign-up for " + getTournamentQueueName() + " opens at " + DateUtils.FormatDateTime(opens) + ".";
        if (maximumPlayers >= 0 && _players.size() >= maximumPlayers)
            return describeFull(maximumPlayers);
        return super.getJoinRefusal();
    }

    @Override
    public boolean shouldBeDisplayedAsWaiting() {
        var window = _signupTimeBeforeStart;
        if (isWC()) {
            window = _wcSignupTimeBeforeStart;
        }
        return DateUtils.Now().isAfter(_startTime.minus(window));
    }
}
