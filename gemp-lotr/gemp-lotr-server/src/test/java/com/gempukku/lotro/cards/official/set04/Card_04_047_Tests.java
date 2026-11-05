package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_047_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "4_47");
					put("armory2", "4_47");
					put("gimli", "1_12");       // Gimli, Dwarf of Erebor: Dwarf companion, strength 6, vitality 3; damage +1 and a fellowship ability
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who could exert and is no Dwarf
					// Cards for the hand.
					put("guard2", "1_7");       // Dwarf Guard: a [dwarven] companion
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("axestrike", "1_3");    // Axe Strike: a [dwarven] skirmish event, never played
					put("runner", "1_178");     // Goblin Runner: a Shadow minion, in the Free Peoples player's hand
					// Strength 9 and damage +1 against Gimli's 6: he loses, takes two wounds, and is not overwhelmed.
					put("raiders", "1_158");    // Uruk-hai Raiding Party
					// Fierce; 7 in the first skirmish and 7 + 3 + hunter 1 in the fierce one: Gimli loses both, overwhelmed in neither.
					put("charging", "15_156");  // Charging Uruk
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromtheArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: From the Armory
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 		<b>Skirmish:</b> Exert a Dwarf and stack a Free Peoples card from hand here to prevent all wounds to that Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory");

		assertEquals("From the Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingADwarfAndStackingAFreePeoplesCardPreventsAllWoundsToThatDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var armory2 = scn.GetFreepsCard("armory2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword = scn.GetFreepsCard("sword");
		var axestrike = scn.GetFreepsCard("axestrike");
		var runner = scn.GetFreepsCard("runner");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(armory, armory2);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(guard2, sword, axestrike, runner);

		scn.StartGame();
		assertInHand(guard2, sword, axestrike, runner);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Any Dwarf may exert, in the skirmish or not, companion or ally; not Aragorn, who could exert and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// A Free Peoples card of any type; not the Shadow card in the same hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, sword, axestrike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		// Stacked on the copy used, and not on the other.
		assertEquals(1, scn.GetStackedCards(armory).size());
		assertTrue(scn.GetStackedCards(armory).contains(sword));
		assertEquals(0, scn.GetStackedCards(armory2).size());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// 6 against 9: Gimli loses, and neither of the two wounds (damage +1) reaches him.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheDwarfWhoExertedIsProtectedAndTheOneInTheSkirmishStillTakesTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertInHand(sword);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);

		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(armory);
		// The Guard, who is not in the skirmish, exerts; the sword is the one card in hand.
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.GetStackedCards(armory).contains(sword));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThePreventionEndsWithTheSkirmishPhaseAndTheFierceSkirmishWoundsTheDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var charging = scn.GetShadowCard("charging");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertInHand(sword);
		scn.MoveMinionsToTable(charging);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, charging);

		scn.FreepsUseCardAction(armory);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// 6 against 7: Gimli loses the first skirmish and takes no wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The fierce skirmish is another skirmish phase: 6 against 11, and the wound reaches him.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, charging);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAShadowCardInHandAndOfferedOnceAFreePeoplesCardIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		assertInHand(runner);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armory));

		scn.MoveCardsToHand(sword);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(armory));
	}

	@Test
	public void NotOfferedWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(sword);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		assertInHand(sword);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);

		// Vitality 3 with 2 wounds: exhausted. Aragorn could exert, and is no Dwarf.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armory));
	}
}
