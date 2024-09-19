package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.BroadcastAction;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SealedTournament extends BaseTournament implements Tournament {

    private SealedTournamentInfo _sealedInfo;

    public SealedTournament(TournamentService tournamentService, CollectionsManager collectionsManager, ProductLibrary productLibrary,
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

    @Override
    public void issuePlayerMaterial(String player) {
        var collDef = _sealedInfo.generateCollectionInfo();
        var collections = _collectionsManager.getPlayersCollection(collDef.getCode());
        var sealedDef = _sealedInfo.SealedDefinition;

        if(collections.keySet().stream().anyMatch(x -> x.getName().equals(player)))
            return;

        var newCollection = new DefaultCardCollection();
        for (CardCollection.Item serieCollectionItem : sealedDef.GetProductForSerie(0)) {
            newCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());
        }

        _collectionsManager.addPlayerCollection(true, "Sealed tournament product", player, collDef, newCollection);
    }

    public void createAndPopulateCollections() {
        var collDef = _sealedInfo.generateCollectionInfo();
        var collections = _collectionsManager.getPlayersCollection(collDef.getCode());
        var sealedDef = _sealedInfo.SealedDefinition;

        for(var playerName : _players) {
            var player = collections.keySet().stream().filter(x -> x.getName().equals(playerName)).findFirst();
            if(player.isPresent())
                continue;

            var newCollection = new DefaultCardCollection();
            for (CardCollection.Item serieCollectionItem : sealedDef.GetProductForSerie(0)) {
                newCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());
            }

            _collectionsManager.addPlayerCollection(true, "Sealed tournament product", playerName, collDef, newCollection);
        }
    }

    public void disqualifyUnregisteredPlayers() {
        var players = _tournamentService.retrieveTournamentPlayers(_tournamentId);

        for(var playerName : players) {
            var deck = getPlayerDeck(playerName);
            if(deck == null || StringUtils.isEmpty(deck.getDeckName())) {
                dropPlayer(playerName);
            }
        }
    }

    //No locking because it is handled in the function that calls this one
    protected void resumeTournamentFromDatabase() {
        if (getTournamentStage() == Stage.DECK_BUILDING) {
            createAndPopulateCollections();
        }
        if (getTournamentStage() == Stage.DECK_REGISTRATION) {

        }
        else if (_tournamentInfo.Stage == Stage.PLAYING_GAMES) {
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
        try {
            List<TournamentProcessAction> result = new LinkedList<>();
            if (_nextTask == null) {
                if(getTournamentStage() == Stage.STARTING) {
                    _tournamentInfo.Stage = Stage.DECK_BUILDING;
                    _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                    String duration = DateUtils.HumanDuration(_sealedInfo.DeckbuildingDuration);
                    result.add(new BroadcastAction("Sealed product has been issued for tournament <b>" + getTournamentName() + "</b>.  Players now have "
                            + duration + " to open packs and build a deck with the cards you open. "
                            + "<br/><br/>Remember to return to the game hall and register your deck before " + DateUtils.FormatTime(_sealedInfo.RegistrationDeadline) + "."));
                }
                else if (getTournamentStage() == Stage.DECK_BUILDING) {
                    if (DateUtils.Now().isAfter(_sealedInfo.DeckbuildingDeadline) && _playerDecks.values().stream().anyMatch(x -> StringUtils.isEmpty(x.getDeckName()))) {
                        _tournamentInfo.Stage = Stage.DECK_REGISTRATION;
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());

                        String duration = DateUtils.HumanDuration(_sealedInfo.RegistrationDuration);
                        result.add(new BroadcastAction("Deck building in tournament <b>" + getTournamentName() + "</b> has finished.  Players now have "
                                + duration + " to finish registering their decks.  Any player who has not turned in their deck by the deadline at "
                                + DateUtils.FormatTime(_sealedInfo.RegistrationDeadline) + " will be auto-disqualified."
                                + "<br/><br/>Once all players have turned in decks or the deadline has passed, the tournament will begin."));
                    }
                    else if (DateUtils.Now().isAfter(_sealedInfo.DeckbuildingDeadline) && _playerDecks.values().stream().noneMatch(x -> StringUtils.isEmpty(x.getDeckName()))) {
                        _tournamentInfo.Stage = _sealedInfo.PostRegistrationStage();
                        _tournamentService.recordTournamentStage(_tournamentId, getTournamentStage());
                    }
                }

                if (getTournamentStage() == Stage.DECK_REGISTRATION) {
                    if (DateUtils.Now().isAfter(_sealedInfo.RegistrationDeadline) || _playerDecks.values().stream().noneMatch(x -> StringUtils.isEmpty(x.getDeckName()))) {
                        disqualifyUnregisteredPlayers();

                        _tournamentInfo.Stage = _sealedInfo.PostRegistrationStage();
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

    @Override
    public Draft getDraft() {
        return null;
    }

    @Override
    public void playerChosenCard(String playerName, String cardId) {
        //This is for draft only
    }

}
