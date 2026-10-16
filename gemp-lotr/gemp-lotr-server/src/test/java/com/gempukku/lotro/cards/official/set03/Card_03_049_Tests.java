package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("abandon", "3_49");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					put("shaman", "3_59");      // Isengard Shaman: [isengard] Orc; a response to a wound on an [isengard] Orc
					put("storm", "1_130");      // No Ordinary Storm: [isengard] condition; answers only a weather condition played
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its play trigger is never set off

					// Cards held in the Shadow player's hand: a companion, a possession, an event and two minions.
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("bow", "1_41");         // Elven Bow
					put("pathfinder", "1_110"); // Pathfinder
					put("troop", "1_177");      // Goblin Patrol Troop
					put("marksman", "1_176");   // Goblin Marksman

					// Cards put on top of the Shadow player's draw deck, to be read by name once drawn.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("savage2", "1_151");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AbandoningReasonforMadnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Abandoning Reason for Madness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot an [isengard] minion to discard up to 3 cards from hand and draw an equal number of cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("abandon");

		assertEquals("Abandoning Reason for Madness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnUrukHaiDiscardsThreeChosenCardsFromHandAndDrawsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var abandon = scn.GetShadowCard("abandon");
		var savage = scn.GetShadowCard("savage");
		var legolas = scn.GetShadowCard("legolas");
		var bow = scn.GetShadowCard("bow");
		var pathfinder = scn.GetShadowCard("pathfinder");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetShadowCard("aragorn");
		var gimli = scn.GetShadowCard("gimli");
		var savage2 = scn.GetShadowCard("savage2");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(abandon, legolas, bow, pathfinder, troop, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// The Goblin Runner is the top card of the deck, then the second Uruk Savage, Gimli and Aragorn.
		scn.MoveCardsToTopOfDeck(aragorn, gimli, savage2, runner2);
		assertInHand(abandon, legolas, bow, pathfinder, troop, marksman);
		assertInZone(Zone.DECK, aragorn, gimli, savage2, runner2);
		scn.SetTwilight(3);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertSame(runner2, scn.GetShadowTopOfDeck());
		assertTrue(scn.ShadowPlayAvailable(abandon));

		scn.ShadowPlayCard(abandon);
		// Any card in hand may go, of either side and of any type: none of them need, and no more than 3 may.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, bow, pathfinder, troop, marksman));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(legolas, pathfinder, troop);

		// 3 discarded and 3 drawn: the three cards from the top of the deck, and not the fourth.
		assertInDiscard(legolas, pathfinder, troop, abandon);
		assertEquals(4, scn.GetShadowDiscardCount()); // 3 discarded + the event
		assertInHand(bow, marksman, runner2, savage2, gimli);
		assertEquals(5, scn.GetShadowHandCount()); // 6 - the event - 3 discarded + 3 drawn
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertSame(aragorn, scn.GetShadowTopOfDeck());

		// The hand and the deck are the player's own.
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAnIsengardOrcDiscardsOneChosenCardAndDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var abandon = scn.GetShadowCard("abandon");
		var shaman = scn.GetShadowCard("shaman");
		var legolas = scn.GetShadowCard("legolas");
		var bow = scn.GetShadowCard("bow");
		var troop = scn.GetShadowCard("troop");
		var savage2 = scn.GetShadowCard("savage2");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(abandon, legolas, bow, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.MoveCardsToTopOfDeck(savage2, runner2);
		assertInHand(abandon, legolas, bow, troop);
		assertInZone(Zone.DECK, savage2, runner2);
		scn.SetTwilight(3);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(4, scn.GetShadowHandCount());
		assertSame(runner2, scn.GetShadowTopOfDeck());
		assertTrue(scn.ShadowPlayAvailable(abandon));

		scn.ShadowPlayCard(abandon);
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(bow);

		assertInDiscard(bow, abandon);
		assertEquals(2, scn.GetShadowDiscardCount());
		// One card drawn for the one discarded: the top card, and the next stays on the deck.
		assertInHand(legolas, troop, runner2);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertSame(savage2, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingNoCardDiscardsAndDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var abandon = scn.GetShadowCard("abandon");
		var savage = scn.GetShadowCard("savage");
		var legolas = scn.GetShadowCard("legolas");
		var bow = scn.GetShadowCard("bow");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(abandon, legolas, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToTopOfDeck(runner2);
		assertInHand(abandon, legolas, bow);
		scn.SetTwilight(3);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertSame(runner2, scn.GetShadowTopOfDeck());

		scn.ShadowPlayCard(abandon);
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, bow));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// "Up to 3" may be none, and then none is drawn.
		assertInHand(legolas, bow);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(abandon);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertSame(runner2, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherCardInHandItIsPlayedAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var abandon = scn.GetShadowCard("abandon");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(abandon);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(3);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertTrue(deck > 0);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(abandon));

		scn.ShadowPlayCard(abandon);
		assertInDiscard(abandon);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var abandon = scn.GetShadowCard("abandon");
		var storm = scn.GetShadowCard("storm");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var legolas = scn.GetShadowCard("legolas");
		scn.MoveCardsToHand(abandon, legolas);
		scn.MoveCardsToSupportArea(storm);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// An [isengard] card that is no minion, and a minion of another culture; the pool can pay for the event.
		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(abandon));

		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(abandon));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
