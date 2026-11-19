package com.gempukku.lotro.mechanics.ring;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a companion who becomes the Ring-bearer takes over from the one who died, shown with the first Sam to say
 * "make Sam the Ring-bearer (resistance 5)".
 *
 * Comprehensive Rules 4.2, "burden": burdens are only placed on your Ring-bearer, and each reduces the resistance
 * of every companion in the fellowship by 1.  "leaving play": any tokens on a card that leaves play are removed.
 *
 * Ketura's ruling (2026-10-05): resistance is a stat a companion always has, so "(resistance 5)" states Sam's
 * resistance and sets nothing; and as burdens are tokens on the Ring-bearer who died, they go with him, and Sam
 * takes up the Ring with no burdens.
 */
public class RingBearerSuccessionTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: strength 3, resistance 5.
					// Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
					put("sam", "1_311");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, resistance 10
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	@Test
	public void TheNewRingBearerTakesTheRingAndNoneOfTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(3);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertEquals(3, scn.GetBurdens());
		assertAttachedTo(ring, frodo);
		// Each burden on the Ring-bearer takes 1 from every companion's resistance: 10 - 3 and 5 - 3.
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(2, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertSame(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertFalse(scn.GameIsFinished());
		// The burdens were on Frodo and left play with him.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
	}

	@Test
	public void BurdensAddedAfterwardsCountAgainstTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		scn.PassSkirmishActions();
		scn.FreepsAcceptOptionalTrigger();

		assertSame(sam, scn.GetRingBearer());
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));

		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
	}
}
