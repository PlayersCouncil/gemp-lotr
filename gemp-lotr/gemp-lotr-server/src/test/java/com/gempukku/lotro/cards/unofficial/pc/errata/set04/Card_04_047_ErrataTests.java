package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_047_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "54_47");
					put("armory2", "54_47");
					put("gimli", "1_12");       // Gimli, Dwarf of Erebor: Dwarf companion, strength 6, vitality 3; damage +1 and a fellowship ability
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin: Dwarf ally, a maneuver ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who could exert and is no Dwarf
					// Cards for the hand.
					put("guard2", "1_7");       // Dwarf Guard: a [dwarven] companion
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("runner", "1_178");     // Goblin Runner: a Shadow minion, in the Free Peoples player's hand
					// Strength 7 against Gimli's 6: Gimli loses without being overwhelmed.
					// "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("slayer", "3_93");      // Morgul Slayer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromTheArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: From the Armory
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Skirmish: Exert a Dwarf and stack a Free Peoples card from hand here to make that Dwarf unable to take wounds.
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
	public void PlaysFromHandForNothingToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		scn.MoveCardsToHand(armory);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(armory));
		scn.FreepsPlayCard(armory);

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingADwarfAndStackingAFreePeoplesCardMakesThatDwarfUnableToTakeWoundsUntilTheEndOfTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var armory2 = scn.GetFreepsCard("armory2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard2 = scn.GetFreepsCard("guard2");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetFreepsCard("runner");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(armory, armory2);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(guard2, sword, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, slayer);

		int hand = scn.GetFreepsHandCount();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Any Dwarf may exert, in the skirmish or not, companion or ally; not Aragorn, who could exert and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		// Stacked on the copy used, and not on the other.
		assertEquals(1, scn.GetStackedCards(armory).size());
		assertTrue(scn.GetStackedCards(armory).contains(sword));
		assertEquals(0, scn.GetStackedCards(armory2).size());
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// Strength 6 against 7: Gimli loses, and takes no wound for it.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// In the regroup phase he can be wounded again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli));
		scn.ShadowChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheDwarfWhoExertedIsProtectedAndTheOneInTheSkirmishStillTakesTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, slayer);

		scn.FreepsUseCardAction(armory);
		// The guard, who is not in the skirmish, exerts; the sword is the one card in hand.
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.GetStackedCards(armory).contains(sword));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedInTheAssignmentPhaseAndOfferedInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armory));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, slayer);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(armory));
	}

	@Test
	public void NotOfferedWithOnlyAShadowCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetFreepsCard("runner");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, slayer);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(armory));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetFreepsCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(sword);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, slayer);

		// Vitality 3 with 2 wounds: exhausted.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(armory));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
