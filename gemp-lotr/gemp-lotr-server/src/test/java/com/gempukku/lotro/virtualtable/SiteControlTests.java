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
 * Tests of the rig itself: the cheats that give a player control of a site.  They answer no decision, so they work
 * whoever is being asked what, and they say so themselves when the site is not one that can be controlled
 * (CR 4.2 "control": a site on the adventure path that nobody controls and every player's marker has moved beyond).
 */
public class SiteControlTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Runner: a minion the Shadow player can afford, so that he has an action of his own on offer
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// What the rig said when it refused, or null if it did not refuse.
	private static String Refusal(Runnable answer) {
		try {
			answer.run();
		}
		catch (AssertionError refusal) {
			return String.valueOf(refusal.getMessage());
		}
		return null;
	}

	@Test
	public void EitherPlayerIsGivenTheFirstSiteThatCanBeControlledWhateverIsBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// Both markers at site 4: sites 1 to 3 can be controlled.
		scn.SkipToSite(4);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.MoveCardsToHand(runner);
		scn.SetTwilight(5);

		// The fellowship phase: the Shadow player is being asked nothing at all.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.ADVENTURE_PATH, site1);

		scn.ShadowTakeControlOfSite();

		// The lowest site, in his support area as the rules have it.
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The player whose turn it is may be given one as well: the next lowest.
		scn.FreepsTakeControlOfSite();

		assertTrue(scn.FreepsControls(site2));
		assertInZone(Zone.SUPPORT, site2);
		assertFalse(scn.IsSiteControlled(site3));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();

		// The Shadow phase, with a minion on offer to be played.  Taking a site answers nothing: the minion is
		// not played, nothing is paid, and the same decision is still pending.
		// 5 set + 1 companion + 6 for site 5
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(runner));

		scn.ShadowTakeControlOfSite();

		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.HAND, runner);
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 4 is where the second player's marker stands, so nothing is left to take, and the rig says so.
		var refusal = Refusal(scn::ShadowTakeControlOfSite);
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("No site can be controlled"));
		assertNotNull(Refusal(scn::FreepsTakeControlOfSite));
		assertFalse(scn.IsSiteControlled(site4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANamedSiteIsTakenOutOfTurnAndOneThatCannotBeControlledIsRefused() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(4);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);

		// Site 3, with sites 1 and 2 still on the path.
		scn.ShadowTakeControlOfSite(site3);

		assertTrue(scn.ShadowControls(site3));
		assertInZone(Zone.SUPPORT, site3);
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));

		scn.FreepsTakeControlOfSite(site1);

		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.IsSiteControlled(site2));

		// A site somebody controls is not taken again, by either player.
		var refusal = Refusal(() -> scn.FreepsTakeControlOfSite(site3));
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("already controlled"));
		assertNotNull(Refusal(() -> scn.ShadowTakeControlOfSite(site3)));
		assertTrue(scn.ShadowControls(site3));

		// Nor is the site the fellowships are at, nor a card that is no site.
		refusal = Refusal(() -> scn.ShadowTakeControlOfSite(site4));
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("every marker has moved beyond it"));
		assertFalse(scn.IsSiteControlled(site4));
		refusal = Refusal(() -> scn.ShadowTakeControlOfSite(runner));
		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("is not a site"));

		// The first site that can be controlled is now site 2.
		scn.ShadowTakeControlOfSite();

		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
