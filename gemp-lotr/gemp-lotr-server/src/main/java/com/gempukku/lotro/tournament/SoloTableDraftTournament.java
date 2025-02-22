package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraft;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SoloTableDraftTournament extends BaseTournament implements Tournament {

    private SoloTableDraftTournamentInfo soloTableDraftInfo;
    private Map<String, TableDraft> tables = null;

    public SoloTableDraftTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
                                    LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, TableDraftDefinitions tableDraftDefinitions,
                                    TableHolder tables, String tournamentId) {
        super(tournamentService, collectionsManager, productLibrary, formatLibrary, soloDraftDefinitions, tableDraftDefinitions, tables, tournamentId);
        if (this.tables == null) {
            this.tables = new HashMap<>();
        }
    }

    @Override
    protected void recreateTournamentInfo(DBDefs.Tournament data) {
        soloTableDraftInfo = new SoloTableDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, data, _tableDraftLibrary);
        _tournamentInfo = soloTableDraftInfo;
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

                // If 1v1 and both registered the deck, skip the wait and start playing
                var players = _tournamentService.retrieveTournamentPlayers(_tournamentId);
                boolean everyoneSubmitted = true;
                for(var playerName : players) {
                    var registeredDeck = getPlayerDeck(playerName);
                    if(registeredDeck == null || StringUtils.isEmpty(registeredDeck.getDeckName())) {
                        everyoneSubmitted = false;
                    }
                }
                if (players.size() == 2 && everyoneSubmitted) {
                    _tournamentInfo.Stage = soloTableDraftInfo.postRegistrationStage();
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
        if (getTournamentStage() == Stage.DECK_BUILDING) {
            createTablesAndStartingCollections();
        } else if (getTournamentStage() == Stage.DECK_REGISTRATION) {
            createTablesAndStartingCollections();
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

    private void createTablesAndStartingCollections() {
        if (tables == null) {
            tables = new HashMap<>();
        }

        // Create a table for each player to draft with bots
        for (String playerName : _players) {
            if (tables.containsKey(playerName)) {
                continue;
            }
            TableDraft tableDraft = _tableDraftLibrary.getTableDraftDefinition(soloTableDraftInfo.soloTableDraftParams.soloTableDraftFormatCode).getTableDraft();
            tables.put(playerName, tableDraft);
            tableDraft.registerPlayer(playerName);
            // Just one player per table
            tableDraft.startDraft();
        }

        var collDef = soloTableDraftInfo.generateCollectionInfo();
        var collections = _collectionsManager.getPlayersCollection(collDef.getCode());

        for(var playerName : _players) {
            // Check if player already has a collection
            var player = collections.keySet().stream().filter(x -> x.getName().equals(playerName)).findFirst();
            if(player.isPresent()) {
                continue;
            }

            // Create new collection for the player
            var startingCollection = new DefaultCardCollection();

            // Initialize with starting cards
            CardCollection alreadyAssigned = tables.get(playerName).getPlayer(playerName).getCardCollection();
            for (CardCollection.Item item : alreadyAssigned.getAll())
                startingCollection.addItem(item.getBlueprintId(), item.getCount());

            _collectionsManager.addPlayerCollection(false, "Draft tournament product", playerName, collDef, startingCollection);
        }
    }

    @Override
    public List<TournamentProcessAction> advanceTournament(CollectionsManager collectionsManager) {
        writeLock.lock();
        try {
            List<TournamentProcessAction> result = new LinkedList<>();
            if (_nextTask == null) {
                if(getTournamentStage() == Stage.STARTING) {
                    _tournamentInfo.Stage = Stage.DECK_BUILDING;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                    createTablesAndStartingCollections();

                    String duration = DateUtils.HumanDuration(soloTableDraftInfo.deckbuildingDuration);
                    result.add(new BroadcastAction("Draft has been opened for tournament <b>" + getTournamentName() + "</b>. Use the 'Go to Draft' button in the Active Tournaments Section. Players now have "
                            + duration + " to solo draft and build a deck with the cards they got. "
                            + "<br/><br/>Remember to return to the game hall and register your deck before " + DateUtils.FormatTime(soloTableDraftInfo.registrationDeadline) + "."));
                }
                else if (getTournamentStage() == Stage.DECK_BUILDING) {
                    if (DateUtils.Now().isAfter(soloTableDraftInfo.deckbuildingDeadline)) {
                        _tournamentInfo.Stage = Stage.DECK_REGISTRATION;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                        String duration = DateUtils.HumanDuration(soloTableDraftInfo.registrationDuration);
                        result.add(new BroadcastAction("Deck building in tournament <b>" + getTournamentName() + "</b> has finished. Players now have "
                                + duration + " to finish registering their decks.  Any player who has not turned in their deck by the deadline at "
                                + DateUtils.FormatTime(soloTableDraftInfo.registrationDeadline) + " will be auto-disqualified."
                                + "<br/><br/>Once the deadline has passed, the tournament will begin."));
                    }
                }

                if (getTournamentStage() == Stage.DECK_REGISTRATION) {
                    if (DateUtils.Now().isAfter(soloTableDraftInfo.registrationDeadline)) {
                        disqualifyUnregisteredPlayers();

                        _tournamentInfo.Stage = soloTableDraftInfo.postRegistrationStage();
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
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
                            if(getCurrentRound() == 0) {
                                result.add(new BroadcastAction("Deck registration for tournament <b>" + getTournamentName()
                                        + "</b> has closed. Round "
                                        + (getCurrentRound() + 1) + " will begin in " + DateUtils.HumanDuration(PairingDelayTime)
                                        + " at " + DateUtils.FormatTime(DateUtils.Now().plus(PairingDelayTime))+ " server time."));
                            }
                            else {
                                result.add(new BroadcastAction("Tournament " + getTournamentName() + " will start round "
                                        + (getCurrentRound() + 1) + " in " + DateUtils.HumanDuration(PairingDelayTime)
                                        + " at " + DateUtils.FormatTime(DateUtils.Now().plus(PairingDelayTime))+ " server time."));
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

    public TableDraft getTableDraft(String playerName) {
        if (tables == null) {
            return null;
        }
        return tables.get(playerName);
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
        return soloTableDraftInfo.generateCollectionInfo();
    }
}
