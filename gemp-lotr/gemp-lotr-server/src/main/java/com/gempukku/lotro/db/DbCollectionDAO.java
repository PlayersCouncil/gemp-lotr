package com.gempukku.lotro.db;

import com.gempukku.lotro.collection.CollectionSerializer;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;
import org.json.simple.JSONObject;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbCollectionDAO implements CollectionDAO {
    private final DbAccess _dbAccess;
    private final CollectionSerializer _collectionSerializer;

    public DbCollectionDAO(DbAccess dbAccess, CollectionSerializer collectionSerializer) {
        _dbAccess = dbAccess;
        _collectionSerializer = collectionSerializer;
    }

    public Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws SQLException, IOException {
        var colls = getCollectionInfosByType(type);
        Map<Integer, CardCollection> result = new HashMap<>();

        for(var coll : colls) {
            result.put(coll.player_id, getCollection(coll));
        }

        return result;
    }

    public CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException {

        var collection = getCollectionInfo(playerId, type);
        if(collection == null)
            return null;

        var entries = extractCollectionEntries(collection.id);

        var result = _collectionSerializer.deserializeCollection(collection, entries);

        return result;
    }

    public CardCollection getCollection(int collectionID) throws SQLException, IOException {
        return getCollection(getCollectionInfo(collectionID));
    }

    public CardCollection getCollection(DBDefs.Collection coll) throws SQLException, IOException {

        var entries = extractCollectionEntries(coll.id);
        var result = _collectionSerializer.deserializeCollection(coll, entries);

        return result;
    }

    private List<DBDefs.CollectionEntry> extractCollectionEntries(int collectionID) throws SQLException, IOException {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            collection_id, 
                            quantity, 
                            product_type, 
                            product_variant, 
                            product, 
                            source, 
                            created_date, 
                            modified_date, 
                            notes
                        FROM gemp_db.collection_entries
                        WHERE collection_id = :collID;
                                                
                        """;
                List<DBDefs.CollectionEntry> result = conn.createQuery(sql)
                        .addParameter("collID", collectionID)
                        .executeAndFetch(DBDefs.CollectionEntry.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection entries", ex);
        }
    }

    public boolean doesPlayerHaveCardsInCollection(int playerId, String type) {
        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*)
                        FROM collection_entries ce
                        INNER JOIN collection c
                        	ON c.id = ce.collection_id
                        WHERE c.player_id = :playerID
                            AND c.type = :type
                        	AND ce.quantity > 0
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("playerID", playerId)
                        .addParameter("type", type)
                        .executeScalar(Integer.class);

                return result > 0;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to check if player has any cards in collection.", ex);
        }
    }

    @Override
    public List<DBDefs.Collection> getAllCollectionsForPlayer(int playerId) {

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE player_id = :playerID
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("playerID", playerId)
                        .executeAndFetch(DBDefs.Collection.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection types", ex);
        }
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int playerId, String type) {

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type
                            AND player_id = :playerID
                        LIMIT 1;
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("type", type)
                        .addParameter("playerID", playerId)
                        .executeAndFetch(DBDefs.Collection.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection info", ex);
        }
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int collectionID) {

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE collection_id = :collectionID
                        LIMIT 1;
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("collectionID", collectionID)
                        .executeAndFetch(DBDefs.Collection.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection info", ex);
        }
    }

    @Override
    public List<DBDefs.Collection> getCollectionInfosByType(String type) {

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type;
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("type", type)
                        .executeAndFetch(DBDefs.Collection.class);

                return result;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection infos", ex);
        }
    }

    public int getCollectionID(int playerId, String type)  {
        var coll = getCollectionInfo(playerId,  type);
        if(coll == null)
            return -1;

        return coll.id;
    }


    public void overwriteCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = :quantity, source = :source;
                        """;
        String error = "Unable to update product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void addToCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = quantity + :quantity, source = :source;
                        """;
        String error = "Unable to add product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = GREATEST(quantity - :quantity, 0), source = :source;
                        """;
        String error = "Unable to remove product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void convertCollection(int playerId, String type) throws SQLException, IOException {
        MutableCardCollection oldCollection = getOldPlayerCollection(playerId, type);
        var oldinfo = new HashMap<String, Object>(oldCollection.getExtraInformation());
        oldinfo.put(DefaultCardCollection.CurrencyKey, oldCollection.getCurrency());
        oldCollection.setExtraInformation(oldinfo);
        overwriteCollectionContents(playerId, type, oldCollection, "Initial Convert");
    }

    public MutableCardCollection getOldPlayerCollection(int playerId, String type) throws SQLException, IOException {
        try (Connection connection = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select collection from collection where player_id=? and type=?")) {
                statement.setInt(1, playerId);
                statement.setString(2, type);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Blob blob = rs.getBlob(1);
                        return extractCollectionAndClose(blob);
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private MutableCardCollection extractCollectionAndClose(Blob blob) throws SQLException, IOException {
        try {
            try (InputStream inputStream = blob.getBinaryStream()) {
                return _collectionSerializer.deserializeCollection(inputStream);
            }
        } finally {
            blob.free();
        }
    }

    @Override
    public void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        upsertCollectionInfo(playerId, type, extraInformation);
    }


    //TODO:
    // + Convert currency to an extra info entry
    // + add data field to the original collection table to hold the extra info as json
    // + create player-looping function to convert all collections and see if it blows up via test
    // + add CollectionsManager mirror functions to read/writing into the new table
    // + write unit tests to convert and compare a bajillion collections
    // + sunset the old collection handling functions
    // ~ test a: draft, new art card reward, my cards reward, pack openings
    // - write script to back up db and delete the old binary blob column
    // - write script to convert all leagues to use IDs instead of names
    private void updateCollectionContents(int playerId, String type, CardCollection collection, String source, String sql, String error) {
        if(getCollectionID(playerId, type) <= 0) {
            upsertCollectionInfo(playerId, type, collection.getExtraInformation());
        }
        int collID = getCollectionID(playerId, type);

        try {
            var db = _dbAccess.openDB();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                int i = 0;
                //TODO: maybe detect when the batch is 1000 entries long and execute then to prevent errors
                for(var card : collection.getAll()) {
                    query.addParameter("collid", collID)
                            .addParameter("quantity", card.getCount())
                            .addParameter("type", card.getType())
                            .addParameter("product", card.getBlueprintId())
                            .addParameter("source", source)
                            .addToBatch();
                }
                query.executeBatch();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException(error, ex);
        }
    }

    private void upsertCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        String sql = """
                        INSERT INTO collection(player_id, type, extra_info)
                        VALUES (:playerId, :type, :extraInfo)
                        ON DUPLICATE KEY UPDATE extra_info = :extraInfo;
                        """;
        String json = "";
        try {
            var db = _dbAccess.openDB();

            var jsonObj = new JSONObject();
            jsonObj.putAll(extraInformation);
            json = jsonObj.toJSONString();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("playerId", playerId)
                        .addParameter("type", type)
                        .addParameter("extraInfo", json);
                query.executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to upsert collection", ex);
        }
    }


}
