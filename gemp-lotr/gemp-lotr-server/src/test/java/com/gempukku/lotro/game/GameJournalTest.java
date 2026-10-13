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

    private static AwaitingDecision actions(String... cardAndText) {
        List<String> cardIds = new ArrayList<>(), blueprints = new ArrayList<>(), texts = new ArrayList<>();
        for (String entry : cardAndText) {
            cardIds.add(entry.split("=")[0]);
            blueprints.add("inPlay");
            texts.add(entry.split("=")[1]);
        }
        return new com.gempukku.lotro.logic.decisions.CardActionSelectionDecision(1, "Play Fellowship action or Pass", cardIds, blueprints, texts) {
            @Override
            public void decisionMade(String result) { }
        };
    }

    //The same choice of actions as the server sends it now, with its opinion of which of them would do nothing
    private static AwaitingDecision actionsJudged(String[] noEffect, String... cardAndText) {
        List<String> cardIds = new ArrayList<>(), blueprints = new ArrayList<>(), texts = new ArrayList<>();
        for (String entry : cardAndText) {
            cardIds.add(entry.split("=")[0]);
            blueprints.add("inPlay");
            texts.add(entry.split("=")[1]);
        }
        return new com.gempukku.lotro.logic.decisions.CardActionSelectionDecision(1, "Play Fellowship action or Pass", cardIds, blueprints, texts) {
            {
                setParam("noEffect", noEffect);
            }

            @Override
            public void decisionMade(String result) { }
        };
    }

    private static AwaitingDecision arbitrary(String... blueprints) {
        List<String> ids = new ArrayList<>(), selectable = new ArrayList<>();
        for (int i = 0; i < blueprints.length; i++) {
            ids.add("temp" + i);
            selectable.add("true");
        }
        return new com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision(1, "Choose cards from deck", ids, List.of(blueprints), selectable, 0, 2) {
            @Override
            public void decisionMade(String result) { }
        };
    }

    private static AwaitingDecision choice(String... options) {
        return new com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision(1, "Do you want to make another move?", options) {
            @Override
            protected void validDecisionMade(int index, String result) { }
        };
    }

    @Test
    public void TheSameQuestionWithItsOptionsInAnotherOrderHasTheSameFingerprint() {
        AwaitingDecision one = actions("12=Play Gandalf", "15=Use Sam", "12=Use Gandalf");
        AwaitingDecision two = actions("12=Use Gandalf", "12=Play Gandalf", "15=Use Sam");
        assertEquals(GameJournal.fingerprint(one), GameJournal.fingerprint(two));
        //It is the pairing that counts, not just which cards and which texts appear
        assertNotEquals(GameJournal.fingerprint(one), GameJournal.fingerprint(actions("12=Play Gandalf", "12=Use Sam", "15=Use Gandalf")));
        assertNotEquals(GameJournal.fingerprint(one), GameJournal.fingerprint(actions("12=Play Gandalf", "15=Use Sam")));

        assertEquals(GameJournal.fingerprint(arbitrary("1_83", "1_116", "1_83")), GameJournal.fingerprint(arbitrary("1_116", "1_83", "1_83")));
        assertNotEquals(GameJournal.fingerprint(arbitrary("1_83", "1_116", "1_83")), GameJournal.fingerprint(arbitrary("1_83", "1_116", "1_116")));
    }

    @Test
    public void WhichActionsWouldDoNothingIsNoPartOfTheQuestion() {
        //A journal written before decisions said which actions have no effect must go on matching them, and a
        // change in what the server can foresee must not stop a journal matching either
        AwaitingDecision before = actions("12=Play Gandalf", "15=Use Sam", "12=Use Gandalf");
        AwaitingDecision unflagged = actionsJudged(new String[]{"false", "false", "false"}, "12=Play Gandalf", "15=Use Sam", "12=Use Gandalf");
        AwaitingDecision flagged = actionsJudged(new String[]{"false", "skirmish", "effect"}, "12=Play Gandalf", "15=Use Sam", "12=Use Gandalf");
        assertNotNull(flagged.getDecisionParameters().get("noEffect"));
        assertNull(before.getDecisionParameters().get("noEffect"));
        assertEquals(GameJournal.fingerprint(before), GameJournal.fingerprint(unflagged));
        assertEquals(GameJournal.fingerprint(unflagged), GameJournal.fingerprint(flagged));
        //The actions themselves still are
        assertNotEquals(GameJournal.fingerprint(flagged), GameJournal.fingerprint(actionsJudged(new String[]{"false", "skirmish", "effect"}, "12=Play Gandalf", "15=Use Sam", "12=Use Frodo")));
    }

    @Test
    public void AnAnswerThatIsAPositionIsWrittenDownAsWhatWasChosen() {
        AwaitingDecision one = actions("12=Play Gandalf", "15=Use Sam", "12=Use Gandalf");
        AwaitingDecision reordered = actions("12=Use Gandalf", "12=Play Gandalf", "15=Use Sam");
        assertEquals("15|Use Sam", GameJournal.stable(one, "1"));
        assertEquals("2", GameJournal.concrete(reordered, "15|Use Sam"));
        assertEquals("1", GameJournal.concrete(reordered, GameJournal.stable(one, "0")));
        assertEquals("Passing is passing", "", GameJournal.concrete(reordered, GameJournal.stable(one, "")));
        assertNull("An action that is not on offer any more", GameJournal.concrete(actions("12=Play Gandalf"), "15|Use Sam"));

        AwaitingDecision cards = arbitrary("1_83", "1_116", "1_83");
        AwaitingDecision cardsReordered = arbitrary("1_116", "1_83", "1_83");
        assertEquals("b1_83,b1_83", GameJournal.stable(cards, "temp0,temp2"));
        assertEquals("temp1,temp2", GameJournal.concrete(cardsReordered, "b1_83,b1_83"));
        assertEquals("temp0", GameJournal.concrete(cardsReordered, GameJournal.stable(cards, "temp1")));
        assertNull("Two copies where there is only one", GameJournal.concrete(arbitrary("1_83", "1_116"), "b1_83,b1_83"));
        assertEquals("", GameJournal.concrete(cardsReordered, GameJournal.stable(cards, "")));

        assertEquals("No", GameJournal.stable(choice("Yes", "No"), "1"));
        assertEquals("0", GameJournal.concrete(choice("No", "Yes"), "No"));
        assertNull(GameJournal.concrete(choice("Yes", "Maybe"), "No"));
    }

    @Test
    public void AnswersThatAreAlreadyStableAreLeftAlone() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		shadow: {
                			minions: runner1
                		}
                	}
                """);
        //Card IDs and numbers mean the same thing in any run of the same game
        AwaitingDecision decision = table.GetAwaitingDecision(table.P1());
        assertEquals("", GameJournal.stable(decision, ""));
        assertEquals("12 31,13 32", GameJournal.stable(new com.gempukku.lotro.logic.decisions.PlayerAssignMinionsDecision(1, "Assign", List.of("12", "13"), List.of("31", "32")) {
            @Override
            public void decisionMade(String result) { }
        }, "12 31,13 32"));
    }
}
