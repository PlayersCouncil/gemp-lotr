package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "11_168");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer; his texts are not used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F, so he may be assigned at site 2; a skirmish ability used only on purpose
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not set off by a cheat
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
					put("runner6", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Merry, Loyal Companion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Game Text: For each other unbound companion assigned to a skirmish, Merry is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Loyal Companion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(9, card.getBlueprint().getResistance());
	}

	@Test
	public void IsStrengthPlus2ForEachOtherUnboundCompanionAssignedAndCountsEachOnlyUntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var runner5 = scn.GetShadowCard("runner5");
		var runner6 = scn.GetShadowCard("runner6");
		scn.MoveCompanionsToTable(merry, sam, aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, runner4, runner5, runner6);
		// The fellowship moves to site 2, Bounder's home, where he may be assigned.
		scn.SkipToAssignments();

		// Nobody assigned yet: printed strength.
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2},
				new PhysicalCardImpl[]{merry, runner3},
				new PhysicalCardImpl[]{frodo, runner4},
				new PhysicalCardImpl[]{sam, runner5},
				new PhysicalCardImpl[]{bounder, runner6});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		// Aragorn ([gondor] Man) and Gimli ([dwarven] Dwarf) count: 3 + 2 + 2.  Merry himself, the Ring-bound
		// Frodo and Sam, the ally Bounder and the minions, all assigned as well, do not.
		assertEquals(7, scn.GetStrength(merry));

		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn is still assigned while his own skirmish is fought.
		assertEquals(7, scn.GetStrength(merry));
		scn.PassSkirmishActions();

		// Once his skirmish is over Aragorn is no longer assigned: Gimli alone counts.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(5, scn.GetStrength(merry));
		scn.PassSkirmishActions();

		// With Gimli's skirmish over too, none counts in Merry's own.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void CountsAnAssignedCompanionWhileMerryIsNotAssignedAndNotOneWhoIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();

		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsAssignToMinions(aragorn, runner1);

		// Aragorn is assigned and counts; Gimli, an unbound companion in play but not assigned, does not.  Merry's
		// own skirmish is no part of the text.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(5, scn.GetStrength(merry));
	}
}
