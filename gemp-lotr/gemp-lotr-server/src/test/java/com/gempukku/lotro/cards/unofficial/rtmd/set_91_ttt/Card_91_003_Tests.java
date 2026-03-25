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
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		put("filler1", "1_3");
		put("filler2", "1_3");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
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
		 * Game Text: Each time your fellowship moves, you may draw a card.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsDrawsCardWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_3: "Each time your fellowship moves, you may draw a card."
		// When the FP player moves, they should get an optional trigger to draw.

		var scn = GetFreepsScenario();

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
}
