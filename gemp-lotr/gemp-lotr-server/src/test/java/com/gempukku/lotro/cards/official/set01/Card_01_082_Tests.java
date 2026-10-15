package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("risk", "1_82");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf to spot
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf

					// Three cards of three blueprints for the top of the draw deck.
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RiskaLittleLightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Risk a Little Light
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, spot Gandalf to look at the top 2 cards of your draw deck. Take one into hand and discard the other.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("risk");

		assertEquals("Risk a Little Light", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOneInThePoolLooksAtTheTopTwoTakesOneIntoHandAndDiscardsTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		// From the top: Legolas, Gimli, and Aragorn third.
		scn.MoveCardsToTopOfDeck(aragorn, gimli, legolas);
		// 1 is the most that is fewer than 2.
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, legolas, gimli, aragorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(risk));

		scn.FreepsPlayCard(risk);
		// The pool was looked at before the price was paid: with 1 + 1 in it now, the event still does its work.
		assertEquals(2, scn.GetTwilight());

		// The player is shown the top two cards and not the third, and the Shadow player nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Exactly one of those two is taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseCards(legolas);

		assertInHand(legolas);
		assertEquals(1, scn.GetFreepsHandCount()); // 1 - the event + 1 taken
		assertInDiscard(gimli, risk);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		// A look at the top two cards is no search of the deck: it is not shuffled.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// The deck is the player's own.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertTrue(scn.GetFreepsDeckCount() >= 2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(risk));

		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(risk));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(risk);

		scn.StartGame();

		// Ent Avenger is a [gandalf] companion, but he is not Gandalf.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(risk));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(risk));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCardTakenIntoHandIsNotNamedInTheGameLog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(gimli, legolas);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, legolas, gimli);
		scn.MarkGameLog();
		scn.FreepsPlayCard(risk);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCards(legolas);

		assertInHand(legolas);
		assertInDiscard(gimli);
		// The cards were looked at, not revealed, and the one taken is "one" of them and no kind of card: the
		// other player learns that a card was taken and not which. The one discarded lies in the discard pile
		// for all to see.
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(legolas));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckItIsStillPlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("avenger"), scn.GetFreepsCard("legolas"),
				scn.GetFreepsCard("gimli"), scn.GetFreepsCard("aragorn"));

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsDiscardCount());
		// An event may be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(risk));

		scn.FreepsPlayCard(risk);
		// The look still happens, and shows that there is nothing to see.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInDiscard(risk);
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
