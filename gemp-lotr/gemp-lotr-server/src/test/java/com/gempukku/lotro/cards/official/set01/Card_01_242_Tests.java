package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_242_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("summons", "1_242");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), no text
					put("hate", "1_250");       // Hate: a [sauron] event
					put("runner", "1_178");     // Goblin Runner: a [moria] minion; his text answers only his own play
					put("bowmen", "1_264");     // Orc Bowmen: a [sauron] condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDarkLordsSummonsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Dark Lord's Summons
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot a [sauron] Orc. Plays to your support area.<br><b>Shadow:</b> Remove (3) to reveal the top card of your draw deck. If it is a [sauron] card, take it into hand. Otherwise, discard it and one other card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("summons");

		assertEquals("The Dark Lord's Summons", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(summons, chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Uruk-hai and a [moria] Orc: no [sauron] Orc to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(summons));

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(summons));

		scn.ShadowPlayCard(summons);
		assertInZone(Zone.SUPPORT, summons);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingThreeRevealsTheTopCardOfHisDeckAndASauronCardIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var hate = scn.GetShadowCard("hate");
		var freepsHate = scn.GetFreepsCard("hate");
		scn.MoveCardsToHand(summons);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// His deck: a [sauron] minion on top, a [sauron] event beneath it.  The Free Peoples deck has a [sauron]
		// card on top as well, which "your draw deck" leaves alone.
		scn.MoveCardsToTopOfDeck(hate, chieftain);
		scn.MoveCardsToTopOfDeck(freepsHate);
		assertInZone(Zone.DECK, chieftain, hate, freepsHate);
		assertSame(chieftain, scn.GetShadowTopOfDeck());
		assertSame(hate, scn.GetFromTopOfShadowDeck(2));
		assertSame(freepsHate, scn.GetFreepsTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Orc Soldier, the only [sauron] Orc, is spotted.
		scn.ShadowPlayCard(summons);
		assertInZone(Zone.SUPPORT, summons);
		assertTrue(scn.AwaitingShadowPhaseActions());

		int twilight = scn.GetTwilight();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertSame(chieftain, scn.GetShadowTopOfDeck());
		assertSame(freepsHate, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(summons));

		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 3, scn.GetTwilight());
		// The top card is shown to both players: listed, and not to be chosen.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, false));
		scn.DismissRevealedCards();

		// A [sauron] minion: into his hand, and nothing else is asked.
		assertInHand(chieftain);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertSame(freepsHate, scn.GetFreepsTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Used again: a [sauron] event is taken as well.
		assertSame(hate, scn.GetShadowTopOfDeck());
		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 6, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertInHand(chieftain, hate);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardOfAnotherCultureIsDiscardedWithOneOtherCardOfHisChoiceFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(summons);
		scn.MoveCardsToHand(runner, bowmen);
		assertInZone(Zone.SUPPORT, summons);
		assertInHand(runner, bowmen);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// A [moria] minion on top of his deck.
		scn.MoveCardsToTopOfDeck(troop);
		assertInZone(Zone.DECK, troop);
		assertSame(troop, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		int shadowDeck = scn.GetShadowDeckCount();
		assertSame(troop, scn.GetShadowTopOfDeck());
		assertInHand(runner, bowmen);
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		scn.DismissRevealedCards();

		// The revealed card is not taken: it is discarded, and he chooses one other card from his hand, of
		// either culture.
		assertInDiscard(troop);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, bowmen));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(bowmen);

		assertInDiscard(troop, bowmen);
		assertInHand(runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyHandOnlyTheRevealedCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(summons);
		assertInZone(Zone.SUPPORT, summons);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToTopOfDeck(troop);
		assertInZone(Zone.DECK, troop);
		assertSame(troop, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetShadowHandCount());
		int twilight = scn.GetTwilight();
		int discard = scn.GetShadowDiscardCount();
		assertSame(troop, scn.GetShadowTopOfDeck());

		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 3, scn.GetTwilight());
		scn.DismissRevealedCards();

		// No other card to discard: the revealed one goes, and nothing more is asked.
		assertInDiscard(troop);
		assertEquals(discard + 1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThanThreeInTheTwilightPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(summons);
		assertInZone(Zone.SUPPORT, summons);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		// The pool is put at 2, then at 3, each time with his choice of Shadow actions sent afresh.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(summons));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(summons));
	}
}
