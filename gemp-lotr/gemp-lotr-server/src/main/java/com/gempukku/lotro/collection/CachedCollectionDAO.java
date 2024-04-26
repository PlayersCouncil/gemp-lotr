package com.gempukku.lotro.collection;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.CollectionDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CachedCollectionDAO implements CollectionDAO, Cached {
    private final CollectionDAO _delegate;
    private final Map<String, CardCollection> _playerCollections = Collections.synchronizedMap(new LRUMap(100));

    public CachedCollectionDAO(CollectionDAO delegate) {
        _delegate = delegate;
    }

    @Override
    public void clearCache() {
        _playerCollections.clear();
    }

    @Override
    public int getItemCount() {
        return _playerCollections.size();
    }

    @Override
    public CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException {
        String key = constructCacheKey(playerId, type);
        CardCollection collection = (CardCollection) _playerCollections.get(key);
        if (collection == null) {
            collection = _delegate.getPlayerCollection(playerId, type);
            _playerCollections.put(key, collection);
        }
        return collection;
    }

    @Override
    public boolean doesPlayerHaveCardsInCollection(int playerId, String type) {
        String key = constructCacheKey(playerId, type);

        if(_playerCollections.containsKey(key)) {
            var coll = _playerCollections.get(key);
            if(coll == null)
                return _delegate.doesPlayerHaveCardsInCollection(playerId, type);;

            for (CardCollection.Item item : coll.getAll()) {
                if (item.getCount() > 0) {
                    return true;
                }
            }
            return false;
        }
        return _delegate.doesPlayerHaveCardsInCollection(playerId, type);
    }

    private String constructCacheKey(int playerId, String type) {
        return playerId +"-"+type;
    }

    @Override
    public Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws SQLException, IOException {
        return _delegate.getPlayerCollectionsByType(type);
    }



    @Override
    public void overwriteCollectionContents(int playerId, String type, CardCollection collection, String reason) throws SQLException, IOException {
        _delegate.overwriteCollectionContents(playerId, type, collection, reason);
        recacheCollection(playerId, type);
    }

    @Override
    public void convertCollection(int playerId, String type) throws SQLException, IOException {
        _delegate.convertCollection(playerId, type);
    }

    @Override
    public List<DBDefs.Collection> getAllCollectionsForPlayer(int playerId) {
        return _delegate.getAllCollectionsForPlayer(playerId);
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int playerId, String type) {
        return _delegate.getCollectionInfo(playerId, type);
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int collectionID) {
        return _delegate.getCollectionInfo(collectionID);
    }

    @Override
    public List<DBDefs.Collection> getCollectionInfosByType(String type) {
        return _delegate.getCollectionInfosByType(type);
    }

    @Override
    public void addToCollectionContents(int playerId, String type, CardCollection collection, String source) throws SQLException, IOException {
        _delegate.addToCollectionContents(playerId, type, collection, source);
        recacheCollection(playerId, type);
    }

    @Override
    public void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source) throws SQLException, IOException {
        _delegate.removeFromCollectionContents(playerId, type, collection, source);
        recacheCollection(playerId, type);
    }

    @Override
    public void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) throws SQLException, IOException {
        _delegate.updateCollectionInfo(playerId, type, extraInformation);
        recacheCollection(playerId, type);
    }

    private void recacheCollection(int playerId, String type) throws SQLException, IOException {
        String id = constructCacheKey(playerId, type);
        _playerCollections.put(id, _delegate.getPlayerCollection(playerId, type));
    }
}
