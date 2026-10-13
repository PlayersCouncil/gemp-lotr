package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_003_Tests
{

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Axe Strike: twelve cards to draw, so that a reconcile leaves a draw deck behind it
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
					put("filler5", "1_3");
					put("filler6", "1_3");
					put("filler7", "1_3");
					put("filler8", "1_3");
					put("filler9", "1_3");
					put("filler10", "1_3");
					put("filler11", "1_3");
					put("filler12", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Axe Strike: twelve cards to draw, so that a reconcile leaves a draw deck behind it
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
					put("filler5", "1_3");
					put("filler6", "1_3");
					put("filler7", "1_3");
					put("filler8", "1_3");
					put("filler9", "1_3");
					put("filler10", "1_3");
					put("filler11", "1_3");
					put("filler12", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_3"
		);
	}

	@Test
	public void DrawOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_3
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Each time your fellowship moves, you may draw a card.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsDrawsCardWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_3: "Each time your fellowship moves, you may draw a card."
		// When the FP player moves, they should get an optional trigger to draw.

		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// === FELLOWSHIP PHASE ===
		// FP passes, triggering a move from site 1 to site 2
		scn.FreepsPass();

		// FP should get the optional trigger from their copy
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// One card, with eleven more to be had
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainWhenTheFellowshipMovesInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());

		// The first move of the turn, from site 1 to site 2
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());

		// The second, from site 2 to site 3 at the end of the regroup phase
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(10, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDrawsNothingAndNobodyElseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());
		// "You may" is the modifier's owner and nobody else: the other player is not offered the draw
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferIsStillMadeWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		// The whole draw deck is in hand before the game begins
		scn.FreepsDrawCards(12);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(12, scn.GetFreepsHandCount());

		// An optional draw is still offered with no card to draw, and draws nothing
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(12, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsDoesNotDrawForShadowMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_3: "Each time your fellowship moves, you may draw a card."
		// Shadow player's copy should NOT trigger when FP moves.

		var scn = GetShadowScenario();

		scn.StartGame();

		// === FELLOWSHIP PHASE ===
		scn.FreepsPass();

		// Shadow should NOT get a trigger from their copy
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Shadow hand count should be unchanged
		assertEquals(0, scn.GetShadowHandCount());

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowOwnerMayDrawWhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		// Player 2's turn: the fellowship that moves is now his
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
