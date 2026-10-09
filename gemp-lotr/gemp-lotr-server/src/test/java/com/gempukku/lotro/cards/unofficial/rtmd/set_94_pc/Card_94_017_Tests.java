package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_017_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_17", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
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
		 * Intensity: 2
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
		// Player 2's turn has begun.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
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
		scn.ShadowPass(); // Fellowship phase - P2 decides
		scn.FreepsPass(); // Shadow phase - P1 decides
		// No minions anywhere, so maneuver/archery/assignment/skirmish auto-skip to regroup.
		scn.ShadowPass(); // Regroup - P2 (current player) decides first
		scn.FreepsPass(); // then P1
		scn.ShadowChooseToStay(); // decline P2's own extra move
		// "each of your turns" is gated by OwnerIsFreeps, which is false while P2 is Fellowship-active
		// (the mod's owner P1 is Shadow-active this turn), so nothing fires here.
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));

		// Turn 3: back to P1 as Fellowship-active - the owner's turn again.
		scn.FreepsPass(); // Fellowship phase - P1 decides
		scn.ShadowPass(); // Shadow phase - P2 decides
		scn.FreepsPass(); // Regroup - P1 decides first
		scn.ShadowPass(); // then P2
		scn.FreepsChooseToStay(); // decline P1's own extra move

		// Both players have now each had one Fellowship turn (P1 at site 3, P2 at site 2), so
		// site 1 is behind both of them and available to control.
		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		scn.ShadowChooseYes();

		assertEquals(shadowSites + 1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	@Ignore("F-0327: with the modifier in player 2's seat, player 1 is asked to take control of a site before the first turn begins")
	public void NothingFiresAtEndOfTheOwnersShadowTurnSinceThatIsNotOneOfTheirTurns() throws DecisionResultInvalidException, CardNotFoundException {
		// "Each of your turns" - the mod is owned by the physical Shadow-labeled player here, so
		// their own turn is the one where they are playing the Free Peoples role, not their Shadow
		// role. On turn 1 they are Shadow-active, so end-of-turn 1 should not offer anything.
		var scn = GetShadowScenario();

		scn.StartGame();
		int freepsSites = scn.GetFreepsControlledSiteCount();
		int shadowSites = scn.GetShadowControlledSiteCount();

		// No turn has ended yet, the owner's or anybody's: nothing is asked before the first turn begins.
		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertEquals(freepsSites, scn.GetFreepsControlledSiteCount());
		assertEquals(shadowSites, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	@Ignore("Q-0195: is the opponent asked at all while no site can be controlled? As built, and as the first test above has it, he is asked and his yes does nothing")
	public void TheOpponentIsNotAskedWhileNoSiteCanBeControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		// Player 2's marker is still on site 1, so no site has every marker beyond it.
		assertFalse(scn.ShadowDecisionAvailable("take control of a site"));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void DecliningLeavesNoSiteControlledAndTheOfferComesAgainAtTheEndOfTheOwnersNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		// Turn 1, the owner's: player 2's marker is still on site 1 and there is no site to take.
		// As the game stands he is asked all the same.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.ShadowChooseNo();

		// Turn 2, player 2's.
		scn.SkipToMovementDecision();
		scn.ShadowChooseToStay();

		// Turn 3, the owner's: he stops at site 3 with player 2 at site 2, and site 1 can be taken.
		scn.SkipToMovementDecision();
		var site1 = scn.GetSite(1);
		scn.FreepsChooseToStay();

		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseNo();

		// Declined: nobody controls a site, and the owner is not asked in his opponent's place.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 4, player 2's: nothing is asked at its end.
		scn.SkipToMovementDecision();
		scn.ShadowChooseToStay();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 5, the owner's again, and the offer with it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertTrue(scn.ShadowDecisionAvailable("take control of a site"));
		scn.ShadowChooseYes();

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	@Ignore("F-0327: with the modifier in player 2's seat, player 1 is asked to take control of a site before the first turn begins")
	public void AtTheEndOfEachTurnOfAnOwnerInPlayer2sSeatPlayer1MayTakeTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		// No turn has ended yet, the owner's or anybody's: nothing is asked before the first turn begins.
		assertFalse(scn.FreepsDecisionAvailable("take control of a site"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 1 is player 1's and not the owner's: nothing is asked at its end.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 2, the owner's: both markers are on site 2, and site 1 can be taken.
		scn.SkipToMovementDecision();
		var site1 = scn.GetSite(1);
		scn.ShadowChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("take control of a site"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 3, player 1's: nothing again.
		scn.SkipToMovementDecision();
		var site2 = scn.GetSite(2);
		scn.FreepsChooseToStay();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 4, the owner's: both markers are on site 3, and site 2 is the lowest that can be taken.
		scn.SkipToMovementDecision();
		scn.ShadowChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("take control of a site"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsControls(site2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
