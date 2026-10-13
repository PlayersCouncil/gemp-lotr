package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_006_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_6", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_6"
		);
	}

	@Test
	public void BurdenOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_6
		 * Type: MetaSite
		 * Intensity: 6
		 * Game Text: Each time your fellowship moves, add a burden.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(6, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsAddsBurdenWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_6: "Each time your fellowship moves, add a burden."

		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass(); // move to site 2

		// Burden should have been added (mandatory trigger): one, and nobody is asked
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMoveInTheSameTurnAddsASecondBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		// The first move of the turn, from site 1 to site 2
		scn.FreepsPass();
		assertEquals(1, scn.GetBurdens());

		// Nothing more is added until the fellowship moves again, at the end of the regroup phase
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetBurdens());
		scn.FreepsChooseToMove();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotAddBurdenWhenFreepsMoves() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_6: Shadow's copy should NOT add a burden when FP moves.

		var scn = GetShadowScenario();

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass(); // move to site 2

		// Shadow's copy should NOT add a burden
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayerTwoOwnerAddsABurdenWhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		// Player 1's turn: the fellowship that moves is not the owner's, and GetBurdens reads the turn's player
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());

		// Player 2's turn: the fellowship that moves is the owner's
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowPass();
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
