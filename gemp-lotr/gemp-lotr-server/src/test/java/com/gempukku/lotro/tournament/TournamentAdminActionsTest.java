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
import com.gempukku.lotro.hall.HallException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * What an admin does to a tournament while it runs: setting its stage ({@link TournamentService#setTournamentStageByAdmin})
 * and removing a player from it or from its queue ({@link TournamentService#removePlayerByAdmin}).  Setting a stage
 * must touch that one tournament only; it used to reload every queue and tournament on the server.
 */
public class TournamentAdminActionsTest {
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
        params.startTime = DateUtils.Now().minusMinutes(1).toLocalDateTime().withNano(0);
        params.playoff = Tournament.PairingType.SWISS;
        params.prizes = Tournament.PrizeType.NONE;
        params.minimumPlayers = 2;
        params.manualKickoff = true;
        return params;
    }

    private static Player player(String name) {
        var player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    /** The database row of a started tournament. */
    private DBDefs.Tournament record(String id, Tournament.Stage stage) {
        var params = params(id);
        var row = new DBDefs.Tournament();
        row.tournament_id = id;
        row.name = params.name;
        row.start_date = params.startTime;
        row.type = "CONSTRUCTED";
        row.parameters = JsonUtils.Serialize(params);
        row.stage = stage.name();
        row.round = 0;
        Mockito.when(_tournamentDao.getTournamentById(id)).thenReturn(row);
        Mockito.when(_tournamentDao.getTournament(id)).thenReturn(row);
        return row;
    }

    /** A queue with alice and bob signed up, among the service's queues. */
    private ScheduledTournamentQueue queueWithTwoPlayers(String id) throws Exception {
        var params = params(id);
        var info = new TournamentInfo(null, Tournament.getPairingMechanism(params.playoff),
                Mockito.mock(LotroFormat.class), params, params.tournamentId,
                DateUtils.ParseDate(params.startTime), params.getInitialStage(), 0);
        var queue = new ScheduledTournamentQueue(_service, id, params.name, info, tournament -> { },
                Mockito.mock(CollectionsManager.class));
        queue.joinPlayer(player("alice"), new LotroDeck("alice's deck"));
        queue.joinPlayer(player("bob"), new LotroDeck("bob's deck"));
        queues().put(id, queue);
        return queue;
    }

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

    private static String refusal(Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            return expected.getMessage();
        }
        fail("expected the action to be refused");
        return null;
    }

    // ---- stage ----

    @Test
    public void settingAStageRebuildsThatTournamentAndLeavesEverythingElseAlone() throws Exception {
        record("wc", Tournament.Stage.AWAITING_KICKOFF);
        var before = Mockito.mock(Tournament.class);
        var other = Mockito.mock(Tournament.class);
        activeTournaments().put("wc", before);
        activeTournaments().put("other", other);
        var queue = queueWithTwoPlayers("waiting");

        assertTrue(_service.setTournamentStageByAdmin("wc", Tournament.Stage.PREPARING));

        Mockito.verify(_tournamentDao).updateTournamentStage("wc", Tournament.Stage.PREPARING);
        var after = activeTournaments().get("wc");
        assertNotNull(after);
        assertNotSame("rebuilt from the database, as a restart would resume it", before, after);
        assertSame("another running tournament is untouched", other, activeTournaments().get("other"));
        assertSame("a queue is untouched", queue, _service.getTournamentQueue("waiting"));
        assertEquals("and keeps its players", 2, queue.getPlayerCount());
        Mockito.verify(_tournamentDao, Mockito.never()).getUnfinishedTournaments();
    }

    @Test
    public void settingFinishedTakesTheTournamentOutOfTheRunningOnes() throws Exception {
        record("wc", Tournament.Stage.PLAYING_GAMES);
        activeTournaments().put("wc", Mockito.mock(Tournament.class));

        assertTrue(_service.setTournamentStageByAdmin("wc", Tournament.Stage.FINISHED));

        Mockito.verify(_tournamentDao).updateTournamentStage("wc", Tournament.Stage.FINISHED);
        assertTrue(_service.getLiveTournaments().isEmpty());
    }

    @Test
    public void aFinishedTournamentSetBackToARunningStageRunsAgain() throws Exception {
        record("wc", Tournament.Stage.FINISHED);

        assertTrue(_service.setTournamentStageByAdmin("wc", Tournament.Stage.PAUSED));

        assertNotNull(activeTournaments().get("wc"));
    }

    @Test
    public void thereIsNoStageToSetOnAnUnknownTournament() {
        assertFalse(_service.setTournamentStageByAdmin("nope", Tournament.Stage.PREPARING));
        Mockito.verify(_tournamentDao, Mockito.never()).updateTournamentStage(Mockito.anyString(), Mockito.any());
    }

    // ---- removing a player ----

    private Tournament runningWith(String id, String... players) throws Exception {
        var tournament = Mockito.mock(Tournament.class);
        Mockito.when(tournament.getTournamentName()).thenReturn("Cup " + id);
        Mockito.when(tournament.dropPlayer(Mockito.anyString())).thenReturn(BaseTournament.DROPPED_MESSAGE);
        Mockito.when(_playerDao.getPlayers(id)).thenReturn(new LinkedHashSet<>(List.of(players)));
        activeTournaments().put(id, tournament);
        return tournament;
    }

    @Test
    public void aPlayerOfARunningTournamentIsDropped() throws Exception {
        var tournament = runningWith("wc", "Alice", "bob");

        // found whatever the case the admin typed the name in
        String result = _service.removePlayerByAdmin("wc", "alice");

        Mockito.verify(tournament).dropPlayer("Alice");
        assertEquals("'Alice' was dropped from 'Cup wc' and cannot join it again.", result);
    }

    @Test
    public void removingSomeoneWhoIsNotInTheTournamentIsRefused() throws Exception {
        var tournament = runningWith("wc", "Alice", "bob");

        assertEquals("'carol' is not in tournament 'Cup wc'.", refusal(() -> _service.removePlayerByAdmin("wc", "carol")));
        Mockito.verify(tournament, Mockito.never()).dropPlayer(Mockito.anyString());
    }

    @Test
    public void removingSomeoneWhoAlreadyDroppedIsRefused() throws Exception {
        var tournament = runningWith("wc", "Alice", "bob");
        Mockito.when(tournament.isPlayerAbandoned("bob")).thenReturn(true);

        assertEquals("'bob' has already dropped from 'Cup wc'.", refusal(() -> _service.removePlayerByAdmin("wc", "bob")));
        Mockito.verify(tournament, Mockito.never()).dropPlayer(Mockito.anyString());
    }

    @Test
    public void aPlayerInAGameIsNotRemovedAndTheAdminIsToldWhy() throws Exception {
        var tournament = runningWith("wc", "Alice", "bob");
        Mockito.when(tournament.dropPlayer("bob")).thenReturn("You are currently playing a match.");

        String message = refusal(() -> _service.removePlayerByAdmin("wc", "bob"));

        assertEquals("'bob' was not removed.  The tournament answered: You are currently playing a match.", message);
    }

    @Test
    public void aPlayerRemovedFromAQueueCannotSignUpForItAgain() throws Exception {
        var queue = queueWithTwoPlayers("waiting");

        String result = _service.removePlayerByAdmin("waiting", "bob");

        assertEquals("'bob' was removed from the queue of 'Cup waiting' and cannot join it again.", result);
        assertEquals(1, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("bob"));
        try {
            queue.joinPlayer(player("bob"), new LotroDeck("bob's deck"));
            fail("expected the join to be refused");
        } catch (HallException expected) {
            assertEquals("An administrator removed you from Cup waiting, so you cannot join it again.", expected.getMessage());
        }
        assertEquals(1, queue.getPlayerCount());
    }

    @Test
    public void aPlayerRemovedFromAQueueCannotJoinItsTournamentLate() throws Exception {
        var queue = queueWithTwoPlayers("waiting");
        queue.joinPlayer(player("carol"), new LotroDeck("carol's deck"));
        _service.removePlayerByAdmin("waiting", "bob");
        var stored = org.mockito.ArgumentCaptor.forClass(DBDefs.Tournament.class);

        _service.processTournamentQueues(); // its start time has passed: the tournament starts with alice and carol

        Mockito.verify(_tournamentDao).addTournament(stored.capture());
        Mockito.verify(_playerDao, Mockito.never()).addPlayer(Mockito.eq("waiting"), Mockito.eq("bob"), Mockito.any());
        assertEquals(List.of("bob"), Tournament.parseInfo(stored.getValue().type, stored.getValue().parameters).removedPlayers);

        // the started tournament, as it is built from its stored row, turns the removed player away
        var row = stored.getValue();
        Mockito.when(_tournamentDao.getTournament("waiting")).thenReturn(row);
        var tournament = new ConstructedTournament(_service, Mockito.mock(CollectionsManager.class),
                Mockito.mock(ProductLibrary.class), Mockito.mock(LotroFormatLibrary.class), null, null, null, "waiting");
        assertTrue("open to late joiners while it waits for its kickoff", tournament.isJoinable());
        assertFalse(tournament.join("bob", new LotroDeck("bob's deck")));
        assertTrue(tournament.join("dave", new LotroDeck("dave's deck")));
    }

    @Test
    public void removingSomeoneWhoIsNotInTheQueueIsRefused() throws Exception {
        var queue = queueWithTwoPlayers("waiting");

        assertEquals("'carol' is not signed up for 'Cup waiting'.", refusal(() -> _service.removePlayerByAdmin("waiting", "carol")));
        assertEquals(2, queue.getPlayerCount());
    }

    @Test
    public void thereIsNobodyToRemoveFromAnUnknownTournament() {
        assertEquals("There is no running tournament or open queue 'nope'.",
                refusal(() -> _service.removePlayerByAdmin("nope", "bob")));
    }
}
