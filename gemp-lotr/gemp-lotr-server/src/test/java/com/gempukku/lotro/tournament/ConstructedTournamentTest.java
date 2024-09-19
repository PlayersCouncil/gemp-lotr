package com.gempukku.lotro.tournament;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.*;

import static com.gempukku.lotro.tournament.Tournament.getPairingMechanism;
import static org.junit.Assert.assertEquals;

public class ConstructedTournamentTest extends AbstractAtTest {
    private final int _waitForPairingsTime = 100;

    static {
        _cardLibrary = new LotroCardBlueprintLibrary();
        _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        _productLibrary = new ProductLibrary(_cardLibrary);
    }

    @Test
    public void testTournament() throws InterruptedException {
        var tournamentService = Mockito.mock(TournamentService.class);
        String tournamentId = "t1";
        var tourneyData = new DBDefs.Tournament();
        tourneyData.tournament_id = tournamentId;
        tourneyData.name = "Name";
        tourneyData.stage = "Playing Games";
        tourneyData.type = Tournament.TournamentType.CONSTRUCTED.toString();
        tourneyData.start_date = DateUtils.Now().plus(Duration.ofMinutes(10)).toLocalDateTime();
        tourneyData.parameters = """
                {
                    format: fotr_block
                    playoff: TEST
                    type: CONSTRUCTED
                    prizes: DAILY
                }
                """;
        var tables = Mockito.mock(com.gempukku.lotro.hall.TableHolder.class);
        Map<String, LotroDeck> playerDecks = new HashMap<>();
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8"));
        playerDecks.put("p1", new LotroDeck("p1"));
        playerDecks.put("p2", new LotroDeck("p2"));
        playerDecks.put("p3", new LotroDeck("p3"));
        playerDecks.put("p4", new LotroDeck("p4"));
        playerDecks.put("p5", new LotroDeck("p5"));
        playerDecks.put("p6", new LotroDeck("p6"));
        playerDecks.put("p7", new LotroDeck("p7"));
        playerDecks.put("p8", new LotroDeck("p8"));

        Set<String> droppedAfterRoundOne = new HashSet<>(Arrays.asList("p2", "p4", "p6", "p8"));
        Set<String> droppedAfterRoundTwo = new HashSet<>(Arrays.asList("p2", "p3", "p4", "p6", "p7", "p8"));
        Set<String> droppedAfterRoundThree = new HashSet<>(Arrays.asList("p2", "p3", "p4", "p5", "p6", "p7", "p8"));

        PairingMechanism pairingMechanism = Mockito.mock(PairingMechanism.class);
        Mockito.when(pairingMechanism.shouldDropLoser()).thenReturn(true);

        //Tournament tourney = Mockito.mock(Tournament.class);
        //Mockito.when(tourney.getPairingMechanism("testPairing")).thenReturn(pairingMechanism);

        Mockito.when(tournamentService.retrieveTournamentData(tournamentId)).thenReturn(tourneyData);
        Mockito.when(tournamentService.retrieveTournamentPlayers(tournamentId)).thenReturn(allPlayers);
        Mockito.when(tournamentService.retrievePlayerDecks(tournamentId, "fotr_block")).thenReturn(playerDecks);
        Mockito.when(tournamentService.getPairingMechanism(Tournament.PairingType.TEST)).thenReturn(pairingMechanism);

        Mockito.when(tables.getTournamentTables(tournamentId)).thenReturn(new ArrayList<>());

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        var tournament = new ConstructedTournament(tournamentService, null, _productLibrary, _formatLibrary, tables, tournamentId);

        tournament.setWaitForPairingsTime(_waitForPairingsTime);

        Mockito.when(pairingMechanism.isFinished(Mockito.eq(3), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundThree)))
                .thenReturn(true);

        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(1), Mockito.eq(allPlayers), Mockito.eq(Collections.emptySet()),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocationOnMock) {
                        Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                        pairings.put("p1", "p2");
                        pairings.put("p3", "p4");
                        pairings.put("p5", "p6");
                        pairings.put("p7", "p8");

                        return false;
                    }
                });
        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(2), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundOne),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocationOnMock) {
                        Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                        pairings.put("p1", "p3");
                        pairings.put("p5", "p7");

                        return false;
                    }
                });
        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(3), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundTwo),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocationOnMock) {
                        Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                        pairings.put("p1", "p5");

                        return false;
                    }
                });

        TournamentCallback tournamentCallback = Mockito.mock(TournamentCallback.class);
        Mockito.doAnswer(
                new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocationOnMock) {
                        System.out.println("Broadcasted: "+invocationOnMock.getArguments()[0]);
                        return null;
                    }
                }
        ).when(tournamentCallback).broadcastMessage(Mockito.anyString());

        advanceTournament(tournament, collectionsManager, tournamentCallback);

        Mockito.verify(tournamentCallback).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime + 10);
        advanceTournament(tournament, collectionsManager, tournamentCallback);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p2", playerDecks.get("p2"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p3", playerDecks.get("p3"), "p4", playerDecks.get("p4"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p5", playerDecks.get("p5"), "p6", playerDecks.get("p6"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p7", playerDecks.get("p7"), "p8", playerDecks.get("p8"));
        
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        assertEquals(1, tournament.getCurrentRound());

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p2");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p3", "p4");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p5", "p6");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p7", "p8");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verify(tournamentCallback, new Times(2)).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime);
        advanceTournament(tournament, collectionsManager, tournamentCallback);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p3", playerDecks.get("p3"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p5", playerDecks.get("p5"), "p7", playerDecks.get("p7"));

        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);
        
        assertEquals(2, tournament.getCurrentRound());

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p3");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p5", "p7");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verify(tournamentCallback, new Times(3)).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime);
        advanceTournament(tournament, collectionsManager, tournamentCallback);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p5", playerDecks.get("p5"));

        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        assertEquals(3, tournament.getCurrentRound());

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p5");

        advanceTournament(tournament, collectionsManager, tournamentCallback);
        Mockito.verify(tournamentCallback, new Times(4)).broadcastMessage(Mockito.anyString());

        Mockito.verify(collectionsManager).addItemsToPlayerCollection(Mockito.eq(true), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());
        Mockito.verify(collectionsManager).addItemsToPlayerCollection(Mockito.eq(true), Mockito.anyString(), Mockito.eq("p5"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());
        Mockito.verify(collectionsManager).addItemsToPlayerCollection(Mockito.eq(true), Mockito.anyString(), Mockito.eq("p3"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());
        Mockito.verify(collectionsManager).addItemsToPlayerCollection(Mockito.eq(true), Mockito.anyString(), Mockito.eq("p7"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());

//        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);
        
        assertEquals(3, tournament.getCurrentRound());
        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
    }

    private void advanceTournament(Tournament tournament, CollectionsManager collectionsManager, TournamentCallback callback) {
        List<TournamentProcessAction> actions = tournament.advanceTournament(collectionsManager);
        for (TournamentProcessAction action : actions) {
            action.process(callback);
        }
    }
}
