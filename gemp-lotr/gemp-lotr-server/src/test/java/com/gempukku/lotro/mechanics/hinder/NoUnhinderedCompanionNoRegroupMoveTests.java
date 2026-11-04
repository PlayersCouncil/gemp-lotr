package com.gempukku.lotro.mechanics.hinder;

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
 * A fellowship with no unhindered companion does not move again in the regroup phase.
 *
 * Comprehensive Rules 5.0, "hinder", 17: "During the Regroup phase, if the fellowship has no unhindered
 * companions, the Free Peoples player cannot choose to move."
 *
 * The game used to read only whether a card forbade the move and whether the move limit allowed it, so a player
 * whose every companion was hindered was asked "Do you want to make another move?" and Yes moved the fellowship
 * (F-0315; met at Race Text 92_25).  Now such a player is not asked: the fellowship stays and its player
 * reconciles, which restores his hindered cards.  A card that makes the Free Peoples player choose to move again
 * "(if the move limit allows)" gives way as well: it cannot make him do what the rules say he cannot.
 *
 * The rule speaks of the regroup phase.  The move every fellowship must make in its fellowship phase is no
 * choice, and is made whoever is hindered.
 */
public class NoUnhinderedCompanionNoRegroupMoveTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard: a companion with no text that matters once in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("sword", "1_299");      // Hobbit Sword: a card to hold, so that a reconcile has something to ask

					// No Retreat: "Plays on a site you control.  Regroup: Spot 2 [dunland] Men and discard this
					// condition to make the Free Peoples player choose to move again this turn (if the move limit
					// allows)."
					put("retreat", "4_30");
					put("brigand1", "4_10");    // Dunlending Brigand: a [dunland] Man minion with no text
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AFellowshipWhoseOnlyCompanionIsHinderedIsNotAskedAndReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(frodo);
		assertTrue(scn.IsHindered(frodo));
		// The other player's Ring-bearer is on the table and unhindered: he is no part of this fellowship
		assertFalse(scn.IsHindered(scn.GetShadowRingBearer()));

		scn.MarkGameLog();
		scn.SkipToMovementDecision();

		// One move made of the two the move limit allows, and nothing forbids another but the rule
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		// The player is told why the question was not put
		assertTrue(scn.GameLogSinceMarkSays("has no unhindered companion and cannot choose to move again"));
		// The regroup phase has gone on to the reconcile of the player who stays, which restores his cards first
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(frodo));

		scn.FreepsDeclineReconciliation();
		// The turn is over where it stood: the other player's fellowship phase, at his own site 1
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCurrentSiteNumber());
	}

	@Test
	public void EveryCompanionHinderedIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(frodo, guard, aragorn);

		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(guard));
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void OneUnhinderedCompanionBesideAHinderedRingBearerIsAskedAndMayMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(frodo, aragorn);

		scn.SkipToMovementDecision();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(guard));
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		// Nobody reconciled: the hindered companions go on hindered
		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUnhinderedRingBearerBesideHinderedCompanionsIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(guard, aragorn);

		scn.SkipToMovementDecision();

		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
	}

	@Test
	public void ACompanionRestoredBeforeTheQuestionIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(frodo, guard);
		// Both hindered as the regroup phase begins; one is restored while its actions are still being taken
		scn.FreepsPass();
		scn.RestoreCard(guard);
		scn.ShadowPass();
		scn.PassReconcile(VirtualTableScenario.P2);

		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(guard));
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void TheMoveOfTheFellowshipPhaseIsMadeAndPlayerTwoIsNotAskedOnHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var otherFrodo = scn.GetShadowRingBearer();

		// Player 1 has two companions who are never hindered; they are no part of player 2's fellowship
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.HinderCard(otherFrodo);
		scn.SkipToMovementDecision();

		// Hindered since his fellowship phase: the move every turn must make is no choice, and was made
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.IsHindered(scn.GetFreepsCard("guard")));
		// On player 2's turn the "Shadow" methods are the Free Peoples player's
		assertFalse(scn.ShadowDecisionAvailable("another move"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(otherFrodo));
	}

	@Test
	public void ACardThatMakesThePlayerChooseToMoveAgainGivesWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var retreat = scn.GetShadowCard("retreat");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		// Both fellowships have left site 1, so it can be controlled
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(scn.GetSite(1), retreat);
		scn.SetFreepsHand(sword);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.HinderCard(frodo, guard);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(retreat));
		scn.ShadowUseCardAction(retreat);
		assertInZone(Zone.DISCARD, retreat);

		scn.SkipToMovementDecision();

		// The move limit allows the move and the card would have him choose it: he cannot, and stays
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(guard));

		scn.FreepsDeclineReconciliation();
		// The turn ended: the minions are gone and the other player's turn has begun
		assertInZone(Zone.DISCARD, brigand1, brigand2);
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheSameCardMovesAFellowshipWithOneUnhinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var retreat = scn.GetShadowCard("retreat");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.AttachCardsTo(scn.GetSite(1), retreat);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.HinderCard(frodo);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(retreat));
		scn.ShadowUseCardAction(retreat);
		assertInZone(Zone.DISCARD, retreat);

		scn.SkipToMovementDecision();

		// Not asked here either: the card has made the choice, and the fellowship has moved on
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.IsHindered(frodo));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand1, brigand2);
	}
}
