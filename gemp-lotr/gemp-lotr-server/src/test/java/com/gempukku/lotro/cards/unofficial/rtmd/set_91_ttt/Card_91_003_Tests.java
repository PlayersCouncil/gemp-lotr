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
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_3");
					put("filler1", "1_3");
					put("filler2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrawOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_3
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Each time your fellowship moves, you may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsDrawsCardWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_3: "Each time your fellowship moves, you may draw a card."
		// When the FP player moves, they should get an optional trigger to draw.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		scn.MoveCardsToSupportArea(freepsMod);

		scn.StartGame();

		// === FELLOWSHIP PHASE ===
		// FP passes, triggering a move from site 1 to site 2
		scn.FreepsPass();

		// FP should get the optional trigger from their copy
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetFreepsHandCount());
	}

	@Test
	public void FreepsDoesNotDrawForShadowMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_3: "Each time your fellowship moves, you may draw a card."
		// BUG: Shadow player's copy of the modifier also triggers when FP moves.
		// The Shadow player should NOT get an optional trigger from their copy,
		// because "your fellowship" should only refer to the FP player's fellowship.

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		scn.MoveCardsToSupportArea(shadowMod);

		scn.StartGame();

		// === FELLOWSHIP PHASE ===
		scn.FreepsPass();

		// Shadow should NOT get a trigger from their copy
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Shadow hand count should be unchanged
		assertEquals(0, scn.GetShadowHandCount());

		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
