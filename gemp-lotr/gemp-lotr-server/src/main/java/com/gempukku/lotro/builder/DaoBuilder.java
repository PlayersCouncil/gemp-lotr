package com.gempukku.lotro.builder;

import com.gempukku.lotro.cache.CacheManager;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.CachedCollectionDAO;
import com.gempukku.lotro.collection.CachedTransferDAO;
import com.gempukku.lotro.collection.CollectionSerializer;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.db.*;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.log.LoggingProxy;
import com.gempukku.lotro.tournament.TournamentDAO;
import com.gempukku.lotro.tournament.TournamentMatchDAO;
import com.gempukku.lotro.tournament.TournamentPlayerDAO;

import java.lang.reflect.Type;
import java.util.Map;

public class DaoBuilder {
    public static void CreateDatabaseAccessObjects(Map<Type, Object> objectMap) {
        DbAccess dbAccess = new DbAccess();

        objectMap.put(LeagueParticipationDAO.class, LoggingProxy.createLoggingProxy(LeagueParticipationDAO.class, new DbLeagueParticipationDAO(dbAccess)));
        objectMap.put(LeagueMatchDAO.class, LoggingProxy.createLoggingProxy(LeagueMatchDAO.class, new DbLeagueMatchDAO(dbAccess)));
        objectMap.put(TournamentDAO.class, LoggingProxy.createLoggingProxy(TournamentDAO.class, new DbTournamentDAO(dbAccess)));
        objectMap.put(TournamentPlayerDAO.class, LoggingProxy.createLoggingProxy(TournamentPlayerDAO.class, new DbTournamentPlayerDAO(dbAccess)));
        objectMap.put(TournamentMatchDAO.class, LoggingProxy.createLoggingProxy(TournamentMatchDAO.class, new DbTournamentMatchDAO(dbAccess)));

        MerchantDAO dbMerchantDao = LoggingProxy.createLoggingProxy(MerchantDAO.class, new DbMerchantDAO(dbAccess));
        CachedMerchantDAO merchantDao = new CachedMerchantDAO(dbMerchantDao);
        objectMap.put(MerchantDAO.class, merchantDao);

        objectMap.put(LeagueDAO.class, LoggingProxy.createLoggingProxy(LeagueDAO.class, new DbLeagueDAO(dbAccess)));
        objectMap.put(GameHistoryDAO.class, LoggingProxy.createLoggingProxy(GameHistoryDAO.class, new DbGameHistoryDAO(dbAccess)));

        IgnoreDAO dbIgnoreDao = LoggingProxy.createLoggingProxy(IgnoreDAO.class, new DbIgnoreDAO(dbAccess));
        CachedIgnoreDAO ignoreDao = new CachedIgnoreDAO(dbIgnoreDao);
        objectMap.put(IgnoreDAO.class, ignoreDao);

        DeckDAO dbDeckDao = LoggingProxy.createLoggingProxy(DeckDAO.class, new DbDeckDAO(dbAccess, extract(objectMap, LotroCardBlueprintLibrary.class)));
        CachedDeckDAO deckDao = new CachedDeckDAO(dbDeckDao);
        objectMap.put(DeckDAO.class, deckDao);

        CollectionDAO dbCollectionDao = LoggingProxy.createLoggingProxy(CollectionDAO.class, new DbCollectionDAO(dbAccess, extract(objectMap, CollectionSerializer.class)));
        CachedCollectionDAO collectionDao = new CachedCollectionDAO(dbCollectionDao);
        objectMap.put(CollectionDAO.class, collectionDao);

        PlayerDAO dbPlayerDao = LoggingProxy.createLoggingProxy(PlayerDAO.class, new DbPlayerDAO(dbAccess, collectionDao));
        CachedPlayerDAO playerDao = new CachedPlayerDAO(dbPlayerDao);
        objectMap.put(PlayerDAO.class, playerDao);

        TransferDAO dbTransferDao = LoggingProxy.createLoggingProxy(TransferDAO.class, new DbTransferDAO(dbAccess));
        CachedTransferDAO transferDao = new CachedTransferDAO(dbTransferDao, extract(objectMap, MarkdownParser.class));
        objectMap.put(TransferDAO.class, transferDao);

        IpBanDAO dbIpBanDao = LoggingProxy.createLoggingProxy(IpBanDAO.class, new DbIpBanDAO(dbAccess));
        CachedIpBanDAO ipBanDao = new CachedIpBanDAO(dbIpBanDao);
        objectMap.put(IpBanDAO.class, ipBanDao);

        CacheManager cacheManager = new CacheManager();
        cacheManager.addCache(merchantDao);
        cacheManager.addCache(deckDao);
        cacheManager.addCache(collectionDao);
        cacheManager.addCache(playerDao);
        cacheManager.addCache(transferDao);
        cacheManager.addCache(ipBanDao);
        objectMap.put(CacheManager.class, cacheManager);
    }

    private static <T> T extract(Map<Type, Object> objectMap, Class<T> clazz) {
        T result = (T) objectMap.get(clazz);
        if (result == null)
            throw new RuntimeException("Unable to find class " + clazz.getName());
        return result;
    }
}
