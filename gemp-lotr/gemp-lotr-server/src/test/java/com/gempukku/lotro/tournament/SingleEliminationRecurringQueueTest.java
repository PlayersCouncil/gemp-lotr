package com.gempukku.lotro.tournament;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class SingleEliminationRecurringQueueTest extends AbstractAtTest {

    @Test
    public void joiningQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue("test", 10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(1, queue.getPlayerCount());
        assertTrue(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void leavingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue("test", 10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leavePlayer(collectionsManager, player);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }
    
    @Test
    public void cancellingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue("test", 10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        Player player = new Player(1, "p1", "pass", "u", null, null, null, null, false);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leaveAllPlayers(collectionsManager);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void fillingQueue() throws SQLException, IOException {
        var tournament = Mockito.mock(Tournament.class);
        var tournamentService = Mockito.mock(TournamentService.class);
        var tournamentInfo = Mockito.mock(TournamentInfo.class);

        //var tournamentInfo = new TournamentInfo("id-test", null, "name-", "format", ZonedDateTime.now(), CollectionType.MY_CARDS,
          //      Tournament.Stage.PLAYING_GAMES, 0, false, Tournament.getPairingMechanism("singleElimination"), Tournament.getTournamentPrizes(_productLibrary, "none"));

//        var info = new TournamentInfo(tournamentId, null, tournamentName, _format, ZonedDateTime.now(),
//                _collectionType, Tournament.Stage.PLAYING_GAMES, 0, false,
//                Tournament.getPairingMechanism("singleElimination"), _tournamentPrizes);

//        var tournamentInfo = new TournamentInfo(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.any(),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq(0), Mockito.eq(false),
//                Mockito.eq("singleElimination"), Mockito.any()))

        Mockito.when(tournamentService.addTournament(tournamentInfo)).thenReturn(tournament);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue("test", 10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));




        Player player1 = new Player(1, "p1", "pass", "u", null, null, null, null, false);
        Player player2 = new Player(2, "p2", "pass", "u", null, null, null, null, false);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        assertFalse(queue.process(queueCallback, collectionsManager));

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);

        assertEquals(2, queue.getPlayerCount());

        assertFalse(queue.process(queueCallback, collectionsManager));

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));

//        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any());

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
        Mockito.when(tournamentService.addTournament(tournamentInfo)).thenReturn(tournament);
//        Mockito.when(tournamentService.addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any()))
//                .thenReturn(tournament);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue("test", 10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        Player player1 = new Player(1, "p1", "pass", "u", null, null, null, null, false);
        Player player2 = new Player(2, "p2", "pass", "u", null, null, null, null, false);
        Player player3 = new Player(3, "p3", "pass", "u", null, null, null, null, false);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        assertFalse(queue.process(queueCallback, collectionsManager));

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);
        queue.joinPlayer(collectionsManager, player3, null);

        assertEquals(3, queue.getPlayerCount());

        assertFalse(queue.process(queueCallback, collectionsManager));

        assertEquals(1, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));
        assertTrue(queue.isPlayerSignedUp("p3"));

//        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
//                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any());

        //Mockito.verify(tournamentService).addTournament(tournamentInfo);
        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).recordTournamentPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        //Mockito.verify(queueCallback).createTournament(tournament);
        //Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }
}
