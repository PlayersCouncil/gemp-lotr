package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SoloDraftTournament extends BaseTournament implements Tournament {

    private SealedTournamentInfo _sealedInfo;

    private long _deckBuildStartTime;
    private Draft _draft;

    public SoloDraftTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
            LotroFormatLibrary formatLibrary,  TableHolder tables, String tournamentId) {
        super(tournamentService, collectionsManager, productLibrary, formatLibrary, tables, tournamentId);
    }

    @Override
    protected void recreateTournamentInfo(DBDefs.Tournament data) {
        _sealedInfo = new SealedTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, data);
        _tournamentInfo = _sealedInfo;
    }


    @Override
    public void playerSubmittedDeck(String player, LotroDeck deck) {
        writeLock.lock();
        try {
            if (getTournamentStage() == Stage.DECK_BUILDING && _players.contains(player)) {
                _tournamentService.updateRecordedPlayerDeck(_tournamentId, player, deck);
                _playerDecks.put(player, deck);
            }
        } finally {
            writeLock.unlock();
        }
    }

    //No locking because it is handled in the function that calls this one
    protected void resumeTournamentFromDatabase() {
        if (_tournamentInfo.Stage == Stage.PLAYING_GAMES) {
            var matchesToCreate = new HashMap<String, String>();
            var existingTables = _tables.getTournamentTables(_tournamentId);
            for (TournamentMatch tournamentMatch : _tournamentService.retrieveMatchups(_tournamentId)) {
                if (tournamentMatch.isFinished())
                    _finishedTournamentMatches.add(tournamentMatch);
                else {
                    _currentlyPlayingPlayers.add(tournamentMatch.getPlayerOne());
                    _currentlyPlayingPlayers.add(tournamentMatch.getPlayerTwo());

                    if(existingTables.stream().anyMatch(x -> x.hasPlayer(tournamentMatch.getPlayerOne()) &&
                            x.hasPlayer(tournamentMatch.getPlayerTwo())))
                    {
                        continue;
                    }

                    matchesToCreate.put(tournamentMatch.getPlayerOne(), tournamentMatch.getPlayerTwo());
                }
            }

            if (!matchesToCreate.isEmpty())
                _nextTask = new CreateMissingGames(matchesToCreate);
        }
        //TODO: move this and other draft-specific handling into its own DraftTournament
//            else if (_tournamentStage == Stage.DRAFT) {
//                _draft = new DefaultDraft(collectionsManager, _collectionType, productLibrary, draftPack,
//                        _players);
//            }
//            else if (_tournamentStage == Stage.DECK_BUILDING) {
//                _deckBuildStartTime = System.currentTimeMillis();
//            }
        else if (_tournamentInfo.Stage == Stage.AWAITING_KICKOFF || _tournamentInfo.Stage == Stage.PAUSED) {
            // We await a moderator to manually progress the tournament stage
        } else if (_tournamentInfo.Stage == Stage.FINISHED) {
            _finishedTournamentMatches.addAll(_tournamentService.retrieveMatchups(_tournamentId));
        }
    }

    @Override
    public List<TournamentProcessAction> advanceTournament(CollectionsManager collectionsManager) {
        writeLock.lock();
        try {
            List<TournamentProcessAction> result = new LinkedList<>();
            if (_nextTask == null) {
                if (getTournamentStage() == Stage.DRAFT) {
                    _draft.advanceDraft();
                    if (_draft.isFinished()) {
                        result.add(new BroadcastAction("Drafting in tournament " + getTournamentName() + " is finished, starting deck building"));
                        _tournamentInfo.Stage = Stage.DECK_BUILDING;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                        _deckBuildStartTime = System.currentTimeMillis();
                        _draft = null;
                    }
                }
                if (getTournamentStage() == Stage.DECK_BUILDING) {
                    if (_deckBuildStartTime + DeckBuildTime < System.currentTimeMillis()
                            || _playerDecks.size() == _players.size()) {
                        _tournamentInfo.Stage = Stage.PLAYING_GAMES;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                        result.add(new BroadcastAction("Deck building in tournament " + getTournamentName() + " has finished"));
                    }
                }
                if (getTournamentStage() == Stage.AWAITING_KICKOFF || getTournamentStage() == Stage.PAUSED) {
                    // We await a moderator to manually progress the tournament stage
                } else if (getTournamentStage() == Stage.PREPARING) {
                    _tournamentInfo.Stage = Stage.PLAYING_GAMES;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                } else if (getTournamentStage() == Stage.PLAYING_GAMES) {
                    if (_currentlyPlayingPlayers.isEmpty()) {
                        if (_tournamentInfo.PairingMechanism.isFinished(getCurrentRound(), _players, _droppedPlayers)) {
                            result.add(finishTournament(collectionsManager));
                        } else {
                            String duration = DurationFormatUtils.formatDurationWords(PairingDelayTime.toMillis(), true, true);
                            result.add(new BroadcastAction("Tournament " + getTournamentName() + " will start round " + (getCurrentRound()+1) + " in " + duration + "."));
                            _nextTask = new PairPlayers();
                        }
                    }
                }
            }
            if (_nextTask != null && _nextTask.getExecuteAfter() <= System.currentTimeMillis()) {
                TournamentTask task = _nextTask;
                _nextTask = null;
                task.executeTask(result, collectionsManager);
            }
            return result;
        } finally {
            writeLock.unlock();
        }
    }


    @Override
    public Draft getDraft() {
        return _draft;
    }

    @Override
    public void playerChosenCard(String playerName, String cardId) {
        writeLock.lock();
        try {
            if (getTournamentStage() == Stage.DRAFT) {
                _draft.playerChosenCard(playerName, cardId);
            }
        } finally {
            writeLock.unlock();
        }
    }

}
