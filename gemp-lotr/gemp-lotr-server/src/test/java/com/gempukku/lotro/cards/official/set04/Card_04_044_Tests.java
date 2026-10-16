package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("courtesy", "4_44");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a Skirmish ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; a Maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a Maneuver ability only
					// Here Is Good Rock: unique [dwarven] tale condition in the support area; a Skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] condition in the support area, cost 0; a Skirmish ability only
					put("armory", "4_47");
					// Cards to stack, none with text that acts from a stack or a discard pile
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples companion
					put("axe", "1_9");          // Dwarven Axe: a Free Peoples possession
					put("toby", "1_305");       // Old Toby: a Free Peoples possession
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CourtesyofMyHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Courtesy of My Hall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this condition, stack a card from your discard pile here.
		 * <b>Fellowship:</b> Discard a Free Peoples card stacked here to heal a Dwarf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("courtesy");

		assertEquals("Courtesy of My Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedStacksACardOfYourChoiceFromYourDiscardPileHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var armory = scn.GetFreepsCard("armory");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(courtesy, armory);
		// A Free Peoples companion, a Free Peoples possession and a Shadow minion in the discard pile.
		scn.MoveCardsToDiscard(guard, axe, troop);

		scn.StartGame();

		assertInDiscard(guard, axe, troop);
		assertEquals(3, scn.GetFreepsDiscardCount());
		scn.FreepsPlayCard(courtesy);

		assertInZone(Zone.SUPPORT, courtesy);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, axe, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		// "A card": a Shadow card of your own is one.
		scn.FreepsChooseCards(troop);

		assertInZone(Zone.STACKED, troop);
		assertEquals(1, scn.GetStackedCards(courtesy).size());
		assertTrue(scn.GetStackedCards(courtesy).contains(troop));
		assertInDiscard(guard, axe);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [dwarven] condition played with this one in play: nothing is asked, and nothing more is stacked.
		scn.FreepsPlayCard(armory);
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(1, scn.GetStackedCards(courtesy).size());
		assertEquals(0, scn.GetStackedCards(armory).size());
		assertInDiscard(guard, axe);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityDiscardsAFreePeoplesCardStackedHereToHealADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var rock = scn.GetFreepsCard("rock");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(courtesy, rock);
		// Two Free Peoples cards of different types here, and one on another [dwarven] condition.
		scn.StackCardsOn(courtesy, guard, axe);
		scn.StackCardsOn(rock, toby);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertInZone(Zone.STACKED, guard, axe, toby);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsActionAvailable(courtesy));
		scn.FreepsUseCardAction(courtesy);

		// The cost: a Free Peoples card stacked here, and not one stacked elsewhere.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(toby));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);
		assertInDiscard(axe);
		assertEquals(1, scn.GetStackedCards(courtesy).size());
		assertTrue(scn.GetStackedCards(courtesy).contains(guard));
		assertTrue(scn.GetStackedCards(rock).contains(toby));

		// The heal: a Dwarf, companion or ally, and not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowCardStackedHereIsNotOfferedToPayTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(courtesy);
		// A Shadow card of your own can lie here: Courtesy of My Hall stacks any card of your discard pile.
		scn.StackCardsOn(courtesy, guard, axe, troop);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		assertInZone(Zone.STACKED, guard, axe, troop);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(courtesy);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(guard);
		assertInDiscard(guard);
		assertInZone(Zone.STACKED, axe, troop);
		// Gimli, the only Dwarf wounded, is healed with nothing asked.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyAShadowCardStackedHereTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var rock = scn.GetFreepsCard("rock");
		var gimli = scn.GetFreepsCard("gimli");
		var toby = scn.GetFreepsCard("toby");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(courtesy, rock);
		// A Shadow card here, and a Free Peoples card on another [dwarven] condition.
		scn.StackCardsOn(courtesy, troop);
		scn.StackCardsOn(rock, toby);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		assertInZone(Zone.STACKED, troop, toby);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(courtesy));
	}

	@Test
	public void WithNoDwarfWoundedTheCostIsPaidAndNobodyIsHealedAndWithNothingLeftHereItIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var rock = scn.GetFreepsCard("rock");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(courtesy, rock);
		scn.StackCardsOn(courtesy, axe);
		scn.StackCardsOn(rock, toby);
		// Only the companion who is no Dwarf is wounded.
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertInZone(Zone.STACKED, axe, toby);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The discard is a cost, so the ability is offered with no Dwarf to heal.
		assertTrue(scn.FreepsActionAvailable(courtesy));
		scn.FreepsUseCardAction(courtesy);

		assertInDiscard(axe);
		assertEquals(0, scn.GetStackedCards(courtesy).size());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Nothing is stacked here now, and the card on the Rock cannot pay: the ability is not offered again.
		assertInZone(Zone.STACKED, toby);
		assertFalse(scn.FreepsActionAvailable(courtesy));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var courtesy = scn.GetFreepsCard("courtesy");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(courtesy);
		scn.StackCardsOn(courtesy, axe);
		scn.AddWoundsToChar(gimli, 1);
		var minion = scn.GetShadowCard("troop");

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(courtesy));

		scn.MoveMinionsToTable(minion);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.STACKED, axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(courtesy));
	}
}
