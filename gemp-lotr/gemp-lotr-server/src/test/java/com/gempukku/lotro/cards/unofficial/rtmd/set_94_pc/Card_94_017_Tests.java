package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_017_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_17", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_17"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_17
		 * Type: MetaSite
		 * Game Text: At the end of each of your turns, choose an opponent who may take control of
		 * a site.
		 */

		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_17", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void FiresAtEndOfTurnOneButFizzlesSinceNoSiteIsYetAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.SkipToMovementDecision();

		int freepsSites = scn.GetFreepsControlledSiteCount();
		int shadowSites = scn.GetShadowControlledSiteCount();
		scn.FreepsChooseToStay();

		// The mandatory trigger still fires and chooses Shadow as the opponent, but there is no
		// controllable site yet (Shadow hasn't had a Fellowship turn of its own) - cost/choice
		// happens, effect fizzles (Effect Fizzle: Costs Gate, Effects Attempt).
		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		scn.ShadowChooseYes();

		assertEquals(freepsSites, scn.GetFreepsControlledSiteCount());
		assertEquals(shadowSites, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void OpponentMayTakeControlOfASiteOnceOneIsAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();
		int shadowSites = scn.GetShadowControlledSiteCount();

		// Turn 1 (Freeps/P1 is Fellowship-active): decline the extra move, decline the fizzled offer.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		scn.ShadowChooseNo();

		// Turn 2: roles rotate, Shadow/P2 is now Fellowship-active. Drive it manually by player ID
		// (not by the Freeps/Shadow phase-role helpers, which name the physical player, not the
		// current role).
		scn.ShadowPassCurrentPhaseAction(); // Fellowship phase - P2 decides
		scn.FreepsPassCurrentPhaseAction(); // Shadow phase - P1 decides
		// No minions anywhere, so maneuver/archery/assignment/skirmish auto-skip to regroup.
		scn.ShadowPassCurrentPhaseAction(); // Regroup - P2 (current player) decides first
		scn.FreepsPassCurrentPhaseAction(); // then P1
		scn.ShadowChooseToStay(); // decline P2's own extra move
		// "each of your turns" is gated by OwnerIsFreeps, which is false while P2 is Fellowship-active
		// (the mod's owner P1 is Shadow-active this turn), so nothing fires here.
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));

		// Turn 3: back to P1 as Fellowship-active - the owner's turn again.
		scn.FreepsPassCurrentPhaseAction(); // Fellowship phase - P1 decides
		scn.ShadowPassCurrentPhaseAction(); // Shadow phase - P2 decides
		scn.FreepsPassCurrentPhaseAction(); // Regroup - P1 decides first
		scn.ShadowPassCurrentPhaseAction(); // then P2
		scn.FreepsChooseToStay(); // decline P1's own extra move

		// Both players have now each had one Fellowship turn (P1 at site 3, P2 at site 2), so
		// site 1 is behind both of them and available to control.
		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		scn.ShadowChooseYes();

		assertEquals(shadowSites + 1, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void NothingFiresAtEndOfTheOwnersShadowTurnSinceThatIsNotOneOfTheirTurns() throws DecisionResultInvalidException, CardNotFoundException {
		// "Each of your turns" - the mod is owned by the physical Shadow-labeled player here, so
		// their own turn is the one where they are playing the Free Peoples role, not their Shadow
		// role. On turn 1 they are Shadow-active, so end-of-turn 1 should not offer anything.
		var scn = GetShadowScenario();

		scn.StartGame();
		int freepsSites = scn.GetFreepsControlledSiteCount();
		int shadowSites = scn.GetShadowControlledSiteCount();

		// Pre-game setup transiently counts as the end of a "turn 0"; the mandatory trigger fires
		// once there too (offered to Freeps, matching how "OwnerIsFreeps" pre-resolves before turn
		// 1 properly begins). Decline that stray offer before driving turn 1 itself.
		if (scn.FreepsDecisionAvailable("take control of a site"))
			scn.FreepsChooseNo();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertEquals(freepsSites, scn.GetFreepsControlledSiteCount());
		assertEquals(shadowSites, scn.GetShadowControlledSiteCount());
	}
}
