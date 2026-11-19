package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foresight", "4_45");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, a Maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a Maneuver ability only
					// Here Is Good Rock: unique [dwarven] tale condition in the support area; a Skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] condition in the support area; a Skirmish ability only
					put("armory", "4_47");
					// I Would Have Gone With You to the End: [gondor] condition in the support area, which stacks its
					// owner's skirmish events; with no Aragorn in play it does nothing (none is laid where it is)
					put("vow", "101_23");
					// Cards to stack, none with text that acts from a stack
					put("guard2", "1_7");       // Dwarf Guard: a Free Peoples companion
					put("axe", "1_9");          // Dwarven Axe: a Free Peoples possession
					put("strike", "1_3");       // Axe Strike: a Free Peoples skirmish event
					put("strike2", "1_3");
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenForesightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dwarven Foresight
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot a Dwarf to take a Free Peoples card stacked on a [dwarven] condition into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foresight");

		assertEquals("Dwarven Foresight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingADwarfCompanionTakesAFreePeoplesCardStackedOnADwarvenConditionIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foresight = scn.GetFreepsCard("foresight");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var vow = scn.GetFreepsCard("vow");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(foresight);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(rock, armory, vow);
		// Free Peoples cards of three types on two [dwarven] conditions, and a Shadow card beside them; a Free
		// Peoples card on a [gondor] condition.
		scn.StackCardsOn(rock, guard2, troop);
		scn.StackCardsOn(armory, axe, strike);
		scn.StackCardsOn(vow, strike2);

		// The other player's own [dwarven] condition with a Free Peoples card stacked: his Free Peoples cards are
		// not active on this turn.
		var theirArmory = scn.GetShadowCard("armory");
		var theirAxe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(theirArmory);
		scn.StackCardsOn(theirArmory, theirAxe);

		scn.StartGame();

		assertInZone(Zone.STACKED, guard2, troop, axe, strike, strike2, theirAxe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(foresight));
		scn.FreepsPlayCard(foresight);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, axe, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, strike2, theirAxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(foresight);
		assertEquals(2, scn.GetStackedCards(rock).size());
		assertEquals(1, scn.GetStackedCards(armory).size());
		assertTrue(scn.GetStackedCards(armory).contains(strike));
		assertEquals(1, scn.GetStackedCards(vow).size());
		assertEquals(1, scn.GetStackedCards(theirArmory).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foresight = scn.GetFreepsCard("foresight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var rock = scn.GetFreepsCard("rock");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToHand(foresight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, guard2);

		scn.StartGame();

		assertInZone(Zone.STACKED, guard2);
		// The ally is the only Dwarf.
		assertTrue(scn.FreepsPlayAvailable(foresight));
		scn.FreepsPlayCard(foresight);

		// The one card there is taken with nothing asked.
		assertInHand(guard2);
		assertEquals(0, scn.GetStackedCards(rock).size());
		assertInDiscard(foresight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyAShadowCardStackedOnADwarvenConditionItIsPlayedAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foresight = scn.GetFreepsCard("foresight");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		var vow = scn.GetFreepsCard("vow");
		var troop = scn.GetFreepsCard("troop");
		var strike2 = scn.GetFreepsCard("strike2");
		scn.MoveCardsToHand(foresight);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(rock, vow);
		scn.StackCardsOn(rock, troop);
		scn.StackCardsOn(vow, strike2);

		scn.StartGame();

		assertInZone(Zone.STACKED, troop, strike2);
		assertEquals(1, scn.GetFreepsHandCount());
		// What the event takes is its target, not a requirement: it is played, and nothing comes into hand.
		assertTrue(scn.FreepsPlayAvailable(foresight));
		scn.FreepsPlayCard(foresight);

		assertInDiscard(foresight);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.STACKED, troop, strike2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foresight = scn.GetFreepsCard("foresight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToHand(foresight);
		// A companion who is no Dwarf, two [dwarven] cards that are no Dwarves, and a Dwarf who is stacked and so
		// not in play.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rock, armory);
		scn.StackCardsOn(rock, guard2);
		scn.StackCardsOn(armory, axe);

		scn.StartGame();

		assertInZone(Zone.STACKED, guard2, axe);
		assertInHand(foresight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(foresight));
	}
}
