package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractTournamentQueue implements TournamentQueue {
    protected String _id;
    protected int _cost;
    protected Queue<String> _players = new LinkedList<>();
    protected String _playerList;
    protected Map<String, LotroDeck> _playerDecks = new HashMap<>();
    protected boolean _requiresDeck;

    private final CollectionType _currencyCollection = CollectionType.MY_CARDS;

    protected final PairingMechanism _pairingMechanism;
    protected final CollectionType _collectionType;
    protected final TournamentPrizes _tournamentPrizes;
    protected String _format;

    public AbstractTournamentQueue(String id, int cost, boolean requiresDeck, CollectionType collectionType, TournamentPrizes tournamentPrizes, PairingMechanism pairingMechanism, String format) {
        _id = id;
        _cost = cost;
        _requiresDeck = requiresDeck;
        _collectionType = collectionType;
        _tournamentPrizes = tournamentPrizes;
        _pairingMechanism = pairingMechanism;
        _format = format;
    }

    @Override
    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem();
    }

    @Override
    public final CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public final String getPrizesDescription() {
        return _tournamentPrizes.getPrizeDescription();
    }

    protected void regeneratePlayerList() {
        _playerList =  String.join(", ", new ArrayList<>(_players).stream().sorted().toList() );
    }

    @Override
    public final synchronized void joinPlayer(CollectionsManager collectionsManager, Player player, LotroDeck deck) throws SQLException, IOException {
        if (!_players.contains(player.getName()) && isJoinable()) {
            if (_cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection("Joined "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost)) {
                _players.add(player.getName());
                regeneratePlayerList();
                if (_requiresDeck)
                    _playerDecks.put(player.getName(), deck);
            }
        }
    }

    @Override
    public final synchronized void leavePlayer(CollectionsManager collectionsManager, Player player) throws SQLException, IOException {
        if (_players.contains(player.getName())) {
            if (_cost > 0)
                collectionsManager.addCurrencyToPlayerCollection(true, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost);
            _players.remove(player.getName());
            regeneratePlayerList();
            _playerDecks.remove(player.getName());
        }
    }

    @Override
    public final synchronized void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException {
        if (_cost > 0) {
            for (String player : _players)
                collectionsManager.addCurrencyToPlayerCollection(false, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost);
        }
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
        return _cost;
    }

    @Override
    public final boolean isRequiresDeck() {
        return _requiresDeck;
    }

    @Override
    public final String getFormat() {
        return _format;
    }
}
