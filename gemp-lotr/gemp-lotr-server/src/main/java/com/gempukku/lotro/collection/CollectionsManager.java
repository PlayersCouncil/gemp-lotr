package com.gempukku.lotro.collection;

import com.gempukku.lotro.db.CollectionDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.packs.ProductLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionsManager {
    private final ReentrantReadWriteLock _readWriteLock = new ReentrantReadWriteLock();

    private final PlayerDAO _playerDAO;
    private final CollectionDAO _collectionDAO;
    private final TransferDAO _transferDAO;
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;

    private CardCollection _defaultCollection;
    private CardCollection _overwriteCollection;

    public CollectionsManager(PlayerDAO playerDAO, CollectionDAO collectionDAO, TransferDAO transferDAO, final LotroCardBlueprintLibrary lotroCardBlueprintLibrary,
                              final ProductLibrary productLibrary) {
        _playerDAO = playerDAO;
        _collectionDAO = collectionDAO;
        _transferDAO = transferDAO;
        _cardLibrary = lotroCardBlueprintLibrary;
        _productLibrary = productLibrary;

        _defaultCollection = new CompleteCardCollection(_cardLibrary);
        var replacement = new DefaultCardCollection();
        //Hack to get a 0-quantity collection entry
        replacement.addItem("1_1", 1);
        replacement.removeItem("1_1", 1);
        _overwriteCollection = replacement;

        _cardLibrary.subscribeToRefreshes(() -> _defaultCollection = new CompleteCardCollection(_cardLibrary));
    }

    public CardCollection getCompleteCardCollection() {
        return _defaultCollection;
    }

    public CardCollection getPlayerCollection(String playerName, String collectionType) {
        return getPlayerCollection(_playerDAO.getPlayer(playerName), collectionType);
    }

    public CardCollection getPlayerCollection(Player player, String collectionType) {
        _readWriteLock.readLock().lock();
        try {
            if (collectionType.contains("+"))
                return createSumCollection(player, collectionType.split("\\+"));

            if (collectionType.equals("default"))
                return getCompleteCardCollection();

            final CardCollection collection = _collectionDAO.getPlayerCollection(player.getId(), collectionType);

            if (collection == null && (collectionType.equals(CollectionType.MY_CARDS.getCode()) || collectionType.equals(CollectionType.TROPHY.getCode())))
                return new DefaultCardCollection();

            return collection;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get player collection", exp);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }

    public void migratePlayerCollection(Player player) {
        _readWriteLock.readLock().lock();
        try {
            if(!playerCollectionAvailableForMigration(player))
                return;

            var combined = createSumCollection(player,
                    new String[] {CollectionType.MY_CARDS.getCode(), CollectionType.TROPHY.getCode()});

            _collectionDAO.overwriteCollectionContents(player.getId(), CollectionType.MY_CARDS.getCode(),
                    combined, "Player requested trophy -> My Cards migration.");
            _collectionDAO.removeFromCollectionContents(player.getId(), CollectionType.TROPHY.getCode(),
                    combined, "Removing as part of migration.");
            _collectionDAO.removeFromCollectionContents(player.getId(), CollectionType.TROPHY.getCode(),
                    combined, "Removing as part of migration.");
            _collectionDAO.overwriteCollectionContents(player.getId(), CollectionType.TROPHY.getCode(),
                    _overwriteCollection, "Player requested trophy -> My Cards migration.");

            player.setHasTrophies(false);
        }
        catch(SQLException | IOException ex) {
            throw new RuntimeException("Unable to migrate player collection", ex);
        }
        finally {
            _readWriteLock.readLock().unlock();
        }
    }

    //Used to see if the migrate collection button should be available on the player's dashboard.
    public boolean playerCollectionAvailableForMigration(Player player) {
        return _collectionDAO.doesPlayerHaveCardsInCollection(player.getId(), CollectionType.TROPHY.getCode());
    }

    private CardCollection createSumCollection(Player player, String[] collectionTypes) {
        List<CardCollection> collections = new LinkedList<>();
        for (String collectionType : collectionTypes)
            collections.add(getPlayerCollection(player, collectionType));

        return new SumCardCollection(collections);
    }

    private void overwritePlayerCollection(Player player, String collectionType, CardCollection cardCollection, String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.overwriteCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    private void addToPlayerCollection(Player player, String collectionType, CardCollection cardCollection, String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.addToCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    private void removeFromPlayerCollection(Player player, String collectionType, CardCollection cardCollection, String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.removeFromCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    public void addPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, CardCollection cardCollection) {
        addPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, cardCollection);
    }

    public void addPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, CardCollection cardCollection) {
        if (collectionType.getCode().contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.writeLock().lock();
        try {
            overwritePlayerCollection(player, collectionType.getCode(), cardCollection, reason);
            _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), cardCollection.getCurrency(), cardCollection);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public Map<Player, CardCollection> getPlayersCollection(String collectionType) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.readLock().lock();
        try {
            final Map<Integer, CardCollection> playerCollectionsByType = _collectionDAO.getPlayerCollectionsByType(collectionType);

            Map<Player, CardCollection> result = new HashMap<>();
            for (Map.Entry<Integer, CardCollection> playerCollection : playerCollectionsByType.entrySet())
                result.put(_playerDAO.getPlayer(playerCollection.getKey()), playerCollection.getValue());

            return result;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get players collection", exp);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }

    public CardCollection openPackInPlayerCollection(Player player, CollectionType collectionType, String selection, ProductLibrary productLibrary, String packId) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection == null)
                return null;
            MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);

            final CardCollection packContents = mutableCardCollection.openPack(packId, selection, productLibrary);
            if (packContents != null) {
                String reason = "Opened pack";

                removeFromPlayerCollection(player, collectionType.getCode(), cardCollectionFromBlueprintId(1, packId), reason);
                addToPlayerCollection(player, collectionType.getCode(), packContents, reason);

                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, packId));
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), packContents.getCurrency(), packContents);
            }
            return packContents;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    private CardCollection cardCollectionFromBlueprintId(int count, String blueprintId) {
        DefaultCardCollection result = new DefaultCardCollection();
        result.addItem(blueprintId, count);
        return result;
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, Iterable<CardCollection.Item> items, Map<String, Object> extraInformation){
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                MutableCardCollection addedCards = new DefaultCardCollection();
                for (CardCollection.Item item : items) {
                    mutableCardCollection.addItem(item.getBlueprintId(), item.getCount());
                    addedCards.addItem(item.getBlueprintId(), item.getCount());
                }

                if (extraInformation != null) {
                    Map<String, Object> resultExtraInformation = new HashMap<>(playerCollection.getExtraInformation());
                    resultExtraInformation.putAll(extraInformation);
                    _collectionDAO.updateCollectionInfo(player.getId(), collectionType.getCode(), resultExtraInformation);
                }

                addToPlayerCollection(player, collectionType.getCode(), addedCards, reason);

                _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), 0, addedCards);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Could not add items to player collection", e);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, Iterable<CardCollection.Item> items)  {
        addItemsToPlayerCollection(notifyPlayer, reason, player, collectionType, items, null);
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, Iterable<CardCollection.Item> items)  {
        addItemsToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, items);
    }

    public boolean tradeCards(Player player, CollectionType collectionType, String removeBlueprintId, int removeCount, String addBlueprintId, int addCount, int currencyCost) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(removeBlueprintId, removeCount))
                    return false;
                if (!mutableCardCollection.removeCurrency(currencyCost))
                    return false;
                mutableCardCollection.addItem(addBlueprintId, addCount);

                String reason = "Trading items";

                removeFromPlayerCollection(player, collectionType.getCode(), cardCollectionFromBlueprintId(removeCount, removeBlueprintId), reason);
                addToPlayerCollection(player, collectionType.getCode(), cardCollectionFromBlueprintId(addCount, addBlueprintId), reason);
                removeCurrencyFromPlayerCollection(reason, player, collectionType, currencyCost);

                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currencyCost, cardCollectionFromBlueprintId(removeCount, removeBlueprintId));
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(addCount, addBlueprintId));

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean buyCardToPlayerCollection(Player player, CollectionType collectionType, String blueprintId, int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeCurrency(currency))
                    return false;
                mutableCardCollection.addItem(blueprintId, 1);

                String reason = "Items bought";

                removeCurrencyFromPlayerCollection(reason, player, collectionType, currency);
                addToPlayerCollection(player, collectionType.getCode(), cardCollectionFromBlueprintId(1, blueprintId), reason);

                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, blueprintId));

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean sellCardInPlayerCollection(Player player, CollectionType collectionType, String blueprintId, int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(blueprintId, 1))
                    return false;

                String reason = "Selling items";

                addCurrencyToPlayerCollection(false, reason, player, collectionType, currency);

                removeFromPlayerCollection(player, collectionType.getCode(), cardCollectionFromBlueprintId(1, blueprintId), reason);

                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, blueprintId));
                _transferDAO.addTransferTo(false, player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, int currency) throws SQLException, IOException {
        addCurrencyToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, currency);
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, int currency) throws SQLException, IOException {
        if (currency > 0) {
            _readWriteLock.writeLock().lock();
            try {
                final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
                if (playerCollection != null) {
                    MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                    mutableCardCollection.addCurrency(currency);

                    _collectionDAO.updateCollectionInfo(player.getId(), collectionType.getCode(), mutableCardCollection.getExtraInformation());

                    _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());
                }
            } finally {
                _readWriteLock.writeLock().unlock();
            }
        }
    }

    public boolean removeCurrencyFromPlayerCollection(String reason, Player player, CollectionType collectionType, int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (mutableCardCollection.removeCurrency(currency)) {
                    _collectionDAO.updateCollectionInfo(player.getId(), collectionType.getCode(), mutableCardCollection.getExtraInformation());

                    _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());

                    return true;
                }
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }
}
