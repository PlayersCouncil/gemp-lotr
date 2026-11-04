package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("onguard", "11_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, resistance 6; his ability is not used
					// Farin, Dwarven Emissary: a Dwarf companion of another title, resistance 6; his bonus is against Orcs
					// in a skirmish only
					put("farin", "1_11");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, resistance 6; his ability is not used

					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OnGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: On Guard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf who has resistance 3 or more is about to take a wound, prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("onguard");

		assertEquals("On Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtResistance3ItPreventsTheFirstOfTwoWoundsAndTheSecondIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onguard = scn.GetFreepsCard("onguard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(onguard);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(raiders);

		// 6 less 3 burdens: exactly 3.
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(onguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, raiders);
		scn.FreepsResolveSkirmish(gimli);

		// 9 against 6: two wounds for the Raiding Party's damage +1, each in a window of its own.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(onguard));
		scn.FreepsChooseOptionalTrigger(onguard);

		assertInDiscard(onguard);
		// The first wound prevented; the second, with no copy left, taken.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtResistance2ItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onguard = scn.GetFreepsCard("onguard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(onguard);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(raiders);

		assertEquals(2, scn.GetResistance(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(onguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, raiders);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(onguard));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInHand(onguard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundOnACompanionWhoIsNoDwarfAtResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onguard = scn.GetFreepsCard("onguard");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(onguard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);

		// Both at exactly 3; only Aragorn, who is no Dwarf, is about to take a wound.
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(onguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// 13 against 8: one wound.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(onguard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(onguard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAnArcheryWoundOnAnotherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onguard = scn.GetFreepsCard("onguard");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveCardsToHand(onguard);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		assertEquals(6, scn.GetResistance(farin));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertInHand(onguard);
		scn.SkipToArcheryWounds();

		// The Marksman's archery wound, placed by the Free Peoples player on Farin: no skirmish is needed.
		scn.FreepsChooseCard(farin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(onguard));
		scn.FreepsChooseOptionalTrigger(onguard);

		assertInDiscard(onguard);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
