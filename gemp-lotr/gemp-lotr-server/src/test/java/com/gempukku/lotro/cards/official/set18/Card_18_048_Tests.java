package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "18_48");
					// Soldier of Rohan: [rohan] Man, cost 2, printed resistance 5; his text needs the Ring-bearer
					// assigned to a skirmish
					put("soldier", "17_101");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, cost 2, resistance 6; a skirmish ability only
					// Aragorn, Ranger of the North: a [gondor] Man and ranger like Faramir, cost 4, resistance 6;
					// a maneuver ability used only on purpose
					put("aragorn", "1_89");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, cost 3; a skirmish ability only
					// Thrarin, Dwarven Smith: an ally with 2 vitality; a maneuver ability used only on purpose
					put("thrarin", "1_27");

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Faramir, Captain of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Ranger</b>. <b>Hunter 2</b>.<br>If Faramir is in your starting fellowship, his twilight cost is -1.<br><b>Maneuver:</b> Exert Faramir to heal another companion with resistance 5 or more.
		*/

		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", faramir.getBlueprint().getTitle());
		assertEquals("Captain of Ithilien", faramir.getBlueprint().getSubtitle());
		assertTrue(faramir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, faramir.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, faramir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, faramir.getBlueprint().getCardType());
		assertEquals(Race.MAN, faramir.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(faramir, Keyword.RANGER));
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(faramir, Keyword.HUNTER));
		assertEquals(3, faramir.getBlueprint().getTwilightCost());
		assertEquals(7, faramir.getBlueprint().getStrength());
		assertEquals(3, faramir.getBlueprint().getVitality());
		assertEquals(7, faramir.getBlueprint().getResistance());
	}

	@Test
	public void PlaysFromHandAsACompanionForThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(faramir));

		scn.FreepsPlayCard(faramir);
		// Outside the starting fellowship there is no discount: the printed 3.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheStartingFellowshipFaramirCostsTwoAndAnotherGondorManCostsHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		// The game has not been started: both players have yet to choose their starting fellowships.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, gimli, boromir));

		scn.FreepsChooseCard(faramir);
		// A starting fellowship may cost 4. Faramir has used 3 - 1 = 2 of it, so Gimli at 2 still fits: at
		// his printed 3 Faramir would have left room for 1. Boromir, another [gondor] Man, costs his own 3 and
		// does not fit: had Faramir cost 1 he would have.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));

		scn.FreepsChooseCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, faramir, gimli);
		assertInZone(Zone.DECK, boromir);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void ManeuverActionExertsFaramirToHealAnotherCompanionOfResistance5OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir, gimli, soldier, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// Each burden lowers every companion's resistance by 1.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(faramir, 1);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(soldier, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(gimli));      // 6 - 1: exactly 5
		assertEquals(9, scn.GetResistance(frodo));      // 10 - 1
		assertEquals(5, scn.GetResistance(aragorn));    // 6 - 1
		assertEquals(4, scn.GetResistance(soldier));    // 5 - 1: one short
		assertEquals(6, scn.GetResistance(faramir));    // 7 - 1
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);
		// A Dwarf, a [gondor] Man and the Ring-bearer, of resistance 5 or more. Not the Soldier of Rohan
		// (resistance 4, though printed 5), Thrarin (an ally) or Faramir himself (resistance 6).
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, thrarin, faramir));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// Vitality 3 with 2 wounds: Faramir is exhausted and cannot exert, though Gimli, Aragorn and Frodo are
		// still wounded.
		assertEquals(1, scn.GetVitality(faramir));
		assertFalse(scn.FreepsActionAvailable(faramir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
