package com.gempukku.lotro.builder;

import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.CollectionSerializer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.db.*;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.merchant.MerchantService;
import com.gempukku.lotro.packs.DraftPackStorage;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.service.LoggedUserHolder;
import com.gempukku.lotro.tournament.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ServerBuilder {
    public static void CreatePrerequisites(Map<Type, Object> objectMap) {
        final LotroCardBlueprintLibrary library = new LotroCardBlueprintLibrary();
        objectMap.put(LotroCardBlueprintLibrary.class, library);
        objectMap.put(ProductLibrary.class, new ProductLibrary(library));

        LoggedUserHolder loggedUserHolder = new LoggedUserHolder();
        loggedUserHolder.start();
        objectMap.put(LoggedUserHolder.class, loggedUserHolder);

        CollectionSerializer collectionSerializer = new CollectionSerializer();
        objectMap.put(CollectionSerializer.class, collectionSerializer);

        MarkdownParser parser = new MarkdownParser();
        objectMap.put(MarkdownParser.class, parser);
    }

    public static void CreateServices(Map<Type, Object> objectMap) {
        objectMap.put(AdventureLibrary.class,
                new DefaultAdventureLibrary());

        objectMap.put(LotroFormatLibrary.class,
                new LotroFormatLibrary(
                        extract(objectMap, AdventureLibrary.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class)));

        objectMap.put(GameHistoryService.class,
                new GameHistoryService(
                        extract(objectMap, GameHistoryDAO.class)));
        objectMap.put(GameRecorder.class,
                new GameRecorder(
                        extract(objectMap, GameHistoryService.class),
                        extract(objectMap, PlayerDAO.class)));

        objectMap.put(CollectionsManager.class,
                new CollectionsManager(
                        extract(objectMap, PlayerDAO.class),
                        extract(objectMap, CollectionDAO.class),
                        extract(objectMap, TransferDAO.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, ProductLibrary.class)));

        objectMap.put(SoloDraftDefinitions.class,
                new SoloDraftDefinitions(
                    extract(objectMap, CollectionsManager.class),
                    extract(objectMap, LotroCardBlueprintLibrary.class),
                    extract(objectMap, LotroFormatLibrary.class)
                ));

        objectMap.put(LeagueService.class,
                new LeagueService(
                        extract(objectMap, LeagueDAO.class),
                        extract(objectMap, LeagueMatchDAO.class),
                        extract(objectMap, LeagueParticipationDAO.class),
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, LotroFormatLibrary.class),
                        extract(objectMap, ProductLibrary.class),
                        extract(objectMap, SoloDraftDefinitions.class)));

        objectMap.put(AdminService.class,
                new AdminService(
                        extract(objectMap, PlayerDAO.class),
                        extract(objectMap, IpBanDAO.class),
                        extract(objectMap, LoggedUserHolder.class)
                ));

        objectMap.put(TournamentService.class,
                new TournamentService(
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, ProductLibrary.class),
                        new DraftPackStorage(),
                        extract(objectMap, TournamentDAO.class),
                        extract(objectMap, TournamentPlayerDAO.class),
                        extract(objectMap, TournamentMatchDAO.class),
                        extract(objectMap, GameHistoryDAO.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, LotroFormatLibrary.class)));

        objectMap.put(MerchantService.class,
                new MerchantService(
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, CollectionsManager.class)));

        objectMap.put(ChatServer.class, new ChatServer(
                extract(objectMap, IgnoreDAO.class),
                extract(objectMap, PlayerDAO.class)));

        objectMap.put(LotroServer.class,
                new LotroServer(
                        extract(objectMap, DeckDAO.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, ChatServer.class),
                        extract(objectMap, GameRecorder.class)));

        objectMap.put(HallServer.class,
                new HallServer(
                        extract(objectMap, IgnoreDAO.class),
                        extract(objectMap, LotroServer.class),
                        extract(objectMap, ChatServer.class),
                        extract(objectMap, LeagueService.class),
                        extract(objectMap, TournamentService.class),
                        extract(objectMap, LotroCardBlueprintLibrary.class),
                        extract(objectMap, LotroFormatLibrary.class),
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, AdminService.class)
                ));
    }

    private static <T> T extract(Map<Type, Object> objectMap, Class<T> clazz) {
        T result = (T) objectMap.get(clazz);
        if (result == null)
            throw new RuntimeException("Unable to find class " + clazz.getName());
        return result;
    }

    public static void StartServers(Map<Type, Object> objectMap) {
        extract(objectMap, HallServer.class).startServer();
        extract(objectMap, LotroServer.class).startServer();
        extract(objectMap, ChatServer.class).startServer();
    }

    public static void StopServers(Map<Type, Object> objectMap) {
        extract(objectMap, HallServer.class).stopServer();
        extract(objectMap, LotroServer.class).stopServer();
        extract(objectMap, ChatServer.class).stopServer();
    }
}
