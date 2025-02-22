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
                this.minimumPlayers = _playerCap;
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
                this.minimumPlayers = _playerCap;
            }};
            newInfo = new SoloDraftTournamentInfo((SoloDraftTournamentInfo) _tournamentInfo, (SoloDraftTournamentParams) params);
        } else if(isSoloTableDraft) {
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
                this.minimumPlayers = _playerCap;
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
                this.minimumPlayers = _playerCap;
            }};
            newInfo = new TournamentInfo(_tournamentInfo, params);
        }

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
