package com.gempukku.lotro.tournament;

import com.gempukku.lotro.hall.HallException;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * The lookups behind the unified Tournament Admin form ({@link TournamentService#getAdminTournamentView},
 * {@link TournamentService#getAdminTournamentList}, {@link TournamentService#getAdminTournamentPlayers}) and in-place
 * editing of a scheduled tournament ({@link TournamentService#updateScheduledTournament}).
 */
public class TournamentAdminServiceTest {
    private TournamentDAO _tournamentDao;
    private TournamentPlayerDAO _playerDao;
    private TournamentService _service;
    private ZonedDateTime _now;

    @Before
    public void setUp() {
        _tournamentDao = Mockito.mock(TournamentDAO.class);
        _playerDao = Mockito.mock(TournamentPlayerDAO.class);
        _service = new TournamentService(Mockito.mock(CollectionsManager.class), Mockito.mock(ProductLibrary.class),
                null, _tournamentDao, _playerDao, Mockito.mock(TournamentMatchDAO.class),
                Mockito.mock(GameHistoryDAO.class), Mockito.mock(LotroCardBlueprintLibrary.class),
                Mockito.mock(LotroFormatLibrary.class), Mockito.mock(SoloDraftDefinitions.class),
                Mockito.mock(TableDraftDefinitions.class), null);
        _now = DateUtils.Now();
    }

    // ---- fixtures ----

    private static TournamentParams params(String id, String name, LocalDateTime start) {
        var params = new TournamentParams();
        params.tournamentId = id;
        params.name = name;
        params.format = "pc_fotr_block";
        params.type = Tournament.TournamentType.CONSTRUCTED;
        params.startTime = start;
        params.playoff = Tournament.PairingType.SWISS;
        params.prizes = Tournament.PrizeType.DAILY;
        params.minimumPlayers = 2;
        return params;
    }

    private static TournamentInfo info(TournamentParams params) {
        return new TournamentInfo(null, null, null, params, params.tournamentId,
                DateUtils.ParseDate(params.startTime), params.getInitialStage(), 0);
    }

    private DBDefs.ScheduledTournament scheduledRow(String id, String name, LocalDateTime start, boolean started) {
        var row = info(params(id, name, start)).ToScheduledDB();
        row.started = started;
        Mockito.when(_tournamentDao.getScheduledTournament(id)).thenReturn(row);
        return row;
    }

    private DBDefs.Tournament record(String id, String name, String stage, LocalDateTime start) {
        var row = new DBDefs.Tournament();
        row.tournament_id = id;
        row.name = name;
        row.start_date = start;
        row.type = "CONSTRUCTED";
        row.parameters = JsonUtils.Serialize(params(id, name, start));
        row.stage = stage;
        row.round = 3;
        Mockito.when(_tournamentDao.getTournamentById(id)).thenReturn(row);
        Mockito.when(_tournamentDao.getTournament(id)).thenReturn(row);
        return row;
    }

    /** Schedules a tournament through the service, so its queue is loaded as it is on the live server. */
    private ScheduledTournamentQueue scheduleWithQueue(String id, LocalDateTime start) {
        assertTrue(_service.addScheduledTournament(info(params(id, "Cup", start))));
        scheduledRow(id, "Cup", start, false);
        return (ScheduledTournamentQueue) _service.getTournamentQueue(id);
    }

    private static Player player(String name) {
        var player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    private LocalDateTime inMinutes(long minutes) {
        return _now.plusMinutes(minutes).toLocalDateTime().withSecond(0).withNano(0);
    }

    // ---- view ----

    @Test
    public void anUnknownIdHasNoView() {
        assertNull(_service.getAdminTournamentView("nope", _now));
        assertNull(_service.getAdminTournamentView("", _now));
    }

    @Test
    public void aFutureScheduledTournamentIsScheduledAndEditable() {
        scheduledRow("wc-a", "WC deck registration", inMinutes(3 * 24 * 60), false);

        var view = _service.getAdminTournamentView("wc-a", _now);

        assertEquals(TournamentService.ADMIN_STATUS_SCHEDULED, view.status());
        assertNull(view.editBlocker());
        assertNull(view.record());
        assertEquals(0, view.signedUp());
    }

    @Test
    public void aScheduledTournamentWhoseStartPassedIsExpiredButCanBeRescheduled() {
        scheduledRow("old", "Too few players", inMinutes(-120), false);

        var view = _service.getAdminTournamentView("old", _now);

        assertEquals(TournamentService.ADMIN_STATUS_EXPIRED, view.status());
        assertNull(view.editBlocker());
    }

    @Test
    public void aStartedTournamentIsLiveAndCannotBeEdited() {
        scheduledRow("t1", "Cup", inMinutes(-60), true);
        record("t1", "Cup", "PAUSED", inMinutes(-60));

        var view = _service.getAdminTournamentView("t1", _now);

        assertEquals(TournamentService.ADMIN_STATUS_LIVE, view.status());
        assertNotNull(view.editBlocker());
        assertTrue(view.editBlocker(), view.editBlocker().contains("already started"));
    }

    @Test
    public void aFinishedTournamentIsFinishedEvenWhenLoadedIntoTheLiveMap() {
        record("t2", "Old cup", "FINISHED", inMinutes(-5000));
        assertEquals(TournamentService.ADMIN_STATUS_FINISHED, _service.getAdminTournamentView("t2", _now).status());

        record("t3", "Paused cup", "PAUSED", inMinutes(-5000));
        _service.getTournamentById("t3");   // now in the live map
        assertEquals(TournamentService.ADMIN_STATUS_LIVE, _service.getAdminTournamentView("t3", _now).status());
    }

    @Test
    public void aQueueThatIsNotAScheduledTournamentCannotBeEdited() {
        var queue = scheduleWithQueue("pm", inMinutes(30));
        Mockito.when(_tournamentDao.getScheduledTournament("pm")).thenReturn(null);   // e.g. a player-made queue

        var view = _service.getAdminTournamentView("pm", _now);

        assertSame(queue, view.queue());
        assertEquals(TournamentService.ADMIN_STATUS_SCHEDULED, view.status());
        assertTrue(view.editBlocker(), view.editBlocker().contains("not a scheduled tournament"));
    }

    @Test
    public void signedUpPlayersBlockEditing() throws Exception {
        var queue = scheduleWithQueue("cup", inMinutes(30));
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));
        queue.joinPlayer(player("bob"), new LotroDeck("bob"));

        var view = _service.getAdminTournamentView("cup", _now);

        assertEquals(2, view.signedUp());
        assertTrue(view.editBlocker(), view.editBlocker().startsWith("2 players have already signed up"));
    }

    // ---- update ----

    @Test
    public void anEditRewritesTheRowAndRebuildsTheLoadedQueue() {
        var oldQueue = scheduleWithQueue("cup", inMinutes(30));
        Mockito.when(_tournamentDao.updateScheduledTournament(Mockito.any())).thenReturn(1);

        var edited = params("cup", "Cup (renamed)", inMinutes(90));
        edited.cost = 100;
        _service.updateScheduledTournament(info(edited), _now);

        var captor = ArgumentCaptor.forClass(DBDefs.ScheduledTournament.class);
        Mockito.verify(_tournamentDao).updateScheduledTournament(captor.capture());
        assertEquals("cup", captor.getValue().tournament_id);
        assertEquals("Cup (renamed)", captor.getValue().name);
        assertEquals(inMinutes(90), captor.getValue().start_date);
        assertTrue(captor.getValue().parameters, captor.getValue().parameters.contains("\"cost\":100"));

        var newQueue = _service.getTournamentQueue("cup");
        assertNotNull(newQueue);
        assertNotSame(oldQueue, newQueue);
        assertEquals("Cup (renamed)", newQueue.getTournamentQueueName());
        assertEquals(100, newQueue.getCost());
        assertTrue(oldQueue.isRetired());
        assertFalse("the replaced queue must not take anyone's entry fee", oldQueue.isJoinable());
    }

    @Test
    public void anEditIsRefusedOncePlayersHaveSignedUp() throws Exception {
        var queue = scheduleWithQueue("cup", inMinutes(30));
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));

        try {
            _service.updateScheduledTournament(info(params("cup", "Changed", inMinutes(90))), _now);
            fail("expected the edit to be refused");
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("1 player has already signed up"));
        }
        Mockito.verify(_tournamentDao, Mockito.never()).updateScheduledTournament(Mockito.any());
        assertSame(queue, _service.getTournamentQueue("cup"));
        assertFalse(queue.isRetired());
        assertTrue(queue.isPlayerSignedUp("alice"));
    }

    @Test
    public void anEditOfAStartedTournamentIsRefused() {
        scheduledRow("t1", "Cup", inMinutes(-60), true);
        record("t1", "Cup", "PLAYING_GAMES", inMinutes(-60));

        try {
            _service.updateScheduledTournament(info(params("t1", "Changed", inMinutes(90))), _now);
            fail("expected the edit to be refused");
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("already started"));
        }
        Mockito.verify(_tournamentDao, Mockito.never()).updateScheduledTournament(Mockito.any());
    }

    @Test
    public void anEditMovingTheStartBeyondTheLoadWindowUnloadsTheQueue() {
        scheduleWithQueue("cup", inMinutes(30));
        Mockito.when(_tournamentDao.updateScheduledTournament(Mockito.any())).thenReturn(1);

        _service.updateScheduledTournament(info(params("cup", "Cup", inMinutes(30 * 24 * 60))), _now);

        assertNull("refreshQueues loads it again when it is due", _service.getTournamentQueue("cup"));
    }

    @Test
    public void anEditOfAnUnloadedTournamentLoadsItWhenItIsDueSoon() {
        scheduledRow("later", "Later cup", inMinutes(20 * 24 * 60), false);
        Mockito.when(_tournamentDao.updateScheduledTournament(Mockito.any())).thenReturn(1);

        _service.updateScheduledTournament(info(params("later", "Sooner cup", inMinutes(120))), _now);

        assertNotNull(_service.getTournamentQueue("later"));
        assertEquals("Sooner cup", _service.getTournamentQueue("later").getTournamentQueueName());
    }

    @Test
    public void whenTheRowCannotBeWrittenTheOldQueueIsRestored() {
        var oldQueue = scheduleWithQueue("cup", inMinutes(30));
        Mockito.when(_tournamentDao.updateScheduledTournament(Mockito.any())).thenReturn(0);

        try {
            _service.updateScheduledTournament(info(params("cup", "Changed", inMinutes(90))), _now);
            fail("expected the edit to fail");
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("started or was removed"));
        }
        var queue = _service.getTournamentQueue("cup");
        assertNotNull(queue);
        assertNotSame(oldQueue, queue);
        assertEquals("Cup", queue.getTournamentQueueName());
        assertTrue("players can sign up again", queue.isJoinable());
    }

    @Test
    public void aRetiredQueueTakesNobodyAndNeverReportsItselfFinished() throws Exception {
        var queue = scheduleWithQueue("cup", inMinutes(30));
        assertTrue(queue.retireIfEmpty());

        try {
            queue.joinPlayer(player("late"), new LotroDeck("late"));
            fail("a retired queue must refuse the join");
        } catch (HallException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("changed by an administrator"));
        }

        assertEquals(0, queue.getPlayerCount());
        assertFalse("a finished report would make processTournamentQueues remove the replacement by id", queue.process());
    }

    @Test
    public void aQueueWithPlayersCannotBeRetired() throws Exception {
        var queue = scheduleWithQueue("cup", inMinutes(30));
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));

        assertFalse(queue.retireIfEmpty());
        assertFalse(queue.isRetired());
    }

    // ---- list ----

    @Test
    public void theListHasLiveThenScheduledThenFinishedWithEachIdOnce() {
        record("live-1", "Live cup", "DECK_BUILDING", inMinutes(-600));
        record("live-2", "Newer live cup", "PAUSED", inMinutes(-60));
        _service.getTournamentById("live-1");
        _service.getTournamentById("live-2");

        var soon = info(params("soon", "Soon", inMinutes(60))).ToScheduledDB();
        var later = info(params("later", "Later", inMinutes(6000))).ToScheduledDB();
        var startedRow = info(params("live-1", "Live cup", inMinutes(-600))).ToScheduledDB();
        startedRow.started = true;
        Mockito.when(_tournamentDao.getScheduledTournamentsBetween(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(soon, later, startedRow));

        var finished = new DBDefs.Tournament();
        finished.tournament_id = "done";
        finished.name = "Done cup";
        finished.type = "CONSTRUCTED";
        finished.start_date = inMinutes(-10000);
        finished.parameters = JsonUtils.Serialize(params("done", "Done cup", finished.start_date));
        finished.stage = "FINISHED";
        finished.round = 5;
        var duplicate = new DBDefs.Tournament();
        duplicate.tournament_id = "live-2";
        duplicate.type = "CONSTRUCTED";
        duplicate.parameters = "{}";
        duplicate.start_date = inMinutes(-60);
        Mockito.when(_tournamentDao.getFinishedTournamentsBetween(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(finished, duplicate));

        var list = _service.getAdminTournamentList(_now, 60, 366);

        assertEquals(List.of("live-2", "live-1", "soon", "later", "done"),
                list.stream().map(TournamentService.AdminTournamentSummary::tournamentId).toList());
        assertEquals(TournamentService.ADMIN_STATUS_LIVE, list.get(0).status());
        assertEquals("PAUSED", list.get(0).stage());
        assertEquals(TournamentService.ADMIN_STATUS_SCHEDULED, list.get(2).status());
        assertNull(list.get(2).stage());
        assertEquals(TournamentService.ADMIN_STATUS_FINISHED, list.get(4).status());
        assertEquals("pc_fotr_block", list.get(4).format());
        assertEquals(5, list.get(4).round());
    }

    // ---- players ----

    @Test
    public void playersAreSortedWithDroppedAndDeckFlags() {
        Mockito.when(_playerDao.getPlayers("wc")).thenReturn(Set.of("bob", "Alice", "carol"));
        Mockito.when(_playerDao.getDroppedPlayers("wc")).thenReturn(Set.of("carol"));
        Mockito.when(_playerDao.getPlayerDecks("wc", "PC-FotR Block")).thenReturn(Map.of(
                "Alice", new LotroDeck("a"), "carol", new LotroDeck("c")));

        var players = _service.getAdminTournamentPlayers("wc", "PC-FotR Block");

        assertEquals(List.of("Alice", "bob", "carol"),
                players.stream().map(TournamentService.AdminTournamentPlayer::name).toList());
        assertTrue(players.get(0).hasDeck());
        assertFalse(players.get(0).dropped());
        assertFalse("no deck registered", players.get(1).hasDeck());
        assertTrue(players.get(2).dropped());
    }

    @Test
    public void aTournamentWithoutPlayersHasAnEmptyList() {
        assertTrue(_service.getAdminTournamentPlayers("empty", "x").isEmpty());
    }
}
