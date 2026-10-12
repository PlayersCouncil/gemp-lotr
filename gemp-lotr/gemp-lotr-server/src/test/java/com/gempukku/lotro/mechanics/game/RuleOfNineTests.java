package com.gempukku.lotro.mechanics.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The Rule of 9.
 *
 * Comprehensive Rules 4.2, "The Rule of 9": "You cannot have more than nine total companions in play and in your
 * dead pile at any time.  (Each copy of a companion in play or in your dead pile counts as a separate companion,
 * whether it is unique or non-unique.)"  Its example: "If you have Merry (a unique companion) and two copies of
 * Dwarf Guard (a non-unique companion) in your dead pile, you cannot have more than 6 companions in your
 * fellowship."
 *
 * That a hindered companion still counts is in HinderedCardStillRestrictsPlayTests.
 */
public class RuleOfNineTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");      // Merry, Friend to Sam: unique; in the dead pile
					// Dwarf Guard: not unique, cost 1.  "To play, spot a Dwarf."  Two in the dead pile and
					// two in hand.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");

					// Four companions to stand beside the Ring-bearer.  None of them has text that bears on
					// a play from hand.
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf the Guards spot
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("boromir", "1_96");     // Boromir, Lord of Gondor

					put("armor", "1_8");        // Dwarven Armor: cost 0, on a Dwarf; a card that is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithMerryAndTwoDwarfGuardsInTheDeadPileTheFellowshipHoldsSixCompanionsAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var armor = scn.GetFreepsCard("armor");
		// With the Ring-bearer, five companions in play
		scn.MoveCompanionsToTable(gimli, scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("legolas"),
				scn.GetFreepsCard("boromir"));
		// The rules' own example: three companions in the dead pile, two of them copies of one card
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("merry"), scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToHand(guard3, guard4, armor);

		scn.StartGame();

		assertEquals(3, scn.GetFreepsDeadCount());
		// Five in play and three dead: eight, and room for one more, whichever Guard it is.
		assertTrue(scn.FreepsPlayAvailable(guard3));
		assertTrue(scn.FreepsPlayAvailable(guard4));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(guard3);
		assertInZone(Zone.FREE_CHARACTERS, guard3);
		assertEquals(1, scn.GetTwilight());

		// Six in the fellowship and three dead: nine.  The other Guard cannot be played, though there are two
		// Dwarves to spot and the pool takes any cost.  Counted as one companion, the two dead Guards would
		// leave room for him; so would a count of the fellowship alone.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertInZone(Zone.HAND, guard4);

		// It is companions the rule counts: a card that is none is played as ever.
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		// Either Dwarf may bear it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard3));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(armor, gimli);
		assertFalse(scn.FreepsPlayAvailable(guard4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
