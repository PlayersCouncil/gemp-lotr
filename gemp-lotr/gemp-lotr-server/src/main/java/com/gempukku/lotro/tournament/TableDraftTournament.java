package com.gempukku.lotro.tournament;

import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraft;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.draft3.timer.DraftTimerFactory;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class TableDraftTournament extends BaseTournament implements Tournament {

    private TableDraftTournamentInfo tableDraftInfo;
    private TableDraft table = null;
    private final ChatServer chatServer;
    private ZonedDateTime nextRoundStart = null;

    public TableDraftTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
                                LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, TableDraftDefinitions tableDraftDefinitions,
                                TableHolder tables, String tournamentId, ChatServer chatServer) {
        super(tournamentService, collectionsManager, productLibrary, formatLibrary, soloDraftDefinitions, tableDraftDefinitions, tables, tournamentId);
        this.chatServer = chatServer;
        // Create draft chat room
        if (getTournamentStage() == Stage.STARTING || getTournamentStage() == Stage.DRAFT) {
            chatServer.createChatRoom("Draft-" + tableDraftInfo.tableDraftParams.tournamentId, false, 30, false, null,
                    "Welcome to room: " + _tableDraftLibrary.getTableDraftDefinition(tableDraftInfo.tableDraftParams.tableDraftFormatCode).getName());
        }
    }

    @Override
    protected void recreateTournamentInfo(DBDefs.Tournament data) {
        tableDraftInfo = new TableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, data, _tableDraftLibrary);
        _tournamentInfo = tableDraftInfo;
    }


    @Override
    public boolean playerSubmittedDeck(String player, LotroDeck deck) {
        writeLock.lock();
        try {
            var stage = getTournamentStage();
            if ((stage == Stage.DECK_BUILDING || stage == Stage.DECK_REGISTRATION ||
                    stage == Stage.PAUSED || stage == Stage.AWAITING_KICKOFF)
                    && _players.contains(player)) {
                _tournamentService.updateRecordedPlayerDeck(_tournamentId, player, deck);
                _playerDecks.put(player, deck);

                regeneratePlayerList();

                // If all registered the deck, skip the wait and start playing
                Set<String> activePlayers = new HashSet<>(_players);
                activePlayers.removeAll(_droppedPlayers);
                boolean everyoneSubmitted = true;
                for(var playerName : activePlayers) {
                    var registeredDeck = getPlayerDeck(playerName);
                    if(registeredDeck == null || StringUtils.isEmpty(registeredDeck.getDeckName())) {
                        everyoneSubmitted = false;
                    }
                }
                if (everyoneSubmitted) {
                    _tournamentInfo.Stage = tableDraftInfo.postRegistrationStage();
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                }

                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    //No locking because it is handled in the function that calls this one
    protected void resumeTournamentFromDatabase() {
        if (getTournamentStage() == Stage.DRAFT) {
            createTable();
        } else if (getTournamentStage() == Stage.DECK_BUILDING) {
            if (tableDraftInfo.deckbuildingDeadline == null || tableDraftInfo.registrationDeadline == null) {
                tableDraftInfo.deckbuildingDeadline = DateUtils.Now().plus(tableDraftInfo.deckbuildingDuration);
                tableDraftInfo.registrationDeadline = tableDraftInfo.deckbuildingDeadline.plus(tableDraftInfo.registrationDuration);
            }
        } else if (getTournamentStage() == Stage.DECK_REGISTRATION) {
            if (tableDraftInfo.registrationDeadline == null) {
                tableDraftInfo.registrationDeadline = DateUtils.Now().plus(tableDraftInfo.registrationDuration);
            }
        } else if (_tournamentInfo.Stage == Stage.PLAYING_GAMES) {
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
        } else if (_tournamentInfo.Stage == Stage.AWAITING_KICKOFF || _tournamentInfo.Stage == Stage.PAUSED) {
            // We await a moderator to manually progress the tournament stage
        } else if (_tournamentInfo.Stage == Stage.FINISHED) {
            _finishedTournamentMatches.addAll(_tournamentService.retrieveMatchups(_tournamentId));
        }
    }

    private void createTable() {
        // Create one table for all players and start
        if (table == null) {
            table = _tableDraftLibrary.getTableDraftDefinition(tableDraftInfo.tableDraftParams.tableDraftFormatCode).getTableDraft(_collectionsManager, getCollectionType(), DraftTimerFactory.getDraftTimer(tableDraftInfo.tableDraftParams.draftTimerType));
            for (String playerName : _players) {
                table.registerPlayer(playerName);
            }
            table.advanceDraft();
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
                if(getTournamentStage() == Stage.STARTING) {
                    _tournamentInfo.Stage = Stage.DRAFT;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                    createTable();

                    result.add(new BroadcastAction("Draft has been opened for tournament <b>" + getTournamentName() + "</b>. Use the 'Go to Draft' button in the Active Tournaments Section. " +
                            "When the draft is finished, you will have " + (tableDraftInfo.tableDraftParams.deckbuildingDuration + tableDraftInfo.tableDraftParams.turnInDuration) + " minutes to build and register your deck.", activePlayers));
                }
                else if (getTournamentStage() == Stage.DRAFT) {
                    if (table == null) {
                        // Cannot resume draft after server restart, end the tournament
                        result.add(finishTournament(collectionsManager));
                    } else if (table.isFinished()) {
                        _tournamentInfo.Stage = Stage.DECK_BUILDING;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                        tableDraftInfo.deckbuildingDeadline = DateUtils.Now().plus(tableDraftInfo.deckbuildingDuration);
                        tableDraftInfo.registrationDeadline = tableDraftInfo.deckbuildingDeadline.plus(tableDraftInfo.registrationDuration);

                        String duration = DateUtils.HumanDuration(tableDraftInfo.deckbuildingDuration);
                        result.add(new BroadcastAction("Draft has finished for tournament <b>" + getTournamentName() + "</b>. " +
                                "Players now have " + duration + " to build a deck with the cards they got. "
                                + "<br/><br/>Remember to return to the game hall and register your deck before " + DateUtils.FormatTime(tableDraftInfo.registrationDeadline) + ".", activePlayers));
                    }
                }
                else if (getTournamentStage() == Stage.DECK_BUILDING) {
                    if (DateUtils.Now().isAfter(tableDraftInfo.deckbuildingDeadline)) {
                        _tournamentInfo.Stage = Stage.DECK_REGISTRATION;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                        String duration = DateUtils.HumanDuration(tableDraftInfo.registrationDuration);
                        result.add(new BroadcastAction("Deck building in tournament <b>" + getTournamentName() + "</b> has finished. Players now have "
                                + duration + " to finish registering their decks.  Any player who has not turned in their deck by the deadline at "
                                + DateUtils.FormatTime(tableDraftInfo.registrationDeadline) + " will be auto-disqualified."
                                + "<br/><br/>Once the deadline has passed, the tournament will begin.", activePlayers));
                    }
                }

                if (getTournamentStage() == Stage.DECK_REGISTRATION) {
                    if (DateUtils.Now().isAfter(tableDraftInfo.registrationDeadline)) {
                        disqualifyUnregisteredPlayers();

                        _tournamentInfo.Stage = tableDraftInfo.postRegistrationStage();
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                    }
                }

                if (getTournamentStage() == Stage.AWAITING_KICKOFF || getTournamentStage() == Stage.PAUSED) {
                    // We await a moderator to manually progress the tournament stage
                } else if (getTournamentStage() == Stage.PREPARING) {
                    _tournamentInfo.Stage = Stage.PLAYING_GAMES;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                } else if (getTournamentStage() == Stage.PLAYING_GAMES) {

                    // Chat room no longer needed - kept alive during deck-building if people stayed longer
                    chatServer.destroyChatRoom("Draft-" + tableDraftInfo.tableDraftParams.tournamentId);

                    if (_currentlyPlayingPlayers.isEmpty()) {
                        if (_tournamentInfo.PairingMechanism.isFinished(getCurrentRound(), _players, _droppedPlayers)) {
                            result.add(finishTournament(collectionsManager));
                        } else {
                            nextRoundStart = DateUtils.Now().plus(PairingDelayTime);
                            if(getCurrentRound() == 0) {
                                result.add(new BroadcastAction("Deck registration for tournament <b>" + getTournamentName()
                                        + "</b> has closed. Round "
                                        + (getCurrentRound() + 1) + " will begin in " + DateUtils.HumanDuration(PairingDelayTime)
                                        + " at " + DateUtils.FormatTime(DateUtils.Now().plus(PairingDelayTime))+ " server time.", activePlayers));
                            }
                            else {
                                result.add(new BroadcastAction("Tournament " + getTournamentName() + " will start round "
                                        + (getCurrentRound() + 1) + " in " + DateUtils.HumanDuration(PairingDelayTime)
                                        + " at " + DateUtils.FormatTime(DateUtils.Now().plus(PairingDelayTime))+ " server time.", activePlayers));
                            }
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

    private void disqualifyUnregisteredPlayers() {
        var players = _tournamentService.retrieveTournamentPlayers(_tournamentId);

        for(var playerName : players) {
            var deck = getPlayerDeck(playerName);
            if(deck == null || StringUtils.isEmpty(deck.getDeckName())) {
                dropPlayer(playerName);
            }
        }
    }

    public TableDraft getTableDraft() {
        return table;
    }

    @Override
    public Draft getDraft() {
        return null;
    }

    @Override
    public void playerChosenCard(String playerName, String cardId) {
        // this is for real-time draft only
    }

    @Override
    public CollectionType getCollectionType() {
        return tableDraftInfo.generateCollectionInfo();
    }

    @Override
    public boolean isJoinable() {
        return false; // cannot join draft in progress
    }

    @Override
    public long getSecondsRemaining() throws IllegalStateException {
        if (getTournamentStage() == Stage.DECK_BUILDING) {
            return Duration.between(DateUtils.Now(), tableDraftInfo.deckbuildingDeadline).getSeconds();
        } else if (getTournamentStage() == Stage.DECK_REGISTRATION) {
            return Duration.between(DateUtils.Now(), tableDraftInfo.registrationDeadline).getSeconds();
        } else if (getTournamentStage() == Stage.PLAYING_GAMES && nextRoundStart != null && DateUtils.Now().isBefore(nextRoundStart)) {
            return Duration.between(DateUtils.Now(), nextRoundStart).getSeconds();
        } else {
            throw new IllegalStateException();
        }
    }
}
