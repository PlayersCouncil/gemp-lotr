package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.Test;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A game in progress taken back to an earlier decision (LotroGameMediator.revertTo): the game the players are sitting
 * at is replaced by one rebuilt from its journal, and everything around the game (who is listening, the clocks, the
 * players' settings, how it ends) has to carry on as if it were the same game.
 *
 * The games are ordinary ones, from the bids on, played by two players who answer at random: the point a game is
 * taken back to is as often in the middle of something (a target being chosen, a skirmish) as between things.
 */
public class GameMediatorRevertTest {
    private static final List<String> PLAYERS = MediatorSitting.PLAYERS;

    @Test
    public void AGameTakenBackIsWhereItWasAndPlaysOnToItsEnd() {
        Random random = new Random(20261118);
        int takenBack = 0;
        for (int game = 0; game < 30; game++) {
            MediatorSitting sitting = new MediatorSitting();
            //Half the games have a player who is passed through every phase from the start: which decisions a game
            // asks depends on it, so the rebuilt game has to be given the same settings at the same moments
            if (game % 2 == 1)
                sitting.mediator.setPlayerAutoPassSettings("Alice", EnumSet.allOf(Phase.class));
            sitting.mediator.startGame();
            sitting.play(5 + random.nextInt(90));
            if (game % 4 == 3)
                sitting.mediator.setPlayerAutoPassSettings("Bob", EnumSet.of(Phase.FELLOWSHIP, Phase.REGROUP));
            sitting.play(random.nextInt(10));
            if (sitting.mediator.isFinished())
                continue;

            int there = sitting.mediator.journalSize();
            String board = sitting.board();
            String waiting = sitting.waiting();
            Map<String, Integer> clocks = new LinkedHashMap<>(sitting.mediator.getPlayerClocks());

            sitting.play(1 + random.nextInt(25));
            if (sitting.mediator.isFinished())
                continue;

            assertNull("game " + game, sitting.mediator.revertTo(there));
            takenBack++;
            assertEquals("game " + game, there, sitting.mediator.journalSize());
            assertEquals("game " + game, board, sitting.board());
            assertEquals("game " + game, waiting, sitting.waiting());
            for (String player : PLAYERS)
                assertTrue("game " + game + ": no clock is given time back",
                        sitting.mediator.getPlayerClocks().get(player) >= clocks.get(player));

            sitting.play(20000);
            assertTrue("game " + game + " plays on to its end", sitting.mediator.isFinished());
            assertEquals("game " + game + " ends once, for whoever was listening before it was taken back: " + sitting.endings,
                    1, sitting.endings.size());
        }
        assertTrue("Most of the games get as far as being taken back: " + takenBack, takenBack >= 15);
    }

    @Test
    public void ASettingChangedSinceIsStillInForce() {
        MediatorSitting sitting = new MediatorSitting();
        sitting.mediator.startGame();
        sitting.play(12);
        int there = sitting.mediator.journalSize();
        sitting.play(3);
        sitting.mediator.setPlayerAutoPassSettings("Bob", EnumSet.of(Phase.MANEUVER));
        assertFalse(sitting.mediator.isFinished());

        assertNull(sitting.mediator.revertTo(there));

        assertTrue("The player's settings are as the player last set them, not as they were back then",
                sitting.mediator.getGame().shouldAutoPass("Bob", Phase.MANEUVER));
        assertEquals("...and the journal says so, so that the game can be rebuilt again", there + 1, sitting.mediator.journalSize());
        assertNull(sitting.mediator.revertTo(sitting.mediator.journalSize()));
        assertTrue(sitting.mediator.getGame().shouldAutoPass("Bob", Phase.MANEUVER));
    }

    @Test
    public void WhoeverWasWatchingIsSentTheTableAgainAndGoesOnWatching() throws Exception {
        MediatorSitting sitting = new MediatorSitting();
        Player alice = new Player(1, "Alice", "pass", "u", null, null, null, null, false);
        sitting.mediator.startGame();
        final int[] channelNumber = new int[1];
        sitting.mediator.signupUserForGame(alice, new ParticipantCommunicationVisitor() {
            @Override
            public void visitChannelNumber(int number) {
                channelNumber[0] = number;
            }

            @Override
            public void visitClock(Map<String, Integer> secondsLeft) { }

            @Override
            public void visitGameEvent(GameEvent gameEvent) { }
        });
        GameCommunicationChannel channel = sitting.mediator.getCommunicationChannel(alice, channelNumber[0]);
        //(few enough answers that no game is over yet: the soonest a Ring-bearer can die is the first skirmish)
        sitting.play(12);
        int there = sitting.mediator.journalSize();
        sitting.play(3);
        channel.consumeGameEvents();

        assertNull(sitting.mediator.revertTo(there));

        //The watcher is told, apart from the game's own events, to fetch the table again, and on which channel
        List<GameNotices.Notice> notices = sitting.mediator.getNotices().after("Alice", 0);
        GameNotices.Notice resync = notices.get(notices.size() - 1);
        assertEquals(GameNotices.RESYNC, resync.type());
        int newNumber = Integer.parseInt(resync.fields().get("channelNumber"));
        assertTrue(newNumber != channelNumber[0]);
        try {
            sitting.mediator.getCommunicationChannel(alice, channelNumber[0]);
            org.junit.Assert.fail("The channel the game was watched on before is gone, and anything sent on it is refused");
        } catch (com.gempukku.lotro.SubscriptionConflictException expected) {
        }
        assertTrue("...and nothing more is sent on it", channel.consumeGameEvents().isEmpty());

        channel = sitting.mediator.getCommunicationChannel(alice, newNumber);
        List<GameEvent> sent = channel.consumeGameEvents();
        assertEquals("The table is laid out again from nothing", GameEvent.Type.PARTICIPANTS, sent.get(0).getType());
        assertTrue(sent.stream().anyMatch(event -> event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY));
        assertTrue(sent.stream().anyMatch(event -> event.getType() == GameEvent.Type.SEND_MESSAGE
                && event.getMessage() != null && event.getMessage().contains("taken back")));

        sitting.play(10);
        assertFalse("What happens next at the rebuilt game is sent as well", channel.consumeGameEvents().isEmpty());
    }

    @Test
    public void AGameIsNotTakenBackToAPlaceItsJournalDoesNotHave() {
        MediatorSitting sitting = new MediatorSitting();
        sitting.mediator.startGame();
        sitting.play(8);
        String board = sitting.board();

        assertNotNull(sitting.mediator.revertTo(sitting.mediator.journalSize() + 1));
        assertNotNull(sitting.mediator.revertTo(-1));
        assertEquals("The game in progress is not touched", board, sitting.board());
    }

    @Test
    public void AGameThatIsOverIsNotTakenBack() {
        MediatorSitting sitting = new MediatorSitting();
        sitting.mediator.startGame();
        sitting.play(20000);
        assertTrue(sitting.mediator.isFinished());

        assertNotNull(sitting.mediator.revertTo(3));
        assertEquals(1, sitting.endings.size());
    }
}
