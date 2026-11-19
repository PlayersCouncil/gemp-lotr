package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Asking for a game to be taken back (LotroGameMediator.requestUndo): what is done at once, what the other player is
 * asked about and how that is settled, what the game does meanwhile, what is on offer when, and what everybody at the
 * table is told.  Ordinary games, played from the bids.
 */
public class UndoRequestTest {
    private static GameNotices.Notice lastNotice(MediatorSitting sitting, String player, String type) {
        GameNotices.Notice found = null;
        for (GameNotices.Notice notice : sitting.mediator.getNotices().after(player, 0))
            if (notice.type().equals(type))
                found = notice;
        return found;
    }

    /**
     * A game brought to its first turn in which whoever is asked first has a card to lay on the table.
     * @return The sitting, with that player being asked.
     */
    private static MediatorSitting withACardToPlay() {
        for (int game = 0; game < 60; game++) {
            MediatorSitting sitting = new MediatorSitting();
            String player = sitting.toFirstTurn();
            if (sitting.mediator.getGame().getGameState().getCurrentPhase() == Phase.FELLOWSHIP && sitting.playOfACardThatStays(player) != null)
                return sitting;
        }
        throw new AssertionError("No game in 60 began with a card to play");
    }

    private record Waiting(MediatorSitting sitting, String requester, LotroGameMediator.RestorePointView point) { }

    /**
     * A game played on at random until one of the players may ask to go back to something that is not theirs alone.
     */
    private static Waiting withSomethingToAskFor(UndoPolicy policy, long seed) {
        Random random = new Random(seed);
        for (int game = 0; game < 40; game++) {
            MediatorSitting sitting = new MediatorSitting();
            if (policy != null)
                sitting.mediator.setUndoPolicy(policy);
            sitting.toFirstTurn();
            for (int answers = 0; answers < 150 && !sitting.mediator.isFinished(); answers++) {
                sitting.play(1);
                for (String player : MediatorSitting.PLAYERS)
                    for (LotroGameMediator.RestorePointView point : sitting.mediator.getRestorePoints(player))
                        if (!point.tier().equals("GREEN") && random.nextInt(4) == 0 && !sitting.mediator.isFinished())
                            return new Waiting(sitting, player, point);
            }
        }
        throw new AssertionError("No game came to a point where there was something to ask for");
    }

    @Test
    public void OnesOwnChoiceIsTakenBackAtOnceAndEverybodyIsTold() {
        MediatorSitting sitting = withACardToPlay();
        String player = sitting.asked();
        String board = sitting.board();
        sitting.answer(player, sitting.playOfACardThatStays(player));

        LotroGameMediator.UndoReply reply = sitting.mediator.requestUndo(player, null);

        assertEquals(reply.message(), "done", reply.status());
        assertEquals(board, sitting.board());
        assertNull("Nobody was asked", sitting.mediator.getPendingUndo());
        for (String anybody : MediatorSitting.PLAYERS) {
            assertEquals("done", lastNotice(sitting, anybody, GameNotices.UNDO_RESOLVED).fields().get("outcome"));
            assertEquals("revert", lastNotice(sitting, anybody, GameNotices.RESYNC).fields().get("reason"));
        }
    }

    @Test
    public void OnlySoManyOfOnesOwnChoicesAreTakenBackInOnePhase() {
        MediatorSitting sitting = withACardToPlay();
        String player = sitting.asked();
        for (int i = 0; i < 3; i++) {
            sitting.answer(player, sitting.playOfACardThatStays(player));
            assertEquals("done", sitting.mediator.requestUndo(player, null).status());
        }
        sitting.answer(player, sitting.playOfACardThatStays(player));

        LotroGameMediator.UndoReply fourth = sitting.mediator.requestUndo(player, null);

        assertEquals("refused", fourth.status());
        assertTrue(fourth.message(), fourth.message().contains("3"));
        assertNotNull(sitting.mediator.getRestorePoints(player).get(0).unavailable());
    }

