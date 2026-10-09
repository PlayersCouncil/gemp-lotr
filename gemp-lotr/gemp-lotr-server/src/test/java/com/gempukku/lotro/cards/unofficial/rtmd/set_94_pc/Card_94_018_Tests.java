package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_018_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: [gondor] Man, resistance 6; a maneuver ability used only on purpose
					put("gimli", "1_13");   // Gimli, Son of Gloin: [dwarven] Dwarf, resistance 6; a skirmish ability used only on purpose
					put("risk", "7_4");     // Calculated Risk: Fellowship event, cost 2, "Spot a Dwarf and add a threat to draw 3 cards"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_18", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: [gondor] Man, resistance 6; a maneuver ability used only on purpose
					put("gimli", "1_13");   // Gimli, Son of Gloin: [dwarven] Dwarf, resistance 6; a skirmish ability used only on purpose
					put("risk", "7_4");     // Calculated Risk: Fellowship event, cost 2, "Spot a Dwarf and add a threat to draw 3 cards"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_18"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_18
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: Each companion is resistance -1 for each threat.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_18", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ResistanceDropsByOnePerThreatAndRecoversAsTheyAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var ringBearer = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		// No threat and no burden: the printed numbers, Aragorn's 6 and Frodo's 10.
		int aragornBase = scn.GetResistance(aragorn);
		int rbBase = scn.GetResistance(ringBearer);
		assertEquals(0, scn.GetThreats());
		assertEquals(6, aragornBase);
		assertEquals(10, rbBase);

		scn.AddThreats(2);
		assertEquals(aragornBase - 2, scn.GetResistance(aragorn));
		assertEquals(rbBase - 2, scn.GetResistance(ringBearer));

		scn.RemoveThreats(1);
		assertEquals(aragornBase - 1, scn.GetResistance(aragorn));
		assertEquals(rbBase - 1, scn.GetResistance(ringBearer));

		scn.RemoveThreats(1);
		assertEquals(aragornBase, scn.GetResistance(aragorn));
		assertEquals(rbBase, scn.GetResistance(ringBearer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AppliesRegardlessOfWhichSideOwnsTheModSinceThereIsNoYour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var ringBearer = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		int aragornBase = scn.GetResistance(aragorn);
		int rbBase = scn.GetResistance(ringBearer);
		assertEquals(0, scn.GetThreats());
		assertEquals(6, aragornBase);
		assertEquals(10, rbBase);

		// No "your" - a Shadow-owned copy still drops resistance on every companion, including
		// the Free Peoples player's own.
		scn.AddThreats(2);
		assertEquals(aragornBase - 2, scn.GetResistance(aragorn));
		assertEquals(rbBase - 2, scn.GetResistance(ringBearer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AThreatAddedByAnEventLowersEachCompanionsResistanceByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var risk = scn.GetFreepsCard("risk");
		var ringBearer = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(risk);
		scn.StartGame();

		// Three companions of three cultures, one of them the Ring-bearer, at their printed numbers.
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(10, scn.GetResistance(ringBearer));
		assertTrue(scn.FreepsPlayAvailable(risk));

		// Calculated Risk adds one threat as its cost.
		scn.FreepsPlayCard(risk);

		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(9, scn.GetResistance(ringBearer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
