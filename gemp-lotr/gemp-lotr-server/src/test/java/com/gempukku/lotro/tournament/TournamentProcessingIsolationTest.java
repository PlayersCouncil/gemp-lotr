package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * One tournament queue or tournament that fails must not stop the rest of the server's tournaments.  A scheduled
 * tournament whose start threw (a deck name too long for its database column) used to be tried again on every pass,
 * and since the exception left the processing loop, no queue after it started, no running tournament advanced, and
 * the ready check of a player-made queue answered 500.  Also covers what a queue hands to the tournament it starts.
 */
public class TournamentProcessingIsolationTest {
    private TournamentDAO _tournamentDao;
    private TournamentPlayerDAO _playerDao;
    private TournamentService _service;

    @Before
    public void setUp() {
        _tournamentDao = Mockito.mock(TournamentDAO.class);
        _playerDao = Mockito.mock(TournamentPlayerDAO.class);
        _service = new TournamentService(Mockito.mock(CollectionsManager.class), Mockito.mock(ProductLibrary.class),
                null, _tournamentDao, _playerDao, Mockito.mock(TournamentMatchDAO.class),
                Mockito.mock(GameHistoryDAO.class), Mockito.mock(LotroCardBlueprintLibrary.class),
                Mockito.mock(LotroFormatLibrary.class), Mockito.mock(SoloDraftDefinitions.class),
                Mockito.mock(TableDraftDefinitions.class), null);
    }

    private static TournamentParams params(String id) {
        var params = new TournamentParams();
        params.tournamentId = id;
        params.name = "Cup " + id;
        params.format = "pc_fotr_block";
        params.type = Tournament.TournamentType.CONSTRUCTED;
        params.playoff = Tournament.PairingType.SWISS;
        params.prizes = Tournament.PrizeType.NONE;
        params.minimumPlayers = 2;
        return params;
    }

