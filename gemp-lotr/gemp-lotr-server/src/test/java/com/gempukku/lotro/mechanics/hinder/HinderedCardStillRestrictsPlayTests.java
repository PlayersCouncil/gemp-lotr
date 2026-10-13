package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A hindered companion is still a companion to the rules that count companions.
 *
 * Comprehensive Rules 5.0, "hinder": "Although the game text of hindered cards does not apply, 'passive play
 * restriction' rules continue to be enforced on the hindered card.  In general, if a new card play would result in
 * an inconsistent game state once a hindered card has been restored, that play is disallowed. ...  If a hindered
 * card is a Companion, it continues to count towards the Rule of 9.  Example: If you have 9 companions in play and
 * 1 is hindered, playing another companion would result in there being 10 companions in play after reconciling,
 * which is inconsistent with the Rule of 9, therefore it is disallowed. ...  If a hindered card is a Companion, it
 * continues to contribute towards the threat limit."
 *
 * Comprehensive Rules 4.2, "The Rule of 9": "You cannot have more than nine total companions in play and in your
 * dead pile at any time."  "Threats": "threats cannot be added if the number of threats on the dead pile is equal
 * to or greater than the number of companions in play."
 *
 * The same paragraph's sentence on a hindered unique card is UniquenessTests'; the one on a hindered item's class
 * is HinderedBorneCardTests'.  The hindering is how the board is laid, and is done by the rig.
 */
public class HinderedCardStillRestrictsPlayTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Seven companions to stand beside the Ring-bearer.  None of them has text that bears on
					// a play from hand: abilities of the maneuver, archery, skirmish and fellowship phases
					// that nobody uses here.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("boromir", "3_122");    // Boromir, Defender of Minas Tirith
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("merry", "1_302");      // Merry, Friend to Sam
					put("pippin", "1_306");     // Pippin, Friend to Frodo

					// Gandalf, The Grey Wizard: cost 4.  His fellowship ability plays a companion of his own
					// signet, and is not used.
					put("gandalf", "1_364");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: cost 2; nothing needed to play her

					// Dead Man of Dunharrow: cost 2.  "Enduring.  To play, add a threat.  When you play this
					// companion, exert him twice."
					put("deadman", "10_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCompanionStillCountsTowardTheRuleOfNine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		// With the Ring-bearer, eight
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"),
				scn.GetFreepsCard("pippin"));
		scn.MoveCardsToHand(gandalf, arwen);
		scn.HinderCard(aragorn);

		scn.StartGame();

		assertTrue(scn.IsHindered(aragorn));
		// Room for one more, whichever it is
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		assertTrue(scn.FreepsPlayAvailable(arwen));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(4, scn.GetTwilight());

		// Nine, the hindered Aragorn among them.  A count of the eight who can be spotted would let Arwen in.
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.FreepsPlayAvailable(arwen));
		assertInZone(Zone.HAND, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedCompanionStillCountsTowardTheThreatLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(deadman);

		scn.StartGame();

		// The Ring-bearer and Aragorn: a limit of 2.  At the limit the Dead Man's threat cannot be added.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsPlayAvailable(deadman));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(deadman));

		// A limit that fell to 1 with Aragorn hindered would withhold him as the second threat did
		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.FreepsPlayAvailable(deadman));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(deadman);
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetTwilight());
		// "When you play this companion, exert him twice."
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
