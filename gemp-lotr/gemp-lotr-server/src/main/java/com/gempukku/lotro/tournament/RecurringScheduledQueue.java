package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;

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

    private final String _tournamentIdPrefix;
    private final String _tournamentQueueName;

    private final int _minimumPlayers;
    private final TournamentService _tournamentService;

    public RecurringScheduledQueue(String id, ZonedDateTime originalStart, Duration repeatEvery, String tournamentIdPrefix,
                                              String tournamentQueueName, int cost, boolean requiresDeck,
                                              CollectionType collectionType,
                                              TournamentService tournamentService, TournamentPrizes tournamentPrizes, PairingMechanism pairingMechanism, String format, int minimumPlayers) {
        super(id, cost, requiresDeck, collectionType, tournamentPrizes, pairingMechanism, format);
        _repeatEvery = repeatEvery;
        _tournamentIdPrefix = tournamentIdPrefix;
        _tournamentQueueName = tournamentQueueName;
        _tournamentService = tournamentService;
        _minimumPlayers = minimumPlayers;
        var sinceOriginal = Duration.between(originalStart, ZonedDateTime.now());
        long intervals = (sinceOriginal.getSeconds() / repeatEvery.getSeconds()) + 1;

        _nextStart = originalStart.plus(intervals * repeatEvery.getSeconds(), ChronoUnit.SECONDS);
        _nextStartText = DateUtils.FormatDateTime(_nextStart);
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
        return _pairingMechanism.getPlayOffSystem() + ", minimum players: " + _minimumPlayers;
    }

    @Override
    public boolean isJoinable() {
        return ZonedDateTime.now().isAfter(_nextStart.minus(_signupTimeBeforeStart));
    }

    @Override
    public boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) throws SQLException, IOException {
        if (ZonedDateTime.now().isAfter(_nextStart)) {
            if (_players.size() >= _minimumPlayers) {
                String tournamentId = _tournamentIdPrefix+System.currentTimeMillis();
                String tournamentName = _tournamentQueueName + " - " + DateUtils.getStringDateWithHour();

                for (String player : _players)
                    _tournamentService.recordTournamentPlayer(tournamentId, player, _playerDecks.get(player));

                var info = new TournamentInfo(tournamentId, null, tournamentName, _format, ZonedDateTime.now(),
                        _collectionType, Tournament.Stage.PLAYING_GAMES, 0, false,
                        _pairingMechanism, _tournamentPrizes);

                var tournament = _tournamentService.addTournament(info);

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