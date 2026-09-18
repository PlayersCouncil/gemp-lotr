package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.CardCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface CollectionDAO {
    public Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws SQLException, IOException;
    public boolean doesPlayerHaveCardsInCollection(int playerId, String type);

    public CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException;

    public void overwriteCollectionContents(int playerId, String type, CardCollection collection, String reason) throws SQLException, IOException;

    void convertCollection(int playerId, String type) throws SQLException, IOException;

    List<DBDefs.Collection> getAllCollectionsForPlayer(int playerId);

    DBDefs.Collection getCollectionInfo(int playerId, String type);

    DBDefs.Collection getCollectionInfo(int collectionID);

    List<DBDefs.Collection> getCollectionInfosByType(String type);

    void addToCollectionContents(int playerId, String type, CardCollection collection, String source) throws SQLException, IOException;

    void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source) throws SQLException, IOException;

    void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) throws SQLException, IOException;

    /**
     * Finds everyone holding a product, in any collection.  {@code product} is matched exactly against
     * {@code collection_entries.product}, which stores the full blueprint id as awarded (modifiers such as the
     * foil {@code *} included), so pass the id exactly as it was added.
     * @return one entry per (player, collection type) with a positive quantity
     */
    List<DBDefs.CollectionHolder> findHolders(String product);
}
