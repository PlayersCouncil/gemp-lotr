package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.packs.ProductLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZonedDateTime;

public class ScheduledTournamentQueue extends AbstractTournamentQueue implements TournamentQueue {
    private static final Duration _signupTimeBeforeStart = Duration.ofMinutes(60);
    private static final Duration _wcSignupTimeBeforeStart = Duration.ofHours(30);
    private final ZonedDateTime _startTime;
    private final int _minimumPlayers;
    private final String _startCondition;
    private final TournamentService _tournamentService;
    private final String _tournamentName;
    private final Tournament.Stage _stage;
    private final String _scheduledTournamentId;

    public ScheduledTournamentQueue(String id, TournamentService tournamentService, ProductLibrary productLibrary,
                                    DBDefs.ScheduledTournament info) {
        super(id, info.cost, true, CollectionType.ALL_CARDS,
                Tournament.getTournamentPrizes(productLibrary, info.prizes),
                Tournament.getPairingMechanism(info.playoff), info.format);

        _tournamentService = tournamentService;
        _scheduledTournamentId = info.tournament_id;
        _tournamentName = info.name;
        _startTime = info.GetUTCStartDate();
        _startCondition = "at " + _startTime.format(DateUtils.DateTimeFormat);
        _minimumPlayers = info.minimum_players;

        if (info.manual_kickoff) {
            _stage = Tournament.Stage.AWAITING_KICKOFF;
        } else {
            _stage = Tournament.Stage.PLAYING_GAMES;
        }
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentName;
    }

    @Override
    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem() + ", minimum players: " + _minimumPlayers;
    }

    @Override
    public String getStartCondition() {
        return _startCondition;
    }

    @Override
    public synchronized boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) throws SQLException, IOException {
        var now = ZonedDateTime.now();
        if (now.isAfter(_startTime)) {
            if (_players.size() >= _minimumPlayers) {
                for (String player : _players)
                    _tournamentService.recordTournamentPlayer(_scheduledTournamentId, player, _playerDecks.get(player));

                var info = new TournamentInfo(_scheduledTournamentId, null, _tournamentName, _format, ZonedDateTime.now(),
                        _collectionType, _stage, 0, false,
                        _pairingMechanism, _tournamentPrizes);

                var tournament = _tournamentService.addTournament(info);

                tournamentQueueCallback.createTournament(tournament);
            } else {
                _tournamentService.recordScheduledTournamentStarted(_scheduledTournamentId);
                leaveAllPlayers(collectionsManager);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isJoinable() {
        var window = _signupTimeBeforeStart;
        if (_scheduledTournamentId.toLowerCase().contains("wc")) {
            window = _wcSignupTimeBeforeStart;
        }
        return ZonedDateTime.now().isAfter(_startTime.minus(window));
    }
}
