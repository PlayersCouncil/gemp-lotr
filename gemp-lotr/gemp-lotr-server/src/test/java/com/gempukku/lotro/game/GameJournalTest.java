package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioTable;
import com.gempukku.lotro.scenario.ScenarioTesting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GameJournalTest {
    private static List<PhysicalCard> deck() throws Exception {
        ScenarioTable table = ScenarioTesting.table("");
        return new ArrayList<>(table.gameState().getDeck(table.P1()));
    }

    private static List<Integer> ids(List<? extends PhysicalCard> cards) {
        return cards.stream().map(PhysicalCard::getCardId).toList();
    }

    @Test
    public void RandomOutcomesComeOutTheSameInAGameThatFollowsTheJournal() throws Exception {
        List<PhysicalCard> cards = deck();
        assertTrue("The test needs a deck worth shuffling", cards.size() >= 5);

        GameJournal first = new GameJournal();
        GameRandom firstRandom = first.random(GameRandom.DEFAULT);
        List<PhysicalCard> firstDeck = new ArrayList<>(cards);
        firstRandom.shuffleDeck("p", firstDeck);
        List<PhysicalCard> firstPick = firstRandom.pickCards("p", Zone.HAND, cards, 2);
        List<String> firstOrder = new ArrayList<>(List.of("a", "b", "c", "d", "e", "f"));
        firstRandom.shufflePlayers(firstOrder);
        firstRandom.shuffleDeck("p", firstDeck);

        //Often enough that chance alone would not pass it
        for (int i = 0; i < 20; i++) {
            GameJournal second = new GameJournal();
            second.replayRandomFrom(first);
            GameRandom secondRandom = second.random(GameRandom.DEFAULT);
            List<PhysicalCard> secondDeck = new ArrayList<>(cards);
            secondRandom.shuffleDeck("p", secondDeck);
            List<PhysicalCard> secondPick = secondRandom.pickCards("p", Zone.HAND, cards, 2);
            List<String> secondOrder = new ArrayList<>(List.of("a", "b", "c", "d", "e", "f"));
            secondRandom.shufflePlayers(secondOrder);
            secondRandom.shuffleDeck("p", secondDeck);

            assertEquals(ids(firstDeck), ids(secondDeck));
            assertEquals(ids(firstPick), ids(secondPick));
            assertEquals(firstOrder, secondOrder);
            assertNull(second.divergence());
            assertEquals("A game that followed a journal has written the same journal", first.write(), second.write());

            //Past the end of the earlier journal, chance is chance again, and is still written down
            secondRandom.shuffleDeck("p", secondDeck);
            assertEquals(5, second.entries().size());
            assertNull(second.divergence());
        }
    }

    @Test
    public void AGameThatGoesItsOwnWayStopsFollowing() throws Exception {
        List<PhysicalCard> cards = deck();
        GameJournal first = new GameJournal();
        first.random(GameRandom.DEFAULT).shuffleDeck("p", new ArrayList<>(cards));
        first.random(GameRandom.DEFAULT).shuffleDeck("p", new ArrayList<>(cards));

        //A pick where the earlier game shuffled
        GameJournal second = new GameJournal();
        second.replayRandomFrom(first);
        second.random(GameRandom.DEFAULT).pickCards("p", Zone.HAND, cards, 1);
        assertNotNull(second.divergence());
        assertTrue(second.divergence(), second.divergence().contains("a pick for p where the earlier game had a shuffle for p"));

        //The same shuffle, of a deck that is a card short
        GameJournal third = new GameJournal();
        third.replayRandomFrom(first);
        third.random(GameRandom.DEFAULT).shuffleDeck("p", new ArrayList<>(cards.subList(1, cards.size())));
        assertNotNull(third.divergence());
        //Once it has diverged it does not try to pick the thread up again
        third.random(GameRandom.DEFAULT).shuffleDeck("p", new ArrayList<>(cards));
        assertEquals(2, third.entries().size());
    }

    @Test
    public void AnswersAreWrittenWithTheFingerprintOfWhatWasAsked() throws Exception {
        ScenarioTable table = ScenarioTesting.table("");
        AwaitingDecision decision = table.GetAwaitingDecision(table.P1());
        assertNotNull(decision);

        GameJournal journal = new GameJournal(player -> player.equals(table.P1()) ? "one" : "two");
        journal.decisionAnswered(table.P1(), decision, "");
        GameJournal.Entry answer = journal.answers().getFirst();
        assertEquals("one", answer.player());
        assertEquals("", answer.value());
        assertTrue(answer.detail(), answer.detail().startsWith("CARD_ACTION_CHOICE:"));
        assertTrue(GameJournal.matches(answer, decision));
        assertFalse(GameJournal.matches(answer, null));

        //A different question has a different fingerprint, even when it is the same kind of question
        table.PlayerDecided(table.P1(), "");
        AwaitingDecision next = table.FreepsAnyDecisionsAvailable() ? table.GetAwaitingDecision(table.P1()) : table.GetAwaitingDecision(table.P2());
        assertNotEquals(GameJournal.fingerprint(decision), GameJournal.fingerprint(next));
        assertFalse(GameJournal.matches(answer, next));
    }

    @Test
    public void AJournalSurvivesBeingWrittenOutAndReadBack() {
        GameJournal journal = new GameJournal();
        journal.mark("step:1");
        journal.random(GameRandom.DEFAULT).shufflePlayers(new ArrayList<>(List.of("a", "b")));
        journal.mark("an awkward\tname\nwith a \\ in it");
        journal.random(GameRandom.DEFAULT).pickCards("p", Zone.HAND, List.of(), 1);

        GameJournal back = GameJournal.read(journal.write(), GameJournal.PlayerKeys.NAMES);
        assertEquals(journal.entries(), back.entries());
        assertEquals(journal.write(), back.write());
        assertTrue(back.hasMark("an awkward\tname\nwith a \\ in it"));
        assertEquals(0, GameJournal.read("", GameJournal.PlayerKeys.NAMES).entries().size());
    }

    @Test
    public void UpToAMarkIsEverythingBeforeItsLastOccurrence() {
        GameJournal journal = new GameJournal();
        journal.random(GameRandom.DEFAULT).shufflePlayers(new ArrayList<>(List.of("a", "b")));
        journal.mark("here");
        journal.random(GameRandom.DEFAULT).shufflePlayers(new ArrayList<>(List.of("a", "b")));
        journal.mark("here");
        journal.random(GameRandom.DEFAULT).shufflePlayers(new ArrayList<>(List.of("a", "b")));

        assertEquals(3, journal.upTo("here").entries().size());
        assertNull(journal.upTo("nowhere"));
        assertTrue(journal.hasMark("here"));
        assertFalse(journal.hasMark("nowhere"));
    }
}
