package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.competitive.ModifiedMedianStandingsProducer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.CreateGameAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConstructedTournament extends BaseTournament implements Tournament {

    public ConstructedTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
                                 LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, TableDraftDefinitions tableDraftDefinitions, TableHolder tables, String tournamentId) {
        super(tournamentService, collectionsManager, productLibrary, formatLibrary, soloDraftDefinitions, tableDraftDefinitions, tables, tournamentId);
    }

    @Override
    protected void recreateTournamentInfo(DBDefs.Tournament data) {
        _tournamentInfo = new TournamentInfo(_tournamentService, _productLibrary, _formatLibrary, data);
    }

    @Override
    public boolean playerSubmittedDeck(String player, LotroDeck deck) {
        //Constructed has all decks submitted upon signup, so nothing to do here
        return false;
    }



    //No locking because it is handled in the function that calls this one
    @Override
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
        else if (_tournamentInfo.Stage == Stage.AWAITING_KICKOFF || _tournamentInfo.Stage == Stage.PAUSED) {
            // We await a moderator to manually progress the tournament stage
        } else if (_tournamentInfo.Stage == Stage.FINISHED) {
            _finishedTournamentMatches.addAll(_tournamentService.retrieveMatchups(_tournamentId));
        }
    }

    @Override
    public List<TournamentProcessAction> advanceTournament(CollectionsManager collectionsManager) {
        writeLock.lock();
        Set<String> activePlayers = new HashSet<>(_players);
        activePlayers.removeAll(_droppedPlayers);
        try {
            List<TournamentProcessAction> result = new LinkedList<>();
            if (_nextTask == null) {
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
                            result.add(new BroadcastAction("Tournament " + getTournamentName() + " will start round " + (getCurrentRound()+1) + " in " + duration + ".", activePlayers));
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
        return null;
    }

    @Override
    public void playerChosenCard(String playerName, String cardId) {
        //This is for draft only
    }

    @Override
    public boolean isJoinable() {
        Set<String> activePlayers = new HashSet<>(_players);
        activePlayers.removeAll(_droppedPlayers);
        int maximumPlayers = _tournamentInfo._params.maximumPlayers;
        return (getTournamentStage() == Tournament.Stage.STARTING || getTournamentStage() == Stage.PREPARING ||
                getTournamentStage() == Tournament.Stage.PAUSED || getTournamentStage() == Tournament.Stage.AWAITING_KICKOFF)
                && (maximumPlayers > activePlayers.size() || maximumPlayers < 0);
    }
}
