package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the helpers that play a turn out answer a reconcile in full.  A player with more cards
 * in hand than the hand size is asked for all the cards to discard in one answer ("Choose cards to discard down to
 * 8": 3 cards from a hand of 11), and the helpers used to answer with one.
 */
public class ReconcileSkippingTests
{
	// Each player has 11 Hobbit Swords and nothing else: a possession with no text.  The helpers only ever pass,
	// so nothing is played on the way; with every sword in hand the draw deck is empty and nothing is drawn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					for(int i = 1; i <= 11; i++) {
						put("sword" + i, "1_299");
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SkipToMovementDecisionDiscardsTheShadowPlayerDownFromAHandOfEleven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		for(int i = 1; i <= 11; i++) {
			scn.MoveCardsToHand(scn.GetShadowCard("sword" + i));
		}

		scn.StartGame();
		assertEquals(11, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.SkipToMovementDecision();
		// 11 in hand, a hand size of 8: three cards in one answer
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void SkipToMovementDecisionOnPlayerTwosTurnDiscardsPlayerOneDownFromAHandOfEleven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Player 1 is the Shadow player of this turn
		for(int i = 1; i <= 11; i++) {
			scn.MoveCardsToHand(scn.GetFreepsCard("sword" + i));
		}
		assertEquals(11, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void SkipToOtherPlayersTurnDiscardsBothPlayersDownFromHandsOfEleven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		for(int i = 1; i <= 11; i++) {
			scn.MoveCardsToHand(scn.GetFreepsCard("sword" + i), scn.GetShadowCard("sword" + i));
		}

		scn.StartGame();
		assertEquals(11, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetShadowHandCount());

		scn.SkipToOtherPlayersTurn();
		// The Shadow player reconciles when the regroup actions are done, the Free Peoples player on staying
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void SkipToOtherPlayersTurnOnPlayerTwosTurnDiscardsBothPlayersDownFromHandsOfEleven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		for(int i = 1; i <= 11; i++) {
			scn.MoveCardsToHand(scn.GetFreepsCard("sword" + i), scn.GetShadowCard("sword" + i));
		}
		assertEquals(11, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.SkipToOtherPlayersTurn();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkipCurrentSiteDiscardsBothPlayersDownFromHandsOfEleven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		for(int i = 1; i <= 11; i++) {
			scn.MoveCardsToHand(scn.GetFreepsCard("sword" + i), scn.GetShadowCard("sword" + i));
		}

		scn.StartGame();
		assertEquals(11, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetShadowHandCount());
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.SkipCurrentSite();
		// Both turns played: each player discarded down on player 1's turn and had 8 to keep on player 2's
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
