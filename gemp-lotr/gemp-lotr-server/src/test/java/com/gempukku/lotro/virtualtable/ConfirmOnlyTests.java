package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the reading of a decision that asks only to be confirmed (FreepsIsAskedOnlyToConfirm and
 * its twin) and the answer to one (FreepsConfirm and its twin), which refuses to answer anything else.  The one such
 * decision the game asks is "Starting fellowship - Nothing more can be added: press DONE", once a player has put a
 * card into a starting fellowship and nothing more can go into it (mechanics/game/StartingFellowshipTests).
 */
public class ConfirmOnlyTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two companions of cost 2 and no text that touches a cost: the two of them are the 4
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race
					put("legolas", "3_121");    // Legolas, Son of Thranduil
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void AChoiceThatStillOffersCardsIsNotReadAsAskingOnlyToConfirmAndConfirmingThereIsRefused() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		// The ordinary choice, whose DONE chooses nobody: it offers cards, so it is not a confirmation
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertFalse(scn.FreepsIsAskedOnlyToConfirm());
		// The other player is asked nothing yet
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.ShadowIsAskedOnlyToConfirm());

		assertRefused(scn::FreepsConfirm, "was to confirm", "Starting fellowship - Choose next character");
		assertRefused(scn::ShadowConfirm, "was to confirm", "is not being asked anything");

		// Nothing was answered
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertInZone(Zone.DECK, gimli, legolas);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void TheDoneAskIsReadAsAskingOnlyToConfirmAndConfirmingAnswersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(legolas);

		// 2 + 2 is the 4: the player is asked only to press DONE, and only that player is
		assertInZone(Zone.FREE_CHARACTERS, gimli, legolas);
		assertTrue(scn.FreepsIsAskedOnlyToConfirm());
		assertFalse(scn.ShadowIsAskedOnlyToConfirm());
		assertRefused(scn::ShadowConfirm, "was to confirm", "is not being asked anything");

		scn.FreepsConfirm();

		// Answered: the other player chooses now, at a choice that offers his own companions
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.FreepsIsAskedOnlyToConfirm());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship - Choose next character"));
		assertFalse(scn.ShadowIsAskedOnlyToConfirm());
	}
}