    private static Player player(String name) {
        var player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    /**
     * A scheduled tournament whose start time has just passed, with two players signed up: it starts on the next
     * pass.  Put straight among the service's queues, as one loaded from the database would be.
     */
    private ScheduledTournamentQueue dueQueueWithTwoPlayers(TournamentParams params) throws Exception {
        LocalDateTime justNow = DateUtils.Now().minusMinutes(1).toLocalDateTime().withNano(0);
        params.startTime = justNow;
        var info = new TournamentInfo(null, Tournament.getPairingMechanism(params.playoff),
                Mockito.mock(LotroFormat.class), params, params.tournamentId,
                DateUtils.ParseDate(justNow), params.getInitialStage(), 0);
        var queue = new ScheduledTournamentQueue(_service, params.tournamentId, params.name, info,
                _started::add, Mockito.mock(CollectionsManager.class));
        queue.joinPlayer(player(params.tournamentId + "-alice"), new LotroDeck("alice's deck"));
        queue.joinPlayer(player(params.tournamentId + "-bob"), new LotroDeck("bob's deck"));
        queues().put(params.tournamentId, queue);
        return queue;
    }

    private final List<Tournament> _started = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private Map<String, TournamentQueue> queues() throws Exception {
        var field = TournamentService.class.getDeclaredField("_tournamentQueues");
        field.setAccessible(true);
        return (Map<String, TournamentQueue>) field.get(_service);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Tournament> activeTournaments() throws Exception {
        var field = TournamentService.class.getDeclaredField("_activeTournaments");
        field.setAccessible(true);
        return (Map<String, Tournament>) field.get(_service);
    }

    @Test
    public void aQueueWhoseStartFailsDoesNotStopTheOthers() throws Exception {
        var broken = dueQueueWithTwoPlayers(params("broken"));
        dueQueueWithTwoPlayers(params("fine"));
        Mockito.doThrow(new RuntimeException("Data too long for column 'deck_name'"))
                .when(_playerDao).addPlayer(Mockito.eq("broken"), Mockito.anyString(), Mockito.any());

        // twice: whichever order the queues are visited in, the failing one is visited on both passes
        _service.processTournamentQueues();
        _service.processTournamentQueues();

        Mockito.verify(_tournamentDao).updateScheduledTournamentStarted("fine");
        Mockito.verify(_tournamentDao, Mockito.never()).updateScheduledTournamentStarted("broken");
        assertNull("the started queue is gone", _service.getTournamentQueue("fine"));
        assertSame("the failed queue stays, to be tried again", broken, _service.getTournamentQueue("broken"));
        assertEquals("and keeps its players", 2, broken.getPlayerCount());
    }

    @Test
    public void aFailedStartTakesBackThePlayersItRecorded() throws Exception {
        dueQueueWithTwoPlayers(params("broken"));
        Mockito.doThrow(new RuntimeException("Data too long for column 'deck_name'"))
                .when(_playerDao).addPlayer(Mockito.eq("broken"), Mockito.eq("broken-bob"), Mockito.any());

        _service.processTournamentQueues();
        _service.processTournamentQueues();

        // once per failed attempt, so the attempts do not pile up copies of the players recorded before the failure
        Mockito.verify(_playerDao, Mockito.times(2)).removePlayers("broken");
    }

    @Test
    public void aFailedStartLeavesAnExistingTournamentsPlayersAlone() throws Exception {
        dueQueueWithTwoPlayers(params("taken"));
        Mockito.when(_tournamentDao.getTournamentById("taken")).thenReturn(new DBDefs.Tournament());
        Mockito.doThrow(new RuntimeException("Duplicate entry 'taken' for key 'UQ_tournament_id'"))
                .when(_tournamentDao).addTournament(Mockito.any());

        _service.processTournamentQueues();

        Mockito.verify(_playerDao, Mockito.never()).removePlayers(Mockito.anyString());
    }

    @Test
    public void theStartedTournamentKeepsTheQueuesSettings() throws Exception {
        var params = params("kickoff");
        params.manualKickoff = true;
        params.tiebreaker = "owr";
        params.customCollection = true;
        dueQueueWithTwoPlayers(params);

        _service.processTournamentQueues();

        var stored = ArgumentCaptor.forClass(DBDefs.Tournament.class);
        Mockito.verify(_tournamentDao).addTournament(stored.capture());
        assertEquals("a manual kickoff tournament waits for its kickoff",
                Tournament.Stage.AWAITING_KICKOFF.toString(), stored.getValue().stage);
        var storedParams = Tournament.parseInfo(stored.getValue().type, stored.getValue().parameters);
        assertTrue(storedParams.manualKickoff);
        assertEquals("owr", storedParams.tiebreaker);
        assertTrue(storedParams.customCollection);
    }

    @Test
    public void aTournamentWithoutManualKickoffStartsPlaying() throws Exception {
        dueQueueWithTwoPlayers(params("plain"));

        _service.processTournamentQueues();

        var stored = ArgumentCaptor.forClass(DBDefs.Tournament.class);
        Mockito.verify(_tournamentDao).addTournament(stored.capture());
        assertEquals(Tournament.Stage.PLAYING_GAMES.toString(), stored.getValue().stage);
        assertFalse(Tournament.parseInfo(stored.getValue().type, stored.getValue().parameters).manualKickoff);
    }

    @Test
    public void aTournamentThatFailsToAdvanceDoesNotStopTheOthers() throws Exception {
        var broken = Mockito.mock(Tournament.class);
        Mockito.when(broken.advanceTournament(Mockito.any())).thenThrow(new RuntimeException("boom"));
        Mockito.when(broken.getTournamentStage()).thenReturn(Tournament.Stage.PLAYING_GAMES);

        var done = new ArrayList<String>();
        TournamentProcessAction failingAction = callback -> { throw new RuntimeException("no table"); };
        TournamentProcessAction workingAction = callback -> done.add("ran");
        var fine = Mockito.mock(Tournament.class);
        Mockito.when(fine.advanceTournament(Mockito.any())).thenReturn(List.of(failingAction, workingAction));
        Mockito.when(fine.getTournamentStage()).thenReturn(Tournament.Stage.FINISHED);

        activeTournaments().put("broken", broken);
        activeTournaments().put("fine", fine);

        assertTrue(_service.processTournaments(Mockito.mock(HallServer.class)));

        assertEquals("an action failing does not cost the actions after it", List.of("ran"), done);
        assertEquals("the finished tournament left the live list, the failing one stays",
                List.of(broken), _service.getLiveTournaments());
    }
}
