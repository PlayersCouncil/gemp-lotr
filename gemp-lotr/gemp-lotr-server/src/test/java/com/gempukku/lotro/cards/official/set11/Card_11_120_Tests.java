package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_120");
					put("orc2", "11_120");
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] Dwarf, unbound, resistance 6; muster at a battleground or underground site only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound, resistance 6; a maneuver ability only
					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] Dwarf, unbound, resistance 7; a trigger on his win only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, resistance 5; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY (resistance 0), home site 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntrappingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Entrapping Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Assignment:</b> Assign this minion to an unbound companion who has resistance 4 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Entrapping Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityAssignsThisMinionToAnUnboundCompanionOfResistance4OrLessAndOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");

		scn.MoveCompanionsToTable(farin, aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(orc, orc2);

		// Each burden is -1 to every companion's resistance.
		assertEquals(2, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(8, scn.GetResistance(frodo));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(orc));
		scn.ShadowUseCardAction(orc);

		// Two unbound companions at 4, of two cultures and races.  Not Gimli at 5, not Sam or Frodo, who are
		// Ring-bound, not Bounder, an ally at his home site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(farin, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, sam, frodo, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(farin);

		// This copy, and not the other.
		assertTrue(scn.IsCharAssignedAgainst(farin, orc));
		assertFalse(scn.IsCharAssigned(orc2));
		assertFalse(scn.IsCharAssigned(aragorn));

		// An assigned minion cannot be assigned again (CR 4.2 "assignment phase"); the other copy still can.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(orc));
		assertTrue(scn.ShadowActionAvailable(orc2));
	}

	@Test
	public void NotOfferedUntilAnUnboundCompanionHasResistance4OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var orc = scn.GetShadowCard("orc");

		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(orc);

		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(8, scn.GetResistance(frodo));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Nobody the ability could assign it to: Gimli at 5, Ring-bound Sam and Frodo, the ally Bounder.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(orc));

		// A third burden puts Gimli at 4.
		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(gimli));
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(orc));
	}
}
