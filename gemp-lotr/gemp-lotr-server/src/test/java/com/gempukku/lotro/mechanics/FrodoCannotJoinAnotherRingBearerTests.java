package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A player whose Ring-bearer is not Frodo cannot play a Frodo who could have been one.
 *
 * Comprehensive Rules 4.2, "Ring-bearer": "If a character other than Frodo is your Ring-bearer, you cannot play
 * any version of Frodo with the Ring-bearer keyword during the game."
 *
 * "During the game" takes in the starting fellowship, where such a Frodo of cost 0 would otherwise always fit,
 * and every fellowship phase after it.  The rule is about the keyword and not about the title: Frodo, Frenzied
 * Fighter 13_149 is a Frodo who has no Ring-bearer keyword (CR 4.2's ruling on the card: "This version of Frodo
 * cannot become the Ring-bearer"), and he is played beside another Ring-bearer like any companion.  He is the
 * only such Frodo.
 *
 * So no Frodo who may bear the Ring is ever on the table beside a Ring-bearer who is not Frodo (Ketura on
 * Q-0159), and no test lays that board: these tests show the rule that keeps him off it.
 */
public class FrodoCannotJoinAnotherRingBearerTests
{
	// Sam, Bearer of Great Need is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Frodo, Courteous Halfling: "Ring-bearer (resistance 10)", cost 0, nothing needed to play him
					put("frodo", "4_301");
					// Frodo, Frenzied Fighter: Ring-bound, no Ring-bearer keyword, cost 2, nothing needed to play him
					put("fighter", "13_149");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit of cost 1, the control
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithSamAsRingBearerAFrodoWithTheRingBearerKeywordIsNotOfferedForTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var fighter = scn.GetFreepsCard("fighter");
		var merry = scn.GetFreepsCard("merry");

		assertEquals("Sam", scn.GetRingBearer().getBlueprint().getTitle());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, frodo, fighter, merry);

		// Courteous Halfling costs nothing and asks for nothing, and he is the one companion in the draw deck
		// who is not offered.  The Frodo without the keyword is.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, fighter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsChooseCard(merry);

		// Nor with the fellowship begun, and still before any Frodo is on the table to share his title
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fighter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
	}

	@Test
	public void WithSamAsRingBearerAFrodoWithTheRingBearerKeywordCannotBePlayedFromHandAndAFrodoWithoutItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var fighter = scn.GetFreepsCard("fighter");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(frodo, fighter, merry);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, frodo, fighter, merry);
		assertEquals(0, scn.GetTwilight());

		// No Frodo is in play: it is not his title that keeps Courteous Halfling in hand
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.FreepsPlayAvailable(fighter));
		assertFalse(scn.FreepsPlayAvailable(frodo));

		scn.FreepsPlayCard(fighter);

		// The Frodo who cannot bear the Ring joins Sam's fellowship as any companion does
		assertInZone(Zone.FREE_CHARACTERS, fighter);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.HAND, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
