package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: GetAffectingCard, the lines of a card info's "Affecting card" list, named for the
 * modifier's source card or "System" for a rule's, in alphabetical order (engine pass 59).  What the lines of a borne
 * card's printed numbers say is mechanics/inplay/AffectingCardListTests.
 */
public class AffectingCardReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule (CR 4.2 "starting fellowship")
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site 4, no text at all
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARulesModifierReadsAsSystemAndACardsAsItsTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The rules make Sam Ring-bound, and the Troop roaming at site 1; nothing else touches either.
		assertEquals(List.of("System: Ring-Bound"), scn.GetAffectingCard(sam));
		assertEquals(List.of("System: Roaming"), scn.GetAffectingCard(troop));
		// The Ruling Ring's printed strength +1 is named by its title, which is "The One Ring"; the lines are sorted.
		var lines = scn.GetAffectingCard(frodo);
		assertEquals("The One Ring: Strength +1", lines.get(lines.size() - 1));
		assertTrue(lines.contains("System: Can't be discarded from play"));
	}
}
