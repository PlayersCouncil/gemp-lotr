package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_048_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "68_48");
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
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Ranger. Hunter 2.</b>
		 * 	If Faramir is in your starting fellowship, his twilight cost is -1.
		 * 	<b>Maneuver:</b> Spot another wounded companion with resistance 5 or more and exert Faramir to heal
		 * 	that companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Captain of Ithilien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
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
	public void ManeuverActionExertsFaramirToHealAnotherWoundedCompanionOfResistance5OrMore() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.AddWoundsToChar(thrarin, 1);
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(gimli));      // 6 - 1: exactly 5
		assertEquals(9, scn.GetResistance(frodo));      // 10 - 1
		assertEquals(4, scn.GetResistance(soldier));    // 5 - 1: one short
		assertEquals(5, scn.GetResistance(aragorn));    // 6 - 1, and unwounded
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);
		// A Dwarf and the Ring-bearer, both wounded and of resistance 5 or more. Not the Soldier of Rohan
		// (wounded, resistance 4), Aragorn (resistance 5, not wounded), Thrarin (wounded, an ally) or Faramir
		// himself (wounded, resistance 6).
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, aragorn, thrarin, faramir));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// Vitality 3 with 2 wounds: Faramir is exhausted and cannot exert, though Gimli and Frodo are still
		// wounded.
		assertEquals(1, scn.GetVitality(faramir));
		assertFalse(scn.FreepsActionAvailable(faramir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedUntilAnotherWoundedCompanionHasResistance5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(faramir, soldier, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(faramir, 1);
		scn.AddWoundsToChar(soldier, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// The wounded are Faramir himself, an ally, and a companion whose printed resistance of 5 the burden
		// has brought to 4. Aragorn and Frodo have the resistance and no wound.
		assertEquals(4, scn.GetResistance(soldier));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertFalse(scn.FreepsActionAvailable(faramir));

		scn.RemoveBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetResistance(soldier));
		assertTrue(scn.FreepsActionAvailable(faramir));

		// The Soldier of Rohan is the only companion who can be spotted, so he is not asked for.
		scn.FreepsUseCardAction(faramir);
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
