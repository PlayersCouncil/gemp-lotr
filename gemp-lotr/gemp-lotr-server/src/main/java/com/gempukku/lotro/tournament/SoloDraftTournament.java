package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class SoloDraftTournament extends BaseTournament implements Tournament {

    private static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;
    private SoloDraftTournamentInfo _soloDraftInfo;
    private ZonedDateTime nextRoundStart = null;

    public SoloDraftTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
                               LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, TableDraftDefinitions tableDraftDefinitions, TableHolder tables, String tournamentId) {
        super(tournamentService, collectionsManager, productLibrary, formatLibrary, soloDraftDefinitions, tableDraftDefinitions, tables, tournamentId);
    }

    @Override
    protected void recreateTournamentInfo(DBDefs.Tournament data) {
        _soloDraftInfo = new SoloDraftTournamentInfo(_tournamentService, _productLibrary, _formatLibrary, data, _soloDraftLibrary);
        _tournamentInfo = _soloDraftInfo;
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
                    _tournamentInfo.Stage = _soloDraftInfo.PostRegistrationStage();
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
            createStartingCollections();
        } else if (getTournamentStage() == Stage.DECK_REGISTRATION) {
            createStartingCollections();
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

    private void createStartingCollections() {
        var collDef = _soloDraftInfo.generateCollectionInfo();
        var collections = _collectionsManager.getPlayersCollection(collDef.getCode());

        for(var playerName : _players) {
            // check if player already has a collection
            var player = collections.keySet().stream().filter(x -> x.getName().equals(playerName)).findFirst();
            if(player.isPresent()) {
                continue;
            }

            // create new collection for the player
            var startingCollection = new DefaultCardCollection();

            // initialize with starting cards
            long seed = getSeed(playerName, collDef);
            CardCollection leagueProduct = _soloDraftLibrary.getSoloDraft(_soloDraftInfo._soloDraftParams.soloDraftFormatCode).initializeNewCollection(seed);
            for (CardCollection.Item serieCollectionItem : leagueProduct.getAll())
                startingCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());
            startingCollection.setExtraInformation(createExtraInformation(seed));

            _collectionsManager.addPlayerCollection(false, "Draft tournament product", playerName, collDef, startingCollection);
        }
    }

    private long getSeed(String playerName, CollectionType collectionType) {
        // random seed based on tournament id and player's name
        return collectionType.getCode().hashCode() + playerName.hashCode() * HIGH_ENOUGH_PRIME_NUMBER;
    }

    private Map<String, Object> createExtraInformation(long seed) {
        Map<String, Object> extraInformation = new HashMap<>();
        extraInformation.put("finished", false);
        extraInformation.put("stage", 0);
        extraInformation.put("seed", seed);
        // the current time is used in solo draft leagues, so i used it here too
        extraInformation.put("draftPool", _soloDraftLibrary.getSoloDraft(_soloDraftInfo._soloDraftParams.soloDraftFormatCode).initializeDraftPool(seed, System.currentTimeMillis()));
        return extraInformation;
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
                    _tournamentInfo.Stage = Stage.DECK_BUILDING;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                    createStartingCollections();

                    String duration = DateUtils.HumanDuration(_soloDraftInfo.DeckbuildingDuration);
                    result.add(new BroadcastAction("Draft has been opened for tournament <b>" + getTournamentName() + "</b>. Use the 'Go to Draft' button in the Active Tournaments Section. Players now have "
                            + duration + " to solo draft and build a deck with the cards they got. "
                            + "<br/><br/>Remember to return to the game hall and register your deck before " + DateUtils.FormatTime(_soloDraftInfo.RegistrationDeadline) + ".", activePlayers));
                }
                else if (getTournamentStage() == Stage.DECK_BUILDING) {
                    if (DateUtils.Now().isAfter(_soloDraftInfo.DeckbuildingDeadline)) {
                        _tournamentInfo.Stage = Stage.DECK_REGISTRATION;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                        String duration = DateUtils.HumanDuration(_soloDraftInfo.RegistrationDuration);
                        result.add(new BroadcastAction("Deck building in tournament <b>" + getTournamentName() + "</b> has finished. Players now have "
                                + duration + " to finish registering their decks.  Any player who has not turned in their deck by the deadline at "
                                + DateUtils.FormatTime(_soloDraftInfo.RegistrationDeadline) + " will be auto-disqualified."
                                + "<br/><br/>Once the deadline has passed, the tournament will begin.", activePlayers));
                    }
                }

                if (getTournamentStage() == Stage.DECK_REGISTRATION) {
                    if (DateUtils.Now().isAfter(_soloDraftInfo.RegistrationDeadline)) {
                        disqualifyUnregisteredPlayers();

                        _tournamentInfo.Stage = _soloDraftInfo.PostRegistrationStage();
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

    public SoloDraft getSoloDraft() {
        return _soloDraftLibrary.getSoloDraft(_soloDraftInfo._soloDraftParams.soloDraftFormatCode);
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
        return _soloDraftInfo.generateCollectionInfo();
    }

    @Override
    public boolean isJoinable() {
        Set<String> activePlayers = new HashSet<>(_players);
        activePlayers.removeAll(_droppedPlayers);
        int maximumPlayers = _tournamentInfo._params.maximumPlayers;
        return (getTournamentStage() == Stage.STARTING || getTournamentStage() == Stage.DECK_BUILDING || getTournamentStage() == Stage.DECK_REGISTRATION ||
                getTournamentStage() == Tournament.Stage.PAUSED || getTournamentStage() == Tournament.Stage.AWAITING_KICKOFF)
                && (maximumPlayers > activePlayers.size() || maximumPlayers < 0);
    }

    @Override
    public long getSecondsRemaining() throws IllegalStateException {
        if (getTournamentStage() == Stage.DECK_BUILDING) {
            return Duration.between(DateUtils.Now(), _soloDraftInfo.DeckbuildingDeadline).getSeconds();
        } else if (getTournamentStage() == Stage.DECK_REGISTRATION) {
            return Duration.between(DateUtils.Now(), _soloDraftInfo.RegistrationDeadline).getSeconds();
        } else if (getTournamentStage() == Stage.PLAYING_GAMES && nextRoundStart != null && DateUtils.Now().isBefore(nextRoundStart)) {
            return Duration.between(DateUtils.Now(), nextRoundStart).getSeconds();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String getTableDescription() {
        if (_soloDraftInfo._params.prizes == PrizeType.NONE && _soloDraftInfo._params.cost == 0) {
            return "Casual - " + _soloDraftLibrary.getSoloDraft(_soloDraftInfo._soloDraftParams.soloDraftFormatCode).getCode();
        } else {
            return "Competitive - " + _soloDraftLibrary.getSoloDraft(_soloDraftInfo._soloDraftParams.soloDraftFormatCode).getCode();
        }
    }
}
