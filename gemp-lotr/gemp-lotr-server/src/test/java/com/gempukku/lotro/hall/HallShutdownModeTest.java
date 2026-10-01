package com.gempukku.lotro.hall;

import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.TournamentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Shutdown mode reaches the hall: every hall update says whether the server is in shutdown mode (the connection
 * readout turns yellow "Shutdown"), and both entering and leaving it wake the players' hall long-polls.
 */
public class HallShutdownModeTest {
    private HallServer _hall;
    private final Player _alice = new Player(1, "alice", "pass", "u", null, null, null, null, false);

    @Before
    public void setUp() {
        LotroFormatLibrary formatLibrary = Mockito.mock(LotroFormatLibrary.class);
        Mockito.when(formatLibrary.getHallFormats()).thenReturn(Map.of());

        ChatServer chatServer = Mockito.mock(ChatServer.class);
        Mockito.when(chatServer.createChatRoom(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(Mockito.mock(ChatRoomMediator.class));

        _hall = new HallServer(Mockito.mock(IgnoreDAO.class), Mockito.mock(LotroServer.class), chatServer,
                Mockito.mock(LeagueService.class), Mockito.mock(TournamentService.class),
                Mockito.mock(LotroCardBlueprintLibrary.class), formatLibrary, Mockito.mock(CollectionsManager.class),
                Mockito.mock(AdminService.class));
    }

    /** Signs alice up for the hall and returns her channel; the sign-up snapshot goes to <code>visitor</code>. */
    private HallCommunicationChannel signUp(HallChannelVisitor visitor) throws Exception {
        _hall.signupUserForHall(_alice, visitor);
        ArgumentCaptor<Integer> number = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(visitor).channelNumber(number.capture());
        return _hall.getCommunicationChannel(_alice, number.getValue());
    }

    /** True when the channel has news, i.e. a long-poll registered now would be answered at once. */
    private static boolean hasNews(HallCommunicationChannel channel) {
        return channel.registerRequest(() -> { });
    }

    @Test
    public void theHallIsToldWhenTheServerIsNotShuttingDown() throws Exception {
        HallChannelVisitor visitor = Mockito.mock(HallChannelVisitor.class);
        signUp(visitor);
        Mockito.verify(visitor).shutdownMode(false);
        Mockito.verify(visitor, Mockito.never()).shutdownMode(true);
    }

    @Test
    public void enteringAndLeavingShutdownModeWakesTheHallAndEveryUpdateCarriesTheFlag() throws Exception {
        HallCommunicationChannel channel = signUp(Mockito.mock(HallChannelVisitor.class));
        assertFalse("nothing new right after the sign-up snapshot", hasNews(channel));

        _hall.setShutdown(true);
        assertTrue("entering shutdown mode wakes the long-poll", hasNews(channel));
        HallChannelVisitor duringShutdown = Mockito.mock(HallChannelVisitor.class);
        channel.processCommunicationChannel(_hall, _alice, duringShutdown);
        Mockito.verify(duringShutdown).shutdownMode(true);

        // an update with nothing else new (the 5 s long-poll timing out) still says so
        HallChannelVisitor quietUpdate = Mockito.mock(HallChannelVisitor.class);
        channel.processCommunicationChannel(_hall, _alice, quietUpdate);
        Mockito.verify(quietUpdate).shutdownMode(true);

        _hall.setShutdown(false);
        assertTrue("leaving shutdown mode wakes the long-poll too", hasNews(channel));
        HallChannelVisitor afterShutdown = Mockito.mock(HallChannelVisitor.class);
        channel.processCommunicationChannel(_hall, _alice, afterShutdown);
        Mockito.verify(afterShutdown).shutdownMode(false);
        Mockito.verify(afterShutdown, Mockito.never()).shutdownMode(true);
    }

    @Test
    public void theFlagComesWithTheServerTime() throws Exception {
        HallChannelVisitor visitor = Mockito.mock(HallChannelVisitor.class);
        _hall.setShutdown(true);
        signUp(visitor);
        InOrder order = Mockito.inOrder(visitor);
        order.verify(visitor).serverTime(Mockito.anyString());
        order.verify(visitor).shutdownMode(true);
    }

    @Test
    public void newTablesAreRefusedInShutdownMode() throws Exception {
        _hall.setShutdown(true);
        try {
            _hall.createNewTable("test", _alice, "deck", "default", "", false, false, false);
            fail("expected a HallException");
        } catch (HallException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("shutdown mode"));
        }
    }
}
