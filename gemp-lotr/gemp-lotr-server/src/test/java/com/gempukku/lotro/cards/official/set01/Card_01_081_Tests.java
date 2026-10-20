package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("questions", "1_81");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: the Gandalf to spot
					put("barliman", "1_70");      // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] ally who is not Gandalf; a fellowship ability used only on purpose

					// Five cards of five blueprints for the top of the draw deck.
					put("legolas", "1_50");       // Legolas, Greenleaf
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("aragorn", "1_89");       // Aragorn, Ranger of the North
					put("bow", "1_41");           // Elven Bow
					put("pathfinder", "1_110");   // Pathfinder
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuestionsThatNeedAnsweringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Questions That Need Answering
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> If the twilight pool has fewer than 3 twilight tokens, spot Gandalf to look at the top 4 cards of your draw deck. Take 2 of those cards into hand and discard the rest.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("questions");

		assertEquals("Questions That Need Answering", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoInThePoolLooksAtTheTopFourTakesTwoIntoHandAndDiscardsTheOtherTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(questions);

		scn.StartGame();
		// From the top: Legolas, Gimli, Aragorn, the Elven Bow, and Pathfinder fifth.
		scn.MoveCardsToTopOfDeck(pathfinder, bow, aragorn, gimli, legolas);
		// 2 is the most that is fewer than 3.
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(questions));

		scn.FreepsPlayCard(questions);
		// The pool was looked at before the price was paid: with 2 + 3 in it now, the event still does its work.
		assertEquals(5, scn.GetTwilight());

		// The player is shown the top four cards and not the fifth, and the Shadow player nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, false));
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Exactly 2 of those four are taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli, aragorn, bow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pathfinder));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseCards(legolas, aragorn);

		assertInHand(legolas, aragorn);
		assertEquals(2, scn.GetFreepsHandCount()); // 1 - the event + 2 taken
		assertInDiscard(gimli, bow, questions);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertSame(pathfinder, scn.GetFreepsTopOfDeck());

		// The deck is the player's own.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithThreeInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(questions);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertTrue(scn.GetFreepsDeckCount() >= 4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(questions));

		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(questions));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(questions);

		scn.StartGame();

		// Barliman is a [gandalf] card, but he is not Gandalf.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(questions));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(questions));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCardsTakenIntoHandAreNotNamedInTheGameLog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(questions);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(bow, aragorn, gimli, legolas);
		scn.FreepsRefreshDecision();

		scn.MarkGameLog();
		scn.FreepsPlayCard(questions);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCards(legolas, aragorn);

		assertInHand(legolas, aragorn);
		assertInDiscard(gimli, bow);
		// The cards were looked at, not revealed, and the two taken are "2 of those cards" and no kind of card:
		// the other player learns that two cards were taken and not which. The two discarded lie in the
		// discard pile for all to see.
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(legolas));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(aragorn));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gimli, bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeCardsInTheDrawDeckAllThreeAreLookedAtTwoTakenAndOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(questions);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("barliman"), scn.GetFreepsCard("bow"), scn.GetFreepsCard("pathfinder"));

		scn.StartGame();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, legolas, gimli, aragorn);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(questions));

		scn.FreepsPlayCard(questions);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(gimli, aragorn);

		assertInHand(gimli, aragorn);
		assertInDiscard(legolas, questions);
		assertEquals(5, scn.GetFreepsDiscardCount()); // 3 put there + Legolas + the event
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckItIsStillPlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var questions = scn.GetFreepsCard("questions");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(questions);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("barliman"), scn.GetFreepsCard("bow"), scn.GetFreepsCard("pathfinder"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("aragorn"));

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetFreepsDiscardCount());
		// An event may be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(questions));

		scn.FreepsPlayCard(questions);
		assertEquals(3, scn.GetTwilight());
		// The look still happens, and shows that there is nothing to see.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInDiscard(questions);
		assertEquals(7, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
