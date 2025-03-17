package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;

public class ImmediateRecurringQueue extends AbstractTournamentQueue implements TournamentQueue {
    private final int _playerCap;
    private final int maxPlayers;

    private final boolean startableEarly;
    private boolean startRequested = false;

    public ImmediateRecurringQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info) {
        this(tournamentService, queueId, queueName, info, false);
    }

    public ImmediateRecurringQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info, boolean startableEarly) {
        super(tournamentService, queueId, queueName, info);
        _playerCap = info.Parameters().minimumPlayers;
        maxPlayers = info.Parameters().maximumPlayers;
        this.startableEarly = startableEarly;
    }

    @Override
    public String getStartCondition() {
        String playerLimit = "When " + _playerCap + " players join";
        String orRequest = " or when start is requested";
        if (startableEarly) {
            return playerLimit + orRequest;
        } else {
            return playerLimit;
        }
    }

    @Override
    public synchronized boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        if ((_players.size() < _playerCap) && !startRequested)
            return false;

        startRequested = false;

        String tid = _tournamentInfo.generateTimestampId();

        String tournamentName = _tournamentQueueName + " - " + DateUtils.getStringDateWithHour();

        for (int i=0; i<_playerCap; i++) {
            String player = _players.poll();
            if (player != null) { // If start requested we don't have playerCap players
                _tournamentService.recordTournamentPlayer(tid, player, _playerDecks.get(player));
                _playerDecks.remove(player);
            }
        }

        TournamentInfo newInfo = getInfo(tid, tournamentName);

        var tournament = _tournamentService.addTournament(newInfo);
        tournamentQueueCallback.createTournament(tournament);

        //We never want the recurring queues to be removed, so we always return false.
        return false;
    }

    @Override
    public boolean isJoinable() {
        return maxPlayers < 0 || _players.size() < maxPlayers;
    }

    @Override
    public boolean isStartable(String byWhom) {
        return startableEarly && byWhom.equals(_players.peek());
    }

    @Override
    public boolean requestStart(String byWhom) {
        if (isStartable(byWhom)) {
            startRequested = true;
            return true;
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
                this.draftTimerType = ((TableDraftTournamentParams) _tournamentInfo._params).draftTimerType;
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
        tbr.minimumPlayers = _playerCap;
        tbr.maximumPlayers = maxPlayers;
        tbr.requiresDeck = _tournamentInfo._params.requiresDeck;

        return tbr;
    }
}
