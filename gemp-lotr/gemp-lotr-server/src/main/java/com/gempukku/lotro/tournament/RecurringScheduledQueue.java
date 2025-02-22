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

    public RecurringScheduledQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, Duration repeatEvery) {
        super(tournamentService, queueId, queueName, info);
        _minimumPlayers = info._params.minimumPlayers;

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

                boolean isSealed = _tournamentInfo instanceof SealedTournamentInfo;
                boolean isSoloDraft = _tournamentInfo instanceof SoloDraftTournamentInfo;
                boolean isSoloTableDraft = _tournamentInfo instanceof SoloTableDraftTournamentInfo;

                TournamentParams params;
                TournamentInfo newInfo;
                if (isSealed) {
                    params = new SealedTournamentParams() {{
                        this.type = Tournament.TournamentType.SEALED;

                        this.deckbuildingDuration = ((SealedTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                        this.turnInDuration = ((SealedTournamentParams) _tournamentInfo._params).turnInDuration;

                        this.sealedFormatCode = ((SealedTournamentParams) _tournamentInfo._params).sealedFormatCode;
                        this.format = _tournamentInfo._params.format;
                        this.requiresDeck = false;

                        this.tournamentId = tid;
                        this.playoff = _tournamentInfo._params.playoff;
                        this.prizes = _tournamentInfo._params.prizes;
                        this.name = tournamentName;
                        this.format = getFormatCode();
                        this.startTime = DateUtils.Now().toLocalDateTime();
                        this.manualKickoff = false;
                        this.cost = getCost();
                        this.minimumPlayers = _minimumPlayers;
                    }};
                    newInfo = new SealedTournamentInfo((SealedTournamentInfo) _tournamentInfo, (SealedTournamentParams) params);
                } else if (isSoloDraft) {
                    params = new SoloDraftTournamentParams() {{
                        this.type = Tournament.TournamentType.SOLODRAFT;

                        this.deckbuildingDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                        this.turnInDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).turnInDuration;

                        this.soloDraftFormatCode = ((SoloDraftTournamentParams) _tournamentInfo._params).soloDraftFormatCode;
                        this.format = _tournamentInfo._params.format;
                        this.requiresDeck = false;

                        this.tournamentId = tid;
                        this.playoff = _tournamentInfo._params.playoff;
                        this.prizes = _tournamentInfo._params.prizes;
                        this.name = tournamentName;
                        this.format = getFormatCode();
                        this.startTime = DateUtils.Now().toLocalDateTime();
                        this.manualKickoff = false;
                        this.cost = getCost();
                        this.minimumPlayers = _minimumPlayers;
                    }};
                    newInfo = new SoloDraftTournamentInfo((SoloDraftTournamentInfo) _tournamentInfo, (SoloDraftTournamentParams) params);
                } else if (isSoloTableDraft) {
                    params = new SoloTableDraftTournamentParams() {{
                        this.type = Tournament.TournamentType.TABLE_SOLODRAFT;

                        this.deckbuildingDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                        this.turnInDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).turnInDuration;

                        this.soloTableDraftFormatCode = ((SoloTableDraftTournamentParams) _tournamentInfo._params).soloTableDraftFormatCode;
                        this.format = _tournamentInfo._params.format;
                        this.requiresDeck = false;

                        this.tournamentId = tid;
                        this.playoff = _tournamentInfo._params.playoff;
                        this.prizes = _tournamentInfo._params.prizes;
                        this.name = tournamentName;
                        this.format = getFormatCode();
                        this.startTime = DateUtils.Now().toLocalDateTime();
                        this.manualKickoff = false;
                        this.cost = getCost();
                        this.minimumPlayers = _minimumPlayers;
                    }};
                    newInfo = new SoloTableDraftTournamentInfo((SoloTableDraftTournamentInfo) _tournamentInfo, (SoloTableDraftTournamentParams) params);
                } else {
                    params = new TournamentParams() {{
                        this.tournamentId = tid;
                        this.name = tournamentName;
                        this.format = getFormatCode();
                        this.startTime = DateUtils.Now().toLocalDateTime();
                        this.type = Tournament.TournamentType.CONSTRUCTED;
                        this.playoff = _tournamentInfo._params.playoff;
                        this.manualKickoff = false;
                        this.cost = getCost();
                        this.minimumPlayers = _minimumPlayers;
                    }};
                    newInfo = new TournamentInfo(_tournamentInfo, params);
                }


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