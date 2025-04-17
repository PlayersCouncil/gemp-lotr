package com.gempukku.lotro.tournament;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class SingleEliminationRecurringQueueTest extends AbstractAtTest {

    private static LotroFormat _testFormat;
    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
        _testFormat = _formatLibrary.getFormat("fotr_block");
    }

    @Test
    public void joiningQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        var queue = new ImmediateRecurringQueue(tournamentService, "id-", "test",
                new TournamentInfo(tournamentService, _productLibrary, _formatLibrary, DateUtils.Now(),
                        new TournamentParams("id-", "name-", _testFormat.getCode(), 10, 2, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.NONE)),
                        null, collectionsManager);

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verify(tournamentService).getPairingMechanism(Mockito.any());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(1, queue.getPlayerCount());
        assertTrue(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void leavingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        var queue = new ImmediateRecurringQueue(tournamentService, "id-", "test",
                new TournamentInfo(tournamentService, _productLibrary, _formatLibrary, DateUtils.Now(),
                        new TournamentParams("id-", "name-", _testFormat.getCode(), 10, 2, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.NONE)),
                        null, collectionsManager);

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leavePlayer(player);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player.getName()), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verify(tournamentService).getPairingMechanism(Mockito.any());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }
    
    @Test
    public void cancellingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        var queue = new ImmediateRecurringQueue(tournamentService, "id-", "test",
                new TournamentInfo(tournamentService, _productLibrary, _formatLibrary, DateUtils.Now(),
                        new TournamentParams("id-", "name-", _testFormat.getCode(), 10, 2, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.NONE)),
                        null, collectionsManager);

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leaveAllPlayers();
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verify(tournamentService).getPairingMechanism(Mockito.any());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void fillingQueue() throws SQLException, IOException {
        var tournament = Mockito.mock(Tournament.class);
        var tournamentService = Mockito.mock(TournamentService.class);
        var tournamentInfo = Mockito.mock(TournamentInfo.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);

        //var tournamentInfo = new TournamentInfo("id-test", null, "name-", "format", ZonedDateTime.now(), CollectionType.MY_CARDS,
          //      Tournament.Stage.PLAYING_GAMES, 0, false, Tournament.getPairingMechanism("single-elimination"), Tournament.getTournamentPrizes(_productLibrary, "none"));

//        var info = new TournamentInfo(tournamentId, null, tournamentName, _format, ZonedDateTime.now(),
//                _collectionType, Tournament.Stage.PLAYING_GAMES, 0, false,
//                Tournament.getPairingMechanism("single-elimination"), _tournamentPrizes);

//        var tournamentInfo = new TournamentInfo(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.any(),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq(0), Mockito.eq(false),
//                Mockito.eq("single-elimination"), Mockito.any()))

        Mockito.when(tournamentService.addTournament(tournamentInfo)).thenReturn(tournament);

        var queue = new ImmediateRecurringQueue(tournamentService, "id-", "test",
                new TournamentInfo(tournamentService, _productLibrary, _formatLibrary, DateUtils.Now(),
                        new TournamentParams("id-", "name-", _testFormat.getCode(), 10, 2, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.NONE)),
                        queueCallback, collectionsManager);

        Player player1 = new Player(1, "p1", "pass", "u", null, null, null, null, false);
        Player player2 = new Player(2, "p2", "pass", "u", null, null, null, null, false);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(player1, null);

        assertFalse(queue.process());

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(player2, null);

        assertEquals(2, queue.getPlayerCount());

        assertFalse(queue.process());

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));

//        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("single-elimination"), Mockito.nullable(String.class), Mockito.any());

        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));
        //Mockito.verify(tournamentService).addTournament(tournamentInfo);
        //Mockito.when(tournamentService.addTournament(tournamentInfo)).thenReturn(Mockito.any());

        //Mockito.verify(queueCallback).createTournament(tournament);
        //Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }

    @Test
    public void overflowingQueue() throws SQLException, IOException {
        var tournament = Mockito.mock(Tournament.class);
        var tournamentInfo = Mockito.mock(TournamentInfo.class);
        var tournamentService = Mockito.mock(TournamentService.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        Mockito.when(tournamentService.addTournament(tournamentInfo)).thenReturn(tournament);
//        Mockito.when(tournamentService.addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("single-elimination"), Mockito.nullable(String.class), Mockito.any()))
//                .thenReturn(tournament);

        var params = new TournamentParams("id-", "name-", _testFormat.getCode(), 10, 2, Tournament.PairingType.SINGLE_ELIMINATION, Tournament.PrizeType.NONE);
        params.maximumPlayers = 2;
        var queue = new ImmediateRecurringQueue(tournamentService, "id-", "test",
                new TournamentInfo(tournamentService, _productLibrary, _formatLibrary, DateUtils.Now(),
                        params),
                        queueCallback, collectionsManager);

        Player player1 = new Player(1, "p1", "pass", "u", null, null, null, null, false);
        Player player2 = new Player(2, "p2", "pass", "u", null, null, null, null, false);
        Player player3 = new Player(3, "p3", "pass", "u", null, null, null, null, false);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(player1, null);

        assertFalse(queue.process());

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(player2, null);
        queue.joinPlayer(player3, null);

        assertEquals(2, queue.getPlayerCount());

        assertFalse(queue.process());

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));
        assertFalse(queue.isPlayerSignedUp("p3"));


        queue.joinPlayer(player3, null);
        assertEquals(1, queue.getPlayerCount());
        assertTrue(queue.isPlayerSignedUp("p3"));

//        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("single-elimination"), Mockito.nullable(String.class), Mockito.any());

        //Mockito.verify(tournamentService).addTournament(tournamentInfo);
        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        //Mockito.verify(queueCallback).createTournament(tournament);
        //Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }
}
