package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lad", "4_43");
					// Here Is Good Rock: unique [dwarven] tale condition in the support area; a Skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] condition in the support area, not unique; a Skirmish ability only
					put("armory", "4_47");
					// Courtesy of My Hall: unique [dwarven] condition in the support area; its play stacks any card of its
					// owner's discard pile, so a Shadow card can lie on it alone. Its Fellowship ability is not used here.
					put("courtesy", "4_44");
					// Lord of Moria: [dwarven] condition in the support area, "Each Dwarf is damage +1"; nothing stacks on it
					put("lord", "1_21");
					// I Would Have Gone With You to the End: [gondor] condition in the support area, which stacks its
					// owner's skirmish events; with no Aragorn in play it does nothing
					put("vow", "101_23");
					// Cards to stack, none with text that acts from a stack
					put("toby", "1_305");       // Old Toby: a [shire] possession
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
					put("strike", "1_3");       // Axe Strike: a [dwarven] skirmish event
					// Draw deck fillers with no text
					put("top1", "1_177");
					put("top2", "1_177");
					put("top3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComeHereLadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Come Here Lad
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Stack the top 2 cards from your draw deck on a [dwarven] condition that has a card already stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lad");

		assertEquals("Come Here Lad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StacksTheTopTwoCardsOfYourDrawDeckOnADwarvenConditionThatHasACardStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lad = scn.GetFreepsCard("lad");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var courtesy = scn.GetFreepsCard("courtesy");
		var lord = scn.GetFreepsCard("lord");
		var vow = scn.GetFreepsCard("vow");
		var toby = scn.GetFreepsCard("toby");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");
		var strike = scn.GetFreepsCard("strike");
		var top1 = scn.GetFreepsCard("top1");
		var top2 = scn.GetFreepsCard("top2");
		var top3 = scn.GetFreepsCard("top3");
		scn.MoveCardsToHand(lad);
		scn.MoveCardsToSupportArea(rock, armory, courtesy, lord, vow);
		// Three [dwarven] conditions with a card stacked, differing in title, uniqueness and what lies on them (a
		// Free Peoples possession; a second one; a Shadow card alone); a [dwarven] condition with nothing stacked;
		// a condition of another culture with a card stacked.
		scn.StackCardsOn(rock, toby);
		scn.StackCardsOn(armory, sword);
		scn.StackCardsOn(courtesy, troop);
		scn.StackCardsOn(vow, strike);

		// The other player's own [dwarven] condition with a card stacked: his Free Peoples cards are not active
		// on this turn.
		var theirArmory = scn.GetShadowCard("armory");
		var theirToby = scn.GetShadowCard("toby");
		scn.MoveCardsToSupportArea(theirArmory);
		scn.StackCardsOn(theirArmory, theirToby);

		scn.StartGame();
		// top1 on top, then top2, then top3.
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, top1, top2, top3);
		assertInZone(Zone.STACKED, toby, sword, troop, strike, theirToby);
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertEquals(0, scn.GetStackedCards(lord).size());

		assertTrue(scn.FreepsPlayAvailable(lad));
		scn.FreepsPlayCard(lad);

		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, armory, courtesy));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lord, vow, theirArmory));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rock);

		// The top 2 of your own draw deck are on the Rock beside the card already there; the third is now on top.
		assertEquals(3, scn.GetStackedCards(rock).size());
		assertTrue(scn.GetStackedCards(rock).containsAll(java.util.List.of(toby, top1, top2)));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetStackedCards(armory).size());
		assertEquals(1, scn.GetStackedCards(courtesy).size());
		assertEquals(0, scn.GetStackedCards(lord).size());
		assertEquals(1, scn.GetStackedCards(vow).size());
		assertEquals(1, scn.GetStackedCards(theirArmory).size());
		assertInDiscard(lad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenConditionThatHasACardStackedItIsPlayedAndStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lad = scn.GetFreepsCard("lad");
		var lord = scn.GetFreepsCard("lord");
		var rock = scn.GetFreepsCard("rock");
		var vow = scn.GetFreepsCard("vow");
		var strike = scn.GetFreepsCard("strike");
		var top1 = scn.GetFreepsCard("top1");
		scn.MoveCardsToHand(lad);
		// Two [dwarven] conditions with nothing on them, and a [gondor] one with a card stacked.
		scn.MoveCardsToSupportArea(lord, rock, vow);
		scn.StackCardsOn(vow, strike);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top1);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();

		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, top1);
		assertInZone(Zone.STACKED, strike);

		// What the event stacks on is its target, not a requirement: it is played, and nothing is stacked.
		assertTrue(scn.FreepsPlayAvailable(lad));
		scn.FreepsPlayCard(lad);

		assertInDiscard(lad);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetStackedCards(lord).size());
		assertEquals(0, scn.GetStackedCards(rock).size());
		assertEquals(1, scn.GetStackedCards(vow).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lad = scn.GetFreepsCard("lad");
		var rock = scn.GetFreepsCard("rock");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(lad);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, toby);
		var minion = scn.GetShadowCard("troop");

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(lad));

		scn.MoveMinionsToTable(minion);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.STACKED, toby);
		assertInHand(lad);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lad));
	}
}
