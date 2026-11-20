package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A game taken back (LotroGameMediator.revertTo) across a voluntary loop the players settled (LoopRun).
 *
 * The rounds of a loop's run are not played by the players: the game answers the decisions inside them itself, as
 * they were answered in the round played by hand (LoopFeedback), so none of those answers is in the journal.  The
 * journal holds the two rounds played by hand and what the players settled, and a game rebuilt from it has to find
 * the loop again and run it again to the same end.
 *
 * The loop is Merry, Rohirrim Squire 7_320 ("Skirmish: If you cannot spot 3 threats, add a threat to make Merry
 * strength +2.") against Mordor Soldier 7_290 ("Skirmish: Remove a threat to make this minion strength +4."), as in
 * VoluntaryLoopTests, reached here in an ordinary game at a mediator: both decks are Frodo, Merry and forty
 * Soldiers, so whoever goes first starts with Merry and the other player has a Soldier to play.
 */
public class LoopRevertTest {
    private static final String MERRY = "7_320";
    private static final String DECK = "1_290|1_2|1_320,1_327,1_340,1_346,1_349,1_351,1_355,1_358,1_361|" + MERRY
            + ",7_290".repeat(40);

    private MediatorSitting sitting;
    private String freeps;
    private String shadow;

    // Where the journal stood at moments the tests go back to
    private int beforeShadowsSecondUse;
    private int beforeShadowAgrees;
    private int afterTheRun;

    /** An ordinary game up to the skirmish between Merry and a Soldier, each player's ability used once. */
    private void toTheSkirmish() {
        sitting = new MediatorSitting(DECK, DECK);
        String first = sitting.toStartingFellowships();
        //Merry is the one companion in each deck: once he is down nothing more can be added, and DONE is pressed
        sitting.pick(first);
        sitting.pressDone(first);
        sitting.pick(sitting.other(first));
        sitting.pressDone(sitting.other(first));
        String mulligan = sitting.asked();
        sitting.answer(mulligan, "0");
        sitting.answer(sitting.other(mulligan), "0");

        freeps = sitting.asked();
        shadow = sitting.other(freeps);
        assertTrue(sitting.asked(freeps, "Play Fellowship action"));
        sitting.answer(freeps, "");
        assertTrue(sitting.asked(shadow, "Play Shadow action"));
        sitting.answer(shadow, "0");
        sitting.answer(shadow, "");
        //Maneuver, archery and assignment actions: nobody has any
        for (int i = 0; i < 6; i++)
            sitting.answer(sitting.asked(), "");

        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, sitting.mediator.awaitingDecision(freeps).getDecisionType());
        String merry = null;
        for (String character : sitting.parameter(freeps, "freeCharacters"))
            if (sitting.mediator.getGame().getGameState().findCardById(Integer.parseInt(character)).getBlueprintId().equals(MERRY))
                merry = character;
        sitting.answer(freeps, merry + " " + sitting.parameter(freeps, "minions")[0]);
        assertTrue(sitting.asked(freeps, "Choose next skirmish"));
        sitting.answer(freeps, merry);

        use(freeps);
        use(shadow);
    }

    /** The second round by hand, the loop found, and what the players settle carried out. */
    private void throughTheLoop() {
        use(freeps);
        beforeShadowsSecondUse = sitting.mediator.journalSize();
        use(shadow);

        assertTrue("the loop is found: " + sitting.waiting(), sitting.asked(freeps, "LOOP DETECTED"));
        sitting.answer(freeps, "3");
        assertTrue(sitting.asked(shadow, "LOOP DETECTED"));
        beforeShadowAgrees = sitting.mediator.journalSize();
        sitting.answer(shadow, "0");
        afterTheRun = sitting.mediator.journalSize();
    }

    private void use(String player) {
        assertEquals(player + " has the one action: " + sitting.waiting(), 1, sitting.parameter(player, "actionId").length);
        sitting.answer(player, sitting.parameter(player, "actionId")[0]);
    }

    private int threats() {
        return sitting.mediator.getGame().getGameState().getThreats();
    }

    @Test
    public void AGameTakenBackToJustAfterALoopRanIsWhereTheRunLeftIt() {
        toTheSkirmish();
        throughTheLoop();
        String board = sitting.board();
        String waiting = sitting.waiting();
        int threats = threats();
        assertTrue("Merry's ability is barred after the run", barred(freeps));

        //On through the end of the skirmish and the regroup phase
        sitting.answer(freeps, "");
        sitting.answer(shadow, "");
        sitting.answer(sitting.asked(), "");
        sitting.answer(sitting.asked(), "");

        assertNull(sitting.mediator.revertTo(afterTheRun));
        assertEquals(board, sitting.board());
        assertEquals(waiting, sitting.waiting());
        assertEquals(threats, threats());
        assertTrue("and is barred still in the game built again", barred(freeps));

        sitting.play(20000);
        assertTrue("the game plays on to its end", sitting.mediator.isFinished());
        assertEquals(sitting.endings.toString(), 1, sitting.endings.size());
    }

    @Test
    public void AGameTakenBackIntoTheRoundsPlayedByHandFindsTheLoopAgain() {
        toTheSkirmish();
        throughTheLoop();
        String afterTheRunBoard = sitting.board();
        String afterTheRunWaiting = sitting.waiting();

        assertNull(sitting.mediator.revertTo(beforeShadowsSecondUse));
        assertEquals(sitting.boardAt.get(beforeShadowsSecondUse), sitting.board());
        assertTrue("the Shadow player is asked for the second use again: " + sitting.waiting(), sitting.asked(shadow, "Choose action to play"));

        use(shadow);
        assertTrue("the loop is found again: " + sitting.waiting(), sitting.asked(freeps, "LOOP DETECTED"));
        sitting.answer(freeps, "3");
        sitting.answer(shadow, "0");
        assertEquals("the same settlement comes out the same", afterTheRunBoard, sitting.board());
        assertEquals(afterTheRunWaiting, sitting.waiting());
    }

    @Test
    public void AGameTakenBackToTheSettlingOfALoopCanSettleItAnotherWay() {
        toTheSkirmish();
        throughTheLoop();
        int agreedThreats = threats();

        assertNull(sitting.mediator.revertTo(beforeShadowAgrees));
        assertEquals(sitting.boardAt.get(beforeShadowAgrees), sitting.board());
        assertTrue(sitting.asked(shadow, "LOOP DETECTED"));

        //A lower number this time: the Free Peoples player then takes the last action, so a threat is left on the table
        sitting.answer(shadow, "1");
        AwaitingDecision lower = sitting.mediator.awaitingDecision(shadow);
        assertEquals("the Shadow player names the number: " + sitting.waiting(), AwaitingDecisionType.INTEGER, lower.getDecisionType());
        sitting.answer(shadow, "1");
        assertEquals("one threat more than when the Shadow player agreed", agreedThreats + 1, threats());

        sitting.play(20000);
        assertTrue("the game plays on to its end", sitting.mediator.isFinished());
    }

    private boolean barred(String player) {
        String[] barred = sitting.parameter(player, "loopBanned");
        return barred != null && barred.length == 1 && barred[0].equals("true");
    }
}