    @Test
    public void AnotherPlayersChoicesAreUndoneOnlyIfTheyAgreeAndTheGameWaitsMeanwhile() {
        Waiting waiting = withSomethingToAskFor(null, 1);
        MediatorSitting sitting = waiting.sitting();
        String other = sitting.other(waiting.requester());

        LotroGameMediator.UndoReply reply = sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount());

        assertEquals(reply.message(), "pending", reply.status());
        GameNotices.Notice asked = lastNotice(sitting, other, GameNotices.UNDO_REQUESTED);
        assertEquals(waiting.requester(), asked.fields().get("requester"));
        assertEquals(waiting.point().tier(), asked.fields().get("tier"));
        assertEquals(List.of(other), sitting.mediator.getPendingUndo().get("waitingOn"));

        int entries = sitting.mediator.journalSize();
        sitting.play(3);
        assertEquals("No answer is taken while the request waits", entries, sitting.mediator.journalSize());
        assertEquals("refused", sitting.mediator.requestUndo(other, null).status());
        assertEquals("Only whoever is asked can agree", "refused",
                sitting.mediator.replyUndo(waiting.requester(), reply.requestId(), "approve").status());

        LotroGameMediator.UndoReply settled = sitting.mediator.replyUndo(other, reply.requestId(), "approve");

