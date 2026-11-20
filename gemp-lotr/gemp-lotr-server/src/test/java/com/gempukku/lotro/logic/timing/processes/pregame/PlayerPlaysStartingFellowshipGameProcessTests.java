package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of a decision of PlayerPlaysStartingFellowshipGameProcess checking its own answer: a piece of the server,
 * and no behaviour of any card.  Once a player has put a card into a starting fellowship and nothing more can go
 * into it, the game asks only to press DONE ("Starting fellowship - Nothing more can be added: press DONE"), and the
 * one answer it takes is the empty one.  What a player is asked, and when, is mechanics/game/StartingFellowshipTests.
 *
 * The answer refused here is one no rig method gives, since the rig answers a choice of cards only with cards it is
 * offering: so this test gives it raw, as a client would, and reads the refusal.  The process needs a game, which
 * the logic module, where the class is, has no card library to build: so the test is here, on the test rig.
 */
public class PlayerPlaysStartingFellowshipGameProcessTests
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

	@Test
	public void TheDoneAskRefusesAnAnswerThatNamesACardAndIsStillAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(legolas);

		assertTrue(scn.FreepsIsAskedOnlyToConfirm());
		assertEquals(0, scn.FreepsGetCardChoiceCount());

		// The first card of a showing, as the choice before this one was answered when it named a card
		var refused = assertThrows(RuntimeException.class, () -> scn.FreepsDecided("temp0")); // lint-ok: an answer no rig method gives, which is what is under test
		assertTrue(String.valueOf(refused.getCause()), refused.getCause() instanceof DecisionResultInvalidException);

		// Refused, nothing came of it: the same ask is pending, and the fellowship is not complete
		assertTrue(scn.FreepsIsAskedOnlyToConfirm());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.FREE_CHARACTERS, gimli, legolas);

		// The empty answer is taken
		scn.FreepsConfirm();

		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship - Choose next character"));
	}
}
