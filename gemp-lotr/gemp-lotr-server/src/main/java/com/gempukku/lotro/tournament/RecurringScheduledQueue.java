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

                TournamentInfo newInfo = getInfo(tid, tournamentName);

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

    private TournamentInfo getInfo(String tournamentId, String tournamentName) {
        TournamentParams params = getParams(tournamentId, tournamentName);

        boolean isSealed = _tournamentInfo instanceof SealedTournamentInfo;
        boolean isSoloDraft = _tournamentInfo instanceof SoloDraftTournamentInfo;
        boolean isSoloTableDraft = _tournamentInfo instanceof SoloTableDraftTournamentInfo;
        boolean isTableDraft = _tournamentInfo instanceof TableDraftTournamentInfo;

        TournamentInfo newInfo;

        if (isSealed) {
            newInfo = new SealedTournamentInfo((SealedTournamentInfo) _tournamentInfo, (SealedTournamentParams) params);
        } else if (isSoloDraft) {
            newInfo = new SoloDraftTournamentInfo((SoloDraftTournamentInfo) _tournamentInfo, (SoloDraftTournamentParams) params);
        } else if(isSoloTableDraft) {
            newInfo = new SoloTableDraftTournamentInfo((SoloTableDraftTournamentInfo) _tournamentInfo, (SoloTableDraftTournamentParams) params);
        } else if (isTableDraft) {
            newInfo = new TableDraftTournamentInfo((TableDraftTournamentInfo) _tournamentInfo, (TableDraftTournamentParams) params);
        } else {
            newInfo = new TournamentInfo(_tournamentInfo, params);
        }

        return newInfo;
    }

    private TournamentParams getParams(String tournamentId, String tournamentName) {
        TournamentParams tbr;
        if (_tournamentInfo instanceof SealedTournamentInfo) {
            tbr = new SealedTournamentParams() {{
                this.type = Tournament.TournamentType.SEALED;
                this.deckbuildingDuration = ((SealedTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SealedTournamentParams) _tournamentInfo._params).turnInDuration;
                this.sealedFormatCode = ((SealedTournamentParams) _tournamentInfo._params).sealedFormatCode;
            }};
        } else if (_tournamentInfo instanceof SoloDraftTournamentInfo) {
            tbr = new SoloDraftTournamentParams() {{
                this.type = Tournament.TournamentType.SOLODRAFT;
                this.deckbuildingDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SoloDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.soloDraftFormatCode = ((SoloDraftTournamentParams) _tournamentInfo._params).soloDraftFormatCode;
            }};
        } else if (_tournamentInfo instanceof SoloTableDraftTournamentInfo) {
            tbr = new SoloTableDraftTournamentParams() {{
                this.type = Tournament.TournamentType.TABLE_SOLODRAFT;
                this.deckbuildingDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((SoloTableDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.soloTableDraftFormatCode = ((SoloTableDraftTournamentParams) _tournamentInfo._params).soloTableDraftFormatCode;
            }};
        } else if (_tournamentInfo instanceof TableDraftTournamentInfo) {
            tbr = new TableDraftTournamentParams() {{
                this.type = Tournament.TournamentType.TABLE_DRAFT;
                this.draftTimerProducerType = ((TableDraftTournamentParams) _tournamentInfo._params).draftTimerProducerType;
                this.deckbuildingDuration = ((TableDraftTournamentParams) _tournamentInfo._params).deckbuildingDuration;
                this.turnInDuration = ((TableDraftTournamentParams) _tournamentInfo._params).turnInDuration;
                this.tableDraftFormatCode = ((TableDraftTournamentParams) _tournamentInfo._params).tableDraftFormatCode;
            }};
        } else {
            tbr = new TournamentParams();
            tbr.type = Tournament.TournamentType.CONSTRUCTED;
        }

        tbr.tournamentId = tournamentId;
        tbr.name = tournamentName;
        tbr.format = getFormatCode();
        tbr.startTime = DateUtils.Now().toLocalDateTime();
        tbr.playoff = _tournamentInfo._params.playoff;
        tbr.manualKickoff = false;
        tbr.cost = getCost();
        tbr.prizes = _tournamentInfo._params.prizes;
        tbr.minimumPlayers = _minimumPlayers;
        tbr.requiresDeck = _tournamentInfo._params.requiresDeck;

        return tbr;
    }
}