        assertEquals(settled.message(), "done", settled.status());
        assertNull(sitting.mediator.getPendingUndo());
        assertEquals(waiting.point().entryCount(), sitting.mediator.journalSize());
        assertEquals(sitting.boardAt.get(waiting.point().entryCount()), sitting.board());
        assertTrue("The game goes on", sitting.play(3) > 0);
        assertTrue(sitting.mediator.journalSize() > waiting.point().entryCount());
    }

    @Test
    public void ARefusalLeavesTheGameAsItWas() {
        Waiting waiting = withSomethingToAskFor(null, 2);
        MediatorSitting sitting = waiting.sitting();
        String other = sitting.other(waiting.requester());
        String board = sitting.board();
        int entries = sitting.mediator.journalSize();
        int id = sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount()).requestId();

        assertEquals("refused", sitting.mediator.replyUndo(other, id, "decline").status());

        assertNull(sitting.mediator.getPendingUndo());
        assertEquals(board, sitting.board());
        assertEquals(entries, sitting.mediator.journalSize());
        assertEquals("declined", lastNotice(sitting, waiting.requester(), GameNotices.UNDO_RESOLVED).fields().get("outcome"));
        assertTrue("The game goes on", sitting.play(2) > 0 && sitting.mediator.journalSize() > entries);
        assertFalse("One refusal is not a refusal of everything", sitting.mediator.isAutoDeclining(other));
    }

    @Test
    public void APlayerCanRefuseEverythingFromThenOn() {
        Waiting waiting = withSomethingToAskFor(null, 3);
        MediatorSitting sitting = waiting.sitting();
        String other = sitting.other(waiting.requester());
        int id = sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount()).requestId();

        sitting.mediator.replyUndo(other, id, "declineAll");

        assertTrue(sitting.mediator.isAutoDeclining(other));
        LotroGameMediator.UndoReply again = sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount());
        assertEquals("refused", again.status());
        assertTrue(again.message(), again.message().contains(other));
        assertNull("Nobody is asked and the game does not wait", sitting.mediator.getPendingUndo());
        for (LotroGameMediator.RestorePointView point : sitting.mediator.getRestorePoints(waiting.requester()))
            assertEquals(point.label(), point.tier().equals("GREEN"), point.unavailable() == null);

        sitting.mediator.setAutoDecline(other, false);
        assertEquals("pending", sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount()).status());
    }

    @Test
    public void WhoeverAskedCanWithdrawAndNobodyElseCan() {
        Waiting waiting = withSomethingToAskFor(null, 4);
        MediatorSitting sitting = waiting.sitting();
        int id = sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount()).requestId();

        assertEquals("refused", sitting.mediator.replyUndo(sitting.other(waiting.requester()), id, "cancel").status());
        assertNotNull(sitting.mediator.getPendingUndo());
        sitting.mediator.replyUndo(waiting.requester(), id, "cancel");

        assertNull(sitting.mediator.getPendingUndo());
        assertEquals("cancelled", lastNotice(sitting, waiting.requester(), GameNotices.UNDO_RESOLVED).fields().get("outcome"));
    }

    @Test
    public void ARequestNobodyAnswersIsRefusedWhenItsTimeIsUp() throws Exception {
        Waiting waiting = withSomethingToAskFor(new UndoPolicy(true, true, true, false, true, 0, 3), 5);
        MediatorSitting sitting = waiting.sitting();
        int clock = sitting.mediator.getPlayerClocks().get(waiting.requester());
        sitting.mediator.requestUndo(waiting.requester(), waiting.point().entryCount());
        assertNotNull(sitting.mediator.getPendingUndo());
        Thread.sleep(1100);
        assertTrue("The wait is on the clock of whoever asked", sitting.mediator.getCurrentUserPendingTime(waiting.requester()) >= 1);
        assertEquals(0, sitting.mediator.getCurrentUserPendingTime(sitting.other(waiting.requester())));

        sitting.mediator.cleanup();

        assertNull(sitting.mediator.getPendingUndo());
        assertEquals("timedOut", lastNotice(sitting, waiting.requester(), GameNotices.UNDO_RESOLVED).fields().get("outcome"));
        assertTrue("...and stays there", sitting.mediator.getPlayerClocks().get(waiting.requester()) >= clock + 1);
    }

    @Test
    public void WhereOnlyOnesOwnChoicesMayBeTakenBackNothingElseCanBeAskedFor() {
        Waiting waiting = withSomethingToAskFor(UndoPolicy.TOURNAMENT, 6);

        LotroGameMediator.UndoReply reply = waiting.sitting().mediator.requestUndo(waiting.requester(), waiting.point().entryCount());

        assertEquals("refused", reply.status());
        assertNotNull(waiting.point().unavailable());
        assertNull(waiting.sitting().mediator.getPendingUndo());
    }

    @Test
    public void AnOpponentWhoAlwaysAgreesIsNotAsked() {
        Waiting waiting = withSomethingToAskFor(UndoPolicy.SOLO, 7);

        LotroGameMediator.UndoReply reply = waiting.sitting().mediator.requestUndo(waiting.requester(), waiting.point().entryCount());

        assertEquals(reply.message(), "done", reply.status());
        assertEquals(waiting.sitting().boardAt.get(waiting.point().entryCount()), waiting.sitting().board());
    }

    @Test
    public void TheBidsAndMulligansAreNeverOnOfferAndAStartingFellowshipOnlyUntilTheHandsAreDealt() {
        MediatorSitting sitting = new MediatorSitting();
        String first = sitting.toStartingFellowships();
        for (String player : MediatorSitting.PLAYERS)
            assertTrue("A bid is not gone back to", sitting.mediator.getRestorePoints(player).isEmpty());

        sitting.pick(first);
        //(A pick never completes the fellowship by itself: with nothing more to add, the player is asked to press DONE)
        assertTrue(sitting.waiting(), sitting.asked(first, "Starting fellowship"));
        List<LotroGameMediator.RestorePointView> points = sitting.mediator.getRestorePoints(first);
        assertEquals(1, points.size());
        assertTrue(points.get(0).label(), points.get(0).label().startsWith("Played "));

        String mulligans = sitting.toMulligans();
        for (String player : MediatorSitting.PLAYERS)
            assertTrue("With the hands dealt the fellowships are closed", sitting.mediator.getRestorePoints(player).isEmpty());
        sitting.answer(mulligans, "0");
        sitting.answer(sitting.other(mulligans), "1");
        for (String player : MediatorSitting.PLAYERS)
            assertTrue("A mulligan is not gone back to", sitting.mediator.getRestorePoints(player).isEmpty());
    }

    @Test
    public void TheLastPickOfTheStartingFellowshipsIsTakenBackAtOnceUntilDoneIsPressedAndNotAfter() {
        MediatorSitting sitting = new MediatorSitting(Boards.ONE_COMPANION, Boards.ONE_COMPANION);
        String first = sitting.toStartingFellowships();
        String second = sitting.other(first);
        sitting.pick(first);
        sitting.pressDone(first);

        String board = sitting.board();
        String waiting = sitting.waiting();
        int lastPick = sitting.mediator.journalSize();
        sitting.pick(second);

        //Merry was the one companion to add: the second player is asked only to press DONE, and the pick is on offer
        assertTrue(sitting.waiting(), sitting.askedToPressDone(second));
        List<LotroGameMediator.RestorePointView> points = sitting.mediator.getRestorePoints(second);
        assertEquals(lastPick, points.get(0).entryCount());
        assertEquals("GREEN", points.get(0).tier());

        LotroGameMediator.UndoReply reply = sitting.mediator.requestUndo(second, null);

        assertEquals(reply.message(), "done", reply.status());
        assertNull("Nobody was asked", sitting.mediator.getPendingUndo());
        assertEquals(board, sitting.board());
        assertEquals("The second player is choosing again, with Merry on offer", waiting, sitting.waiting());

        //Chosen again, and DONE pressed: that has the hands dealt, and the fellowships are closed
        sitting.pick(second);
        sitting.pressDone(second);

        assertTrue(sitting.waiting(), sitting.asked(first, "Do you wish to mulligan"));
        for (String player : MediatorSitting.PLAYERS)
            assertTrue("With the hands dealt the fellowships are closed", sitting.mediator.getRestorePoints(player).isEmpty());
        assertEquals("refused", sitting.mediator.requestUndo(second, lastPick).status());
    }

    @Test
    public void TheTurnBeforeIsWithinReachUntilTheNewTurnComesToItsManeuverPhase() {
        int early = 0;
        int late = 0;
        for (int game = 0; game < 40 && (early < 3 || late < 3); game++) {
            MediatorSitting sitting = new MediatorSitting();
            sitting.toFirstTurn();
            while (!sitting.mediator.isFinished() && sitting.mediator.getGame().getGameState().getTurnNumber() < 3) {
                sitting.play(1);
                if (sitting.mediator.isFinished())
                    break;
                int turn = sitting.mediator.getGame().getGameState().getTurnNumber();
                Phase phase = sitting.mediator.getGame().getGameState().getCurrentPhase();
                if (turn != 2)
                    continue;
                for (String player : MediatorSitting.PLAYERS) {
                    boolean reachesBack = sitting.mediator.getRestorePoints(player).stream().anyMatch(point -> point.turn() == 1);
                    boolean answeredLastTurn = sitting.mediator.getRestorePoints(player).stream().anyMatch(point -> point.turn() < 1);
                    assertFalse("Nothing before the turn before is ever on offer", answeredLastTurn);
                    if (phase == Phase.FELLOWSHIP || phase == Phase.SHADOW) {
                        if (reachesBack)
                            early++;
                    } else if (phase == Phase.MANEUVER || phase == Phase.ARCHERY || phase == Phase.ASSIGNMENT
                            || phase == Phase.SKIRMISH || phase == Phase.REGROUP) {
                        assertFalse("game " + game + ": " + player + " in " + phase, reachesBack);
                        late++;
                    }
                }
            }
        }
        assertTrue("The turn before was on offer early in the next " + early + " times", early >= 3);
        assertTrue("...and looked for later " + late + " times", late >= 3);
    }

    @Test
    public void AConcessionIsToldToThePlayerWhoIsLookingAtADecision() {
        MediatorSitting sitting = new MediatorSitting();
        String asked = sitting.toFirstTurn();
        String other = sitting.other(asked);
        assertNull(lastNotice(sitting, asked, GameNotices.RESYNC));

        sitting.mediator.concede(new Player(2, other, "pass", "u", null, null, null, null, false));

        assertTrue(sitting.mediator.isFinished());
        assertEquals("ended", lastNotice(sitting, asked, GameNotices.RESYNC).fields().get("reason"));
    }
}
