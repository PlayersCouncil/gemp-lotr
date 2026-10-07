package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * The definition word cantAddBurdens: "Burdens cannot be added."
 *
 * Neither rules document has such a text and no card uses the word yet.  F-0159: the word was mapped to the
 * producer of "burdens cannot be removed", and the class written for it built that modifier too, so the first
 * card to use it would have forbidden the removing of burdens and allowed the adding.  The game's own use of the
 * modifier (no burden is added while the Ring-bearer is hindered, CR 5.0 "hinder") does not go through the word.
 *
 * Shown with a card of the tests' own, Test Seal 0_8004: "Burdens cannot be added."  A burden as an effect is not
 * added, a burden as a cost cannot be paid (Comprehensive Rules 4.2, "cost": an action whose cost cannot be paid
 * may not be taken), and a burden can still be removed.
 */
public class CantAddBurdensTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Seal (the tests' own card): "Burdens cannot be added."
					put("seal", "0_8004");
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; Fellowship: exert Sam to remove a burden
					put("faintly", "4_309");    // Light Shining Faintly: Fellowship event, (0): add a burden to heal a Ring-bound companion twice
					put("found1", "2_78");      // It Wants to be Found: Maneuver event, (2): spot a twilight Nazgul to add a burden
					put("found2", "2_78");
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: a twilight Nazgul; his text needs 2 burdens or 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABurdenIsNotAddedAsAnEffectNorPayableAsACostAndOneCanStillBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seal = scn.GetFreepsCard("seal");
		var sam = scn.GetFreepsCard("sam");
		var faintly = scn.GetFreepsCard("faintly");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(faintly);

		var found1 = scn.GetShadowCard("found1");
		var found2 = scn.GetShadowCard("found2");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(found1, found2);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		// Without the Seal a burden can be paid as a cost.
		assertTrue(scn.FreepsPlayAvailable(faintly));

		scn.MoveCardsToSupportArea(seal);
		scn.FreepsRefreshDecision();

		// With it the cost cannot be paid, and the event is not offered.
		assertFalse(scn.FreepsPlayAvailable(faintly));

		// A burden can still be removed.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A burden as an effect: the event is played and paid for, and adds nothing.
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(found1));
		scn.ShadowPlayCard(found1);

		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, found1);
		assertEquals(0, scn.GetBurdens());

		// The same event on the same board without the Seal adds its burden.
		scn.MoveCardsToDiscard(seal);
		scn.FreepsPass();
		scn.ShadowPlayCard(found2);

		assertInZone(Zone.DISCARD, found2);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
