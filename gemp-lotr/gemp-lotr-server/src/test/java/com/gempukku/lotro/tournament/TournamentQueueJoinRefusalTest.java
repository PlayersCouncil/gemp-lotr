package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.hall.HallException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * Joining a tournament queue either signs the player up or throws a {@link HallException} saying why not; it never
 * silently does nothing (the hall used to report "joined" for a full queue, a closed sign-up or an unpaid entry).
 */
public class TournamentQueueJoinRefusalTest {
    private CollectionsManager _collections;

    @Before
    public void setUp() {
        _collections = Mockito.mock(CollectionsManager.class);
    }

    private static Player player(String name) {
        var player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    private static TournamentParams params(String name, ZonedDateTime start, int cost, int maxPlayers) {
        var params = new TournamentParams();
        params.tournamentId = "q-" + name;
        params.name = name;
        params.format = "pc_fotr_block";
        params.type = Tournament.TournamentType.CONSTRUCTED;
        params.startTime = start.toLocalDateTime().withSecond(0).withNano(0);
        params.playoff = Tournament.PairingType.SWISS;
        params.prizes = Tournament.PrizeType.NONE;
        params.cost = cost;
        params.minimumPlayers = 2;
        params.maximumPlayers = maxPlayers;
        return params;
    }

    private static TournamentInfo info(TournamentParams params) {
        return new TournamentInfo(null, null, null, params, params.tournamentId,
                DateUtils.ParseDate(params.startTime), params.getInitialStage(), 0);
    }

    private ScheduledTournamentQueue scheduled(String name, ZonedDateTime start, int cost, int maxPlayers) {
        return new ScheduledTournamentQueue(Mockito.mock(TournamentService.class), "q-" + name, name,
                info(params(name, start, cost, maxPlayers)), null, _collections);
    }

    private static String refusal(TournamentQueue queue, Player player) throws Exception {
        try {
            queue.joinPlayer(player, new LotroDeck(player.getName()));
        } catch (HallException expected) {
            return expected.getMessage();
        }
        fail("expected the join to be refused");
        return null;
    }

    @Test
    public void signUpNotOpenYetSaysWhenItOpens() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusHours(3), 0, -1);
        String message = refusal(queue, player("alice"));
        assertTrue(message, message.startsWith("Sign-up for Cup opens at "));
        assertEquals(0, queue.getPlayerCount());
    }

    @Test
    public void aFullQueueSaysItIsFull() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusMinutes(30), 0, 1);
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));
        assertEquals("Cup is full (1 players).", refusal(queue, player("bob")));
        assertEquals(1, queue.getPlayerCount());
    }

    @Test
    public void anUnpaidEntryIsRefusedWithTheCost() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusMinutes(30), 150, -1);
        var bob = player("bob");
        Mockito.when(_collections.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(bob),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(150))).thenReturn(false);

        String message = refusal(queue, bob);
        assertEquals("You don't have enough currency to join Cup: the entry cost is 1 gold 50 silver.", message);
        assertFalse(queue.isPlayerSignedUp("bob"));
    }

    @Test
    public void aPaidEntryJoins() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusMinutes(30), 150, -1);
        var bob = player("bob");
        Mockito.when(_collections.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(bob),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(150))).thenReturn(true);

        queue.joinPlayer(bob, new LotroDeck("bob"));
        assertTrue(queue.isPlayerSignedUp("bob"));
    }

    @Test
    public void aSecondJoinIsRefused() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusMinutes(30), 0, -1);
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));
        assertEquals("You have already joined that queue", refusal(queue, player("alice")));
        assertEquals(1, queue.getPlayerCount());
    }

    @Test
    public void aDecklessJoinOfADeckQueueIsRefused() throws Exception {
        var queue = scheduled("Cup", DateUtils.Now().plusMinutes(30), 0, -1);
        try {
            queue.joinPlayer(player("alice"));
            fail("a constructed queue needs a deck");
        } catch (HallException expected) {
            assertEquals("You need to choose a deck to join Cup.", expected.getMessage());
        }
        assertEquals(0, queue.getPlayerCount());
    }

    @Test
    public void aRecurringQueueSaysWhenTheNextSignUpOpens() throws Exception {
        var params = params("Daily", DateUtils.Now().plusHours(3), 0, -1);
        var queue = new RecurringScheduledQueue(Mockito.mock(TournamentService.class), "daily", "Daily",
                info(params), Duration.ofDays(1), null, _collections);
        String message = refusal(queue, player("alice"));
        assertTrue(message, message.startsWith("Sign-up for the next Daily opens at "));
    }

    @Test
    public void aPlayerMadeQueueThatIsFullSaysSo() throws Exception {
        var queue = new PlayerMadeQueue(Mockito.mock(TournamentService.class), "pm", "Casual Test",
                info(params("Casual Test", DateUtils.Now(), 0, 2)), false, -1, null, _collections);
        queue.joinPlayer(player("alice"), new LotroDeck("alice"));
        queue.joinPlayer(player("bob"), new LotroDeck("bob"));
        assertEquals("Casual Test is full (2 players).", refusal(queue, player("carol")));
    }

    @Test
    public void costsReadAsGoldAndSilver() {
        assertEquals("50 silver", AbstractTournamentQueue.describeCost(50));
        assertEquals("2 gold", AbstractTournamentQueue.describeCost(200));
        assertEquals("1 gold 5 silver", AbstractTournamentQueue.describeCost(105));
    }
}
