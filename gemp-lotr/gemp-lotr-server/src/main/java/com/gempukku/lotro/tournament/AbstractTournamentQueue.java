package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractTournamentQueue implements TournamentQueue {
    protected final String _id;
    protected final String _tournamentQueueName;
    protected Queue<String> _players = new LinkedList<>();
    protected String _playerList;
    protected Map<String, LotroDeck> _playerDecks = new HashMap<>();

    private final CollectionType _currencyCollection = CollectionType.MY_CARDS;

    protected TournamentInfo _tournamentInfo;

    protected final TournamentService _tournamentService;

    public AbstractTournamentQueue(TournamentService tournamentService, String queueId, String queueName, TournamentInfo info) {
        _tournamentService = tournamentService;
        _id = queueId;
        _tournamentQueueName = queueName;
        _tournamentInfo = info;
    }

    @Override
    public TournamentInfo getInfo() { return _tournamentInfo; }

    @Override
    public String getTournamentQueueName() {
        return _tournamentQueueName;
    }

    @Override
    public String getPairingDescription() {
        return _tournamentInfo.PairingMechanism.getPlayOffSystem();
    }

    @Override
    public final CollectionType getCollectionType() {
        return _tournamentInfo.Collection;
    }

    @Override
    public final String getPrizesDescription() {
        return _tournamentInfo.Prizes.getPrizeDescription();
    }

    protected void regeneratePlayerList() {
        _playerList =  String.join(", ", new ArrayList<>(_players).stream().sorted().toList() );
    }

    @Override
    public final synchronized void joinPlayer(CollectionsManager collectionsManager, Player player, LotroDeck deck) throws SQLException, IOException {
        if (!_players.contains(player.getName()) && isJoinable()) {
            if (_tournamentInfo._params.cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection("Joined "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost)) {
                _players.add(player.getName());
                regeneratePlayerList();
                if (_tournamentInfo._params.requiresDeck)
                    _playerDecks.put(player.getName(), deck);
            }
        }
    }

    @Override
    public final synchronized void leavePlayer(CollectionsManager collectionsManager, Player player) throws SQLException, IOException {
        if (_players.contains(player.getName())) {
            if (_tournamentInfo._params.cost > 0)
                collectionsManager.addCurrencyToPlayerCollection(true, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost);
            _players.remove(player.getName());
            regeneratePlayerList();
            _playerDecks.remove(player.getName());
        }
    }

    @Override
    public final synchronized void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException {
        if (_tournamentInfo._params.cost > 0) {
            for (String player : _players)
                collectionsManager.addCurrencyToPlayerCollection(false, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _tournamentInfo._params.cost);
        }
        clearPlayersInternal();
    }

    protected void clearPlayersInternal() {
        _players.clear();
        regeneratePlayerList();
        _playerDecks.clear();
    }

    @Override
    public final synchronized int getPlayerCount() {
        return _players.size();
    }

    @Override
    public String getPlayerList() {
        return _playerList;
    }

    @Override
    public final synchronized boolean isPlayerSignedUp(String player) {
        return _players.contains(player);
    }

    @Override
    public final String getID() {
        return _id;
    }

    @Override
    public final int getCost() {
        return _tournamentInfo._params.cost;
    }

    @Override
    public final boolean isRequiresDeck() {
        return _tournamentInfo._params.requiresDeck;
    }

    @Override
    public final String getFormatCode() {
        return _tournamentInfo.Format.getCode();
    }
}
