package com.gempukku.lotro.game;

import com.gempukku.lotro.bots.AssignmentLegality;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.db.DeckSerialization;
import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.ArrayList;
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
    private static final String FORMAT = "fotr_block";
    private static final List<String> PLAYERS = List.of("Alice", "Bob");

    /** A game at a mediator, with two players who answer at random and somebody counting how it ends. */
    private static class Sitting {
        final LotroGameMediator mediator;
        final Map<String, RandomDecisionBot> bots = new LinkedHashMap<>();
        final List<String> endings = new ArrayList<>();

        Sitting() {
            LotroDeck aragorn = DeckSerialization.buildDeckFromContents("Aragorn Starter", Boards.ARAGORN_STARTER, FORMAT, "Aragorn Starter");
            LotroDeck gandalf = DeckSerialization.buildDeckFromContents("Gandalf Starter", Boards.GANDALF_STARTER, FORMAT, "Gandalf Starter");
            LotroGameParticipant[] participants = {new LotroGameParticipant("Alice", aragorn), new LotroGameParticipant("Bob", gandalf)};
            mediator = new LotroGameMediator("1", LoadedLibraries._formatLibrary.getFormat(FORMAT), participants, LoadedLibraries._cardLibrary,
                    new GameTimer(false, "Test", 60 * 80, 60 * 5), true, false, false, "Casual", new MarkdownParser(), null, false, null, null);
            for (String player : PLAYERS)
                bots.put(player, new RandomDecisionBot(player));
            mediator.addGameResultListener(new GameResultListener() {
                @Override
                public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                    endings.add(winnerPlayerId + " won: " + winReason);
                }

                @Override
                public void gameCancelled() {
                    endings.add("cancelled");
                }
            });
        }

        String asked() {
            for (String player : PLAYERS)
                if (mediator.awaitingDecision(player) != null)
                    return player;
            return null;
        }

        /**
         * Has whoever is being asked answer, that many times over or until the game ends.
         * @return How many answers were given.
         */
        int play(int answers) {
            int given = 0;
            while (given < answers && !mediator.isFinished()) {
                String player = asked();
                if (player == null)
                    break;
                AwaitingDecision decision = mediator.awaitingDecision(player);
                String answer = bots.get(player).chooseAction(mediator.getGame().getGameState(), decision, new AssignmentLegality(mediator.getGame()));
                //The bots' door: it takes an answer by name, and puts a refused one right
                mediator.botAnswered(player, decision.getAwaitingDecisionId(), answer);
                given++;
            }
            return given;
        }

        String board() {
            return Boards.of(mediator.getGame());
        }

        /** What each player is being asked, as the journal would know it. */
        String waiting() {
            StringBuilder sb = new StringBuilder();
            for (String player : PLAYERS) {
                AwaitingDecision decision = mediator.awaitingDecision(player);
                sb.append(player).append('=').append(decision == null ? "-" : GameJournal.fingerprint(decision)).append(' ');
            }
            return sb.toString();
        }
    }

    @Test
    public void AGameTakenBackIsWhereItWasAndPlaysOnToItsEnd() {
        Random random = new Random(20261118);
        int takenBack = 0;
        for (int game = 0; game < 30; game++) {
            Sitting sitting = new Sitting();
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
        Sitting sitting = new Sitting();
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
        Sitting sitting = new Sitting();
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
        sitting.play(20);
        int there = sitting.mediator.journalSize();
        sitting.play(5);
        channel.consumeGameEvents();

        assertNull(sitting.mediator.revertTo(there));

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
        Sitting sitting = new Sitting();
        sitting.mediator.startGame();
        sitting.play(8);
        String board = sitting.board();

        assertNotNull(sitting.mediator.revertTo(sitting.mediator.journalSize() + 1));
        assertNotNull(sitting.mediator.revertTo(-1));
        assertEquals("The game in progress is not touched", board, sitting.board());
    }

    @Test
    public void AGameThatIsOverIsNotTakenBack() {
        Sitting sitting = new Sitting();
        sitting.mediator.startGame();
        sitting.play(20000);
        assertTrue(sitting.mediator.isFinished());

        assertNotNull(sitting.mediator.revertTo(3));
        assertEquals(1, sitting.endings.size());
    }
}
