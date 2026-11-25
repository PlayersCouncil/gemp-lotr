package com.gempukku.lotro.hall;

import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.game.Adventure;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentQueue;
import com.gempukku.lotro.tournament.TournamentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link HallServer#isPlayerBusy}: what counts as being in the middle of something, which is what keeps a PC
 * account's other aliases from being entered (PC-ACCOUNT.md, "Switching is gated").
 */
public class HallServerBusyTest {
    private LotroServer _lotroServer;
    private LotroGameMediator _game;
    private TournamentService _tournamentService;
    private HallServer _hall;

    private final Player _alice = player(1, "alice");
    private final Player _bob = player(2, "bob");

    @Before
    public void setUp() {
        _game = Mockito.mock(LotroGameMediator.class);
        Mockito.when(_game.getPlayersPlaying()).thenReturn(List.of("alice", "bob"));
        _lotroServer = Mockito.mock(LotroServer.class);
        Mockito.when(_lotroServer.createNewGame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(_game);
        _tournamentService = Mockito.mock(TournamentService.class);

        LotroFormat format = Mockito.mock(LotroFormat.class);
        Mockito.when(format.getName()).thenReturn("Test Format");
        Mockito.when(format.getAdventure()).thenReturn(Mockito.mock(Adventure.class));
        Mockito.when(format.applyErrata(Mockito.any(LotroDeck.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(format.validateDeckForHall(Mockito.any(LotroDeck.class), Mockito.any())).thenReturn("");
        LotroFormatLibrary formatLibrary = Mockito.mock(LotroFormatLibrary.class);
        Mockito.when(formatLibrary.getFormat("test")).thenReturn(format);
        Mockito.when(formatLibrary.getHallFormats()).thenReturn(Map.of("test", format));

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.getPlayerCollection(Mockito.any(Player.class), Mockito.anyString()))
                .thenReturn(Mockito.mock(CardCollection.class));
        ChatServer chatServer = Mockito.mock(ChatServer.class);
        Mockito.when(chatServer.createChatRoom(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(Mockito.mock(ChatRoomMediator.class));

        _hall = new HallServer(Mockito.mock(IgnoreDAO.class), _lotroServer, chatServer, Mockito.mock(LeagueService.class),
                _tournamentService, Mockito.mock(LotroCardBlueprintLibrary.class), formatLibrary, collectionsManager,
                Mockito.mock(AdminService.class));

        for (Player player : List.of(_alice, _bob)) {
            LotroDeck deck = new LotroDeck("Mine");
            deck.setTargetFormat("Test Format");
            Mockito.when(_lotroServer.getParticipantDeck(player, "Mine")).thenReturn(deck);
        }
    }

    private static Player player(int id, String name) {
        return new Player(id, name, "pass", "u", null, null, null, null, false);
    }

    @Test
    public void aPlayerDoingNothingIsNotBusy() {
        assertFalse(_hall.isPlayerBusy("alice"));
        assertFalse(_hall.isPlayerBusy("nobody at all"));
    }

    @Test
    public void sittingAtAWaitingTableIsBusy() throws Exception {
        _hall.createNewTable("test", _alice, "Mine", "default", "", false, false, false);
        assertTrue(_hall.isPlayerBusy("alice"));
        assertFalse(_hall.isPlayerBusy("bob"));
    }

    @Test
    public void aGameIsBusyUntilItIsOver() throws Exception {
        _hall.createNewTable("test", _alice, "Mine", "default", "", false, false, false);
        assertTrue(_hall.joinTableAsPlayer(waitingTables().keySet().iterator().next(), _bob, "Mine"));
        assertTrue("the table started", waitingTables().isEmpty());

        assertTrue(_hall.isPlayerBusy("alice"));
        assertTrue(_hall.isPlayerBusy("bob"));
        assertFalse("watching is not playing", _hall.isPlayerBusy("carol"));

        Mockito.when(_game.isFinished()).thenReturn(true);
        assertFalse(_hall.isPlayerBusy("alice"));
        assertFalse(_hall.isPlayerBusy("bob"));
    }

    @Test
    public void beingSignedUpInAQueueIsBusy() {
        TournamentQueue queue = Mockito.mock(TournamentQueue.class);
        Mockito.when(queue.isPlayerSignedUp("alice")).thenReturn(true);
        Mockito.when(_tournamentService.getAllTournamentQueues()).thenReturn(List.of(Mockito.mock(TournamentQueue.class), queue));
        assertTrue(_hall.isPlayerBusy("alice"));
        assertFalse(_hall.isPlayerBusy("bob"));
    }

    @Test
    public void beingStillInARunningTournamentIsBusy() {
        // isPlayerInCompetition is false for a tournament that is over and for a player who has dropped
        Tournament tournament = Mockito.mock(Tournament.class);
        Mockito.when(tournament.isPlayerInCompetition("alice")).thenReturn(true);
        Mockito.when(_tournamentService.getLiveTournaments()).thenReturn(List.of(Mockito.mock(Tournament.class), tournament));
        assertTrue(_hall.isPlayerBusy("alice"));
        assertFalse(_hall.isPlayerBusy("bob"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, GameTable> waitingTables() throws Exception {
        java.lang.reflect.Field holderField = HallServer.class.getDeclaredField("tableHolder");
        holderField.setAccessible(true);
        java.lang.reflect.Field awaiting = TableHolder.class.getDeclaredField("awaitingTables");
        awaiting.setAccessible(true);
        return (Map<String, GameTable>) awaiting.get(holderField.get(_hall));
    }
}
