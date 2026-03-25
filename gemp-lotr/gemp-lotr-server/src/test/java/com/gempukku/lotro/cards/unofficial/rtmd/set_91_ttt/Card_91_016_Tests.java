package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_016_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		put("filler1", "1_3");
		put("filler2", "1_3");
		// Need a minion so we don't skip straight to regroup
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_16", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_16"
		);
	}

	@Test
	public void DiscardAtRegroupStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_16
		 * Type: MetaSite
		 * Game Text: At the start of each regroup phase, you must discard a card from your hand.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_16", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsDiscardsAtStartOfRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_16: "At the start of each regroup phase, you must discard a card from your hand."
		// FP has the modifier. At the start of regroup, FP should be forced to discard 1 card.

		var scn = GetFreepsScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToFreepsHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// At start of regroup, FP should be forced to discard a card
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsChooseAnyCard();
		assertEquals(1, scn.GetFreepsHandCount());
	}

	@Test
	public void ShadowDiscardsAtStartOfRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_16: "At the start of each regroup phase, you must discard a card from your hand."
		// FP has the modifier. At the start of regroup, FP should be forced to discard 1 card.

		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToFreepsHand("filler1", "filler2");
		scn.MoveCardsToShadowHand("filler1", "filler2");

		scn.StartGame();
		scn.FreepsPass(); // move
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// At start of regroup, Shadow should be forced to discard a card
		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowChooseAnyCard();
		assertEquals(1, scn.GetShadowHandCount());

		// FP should NOT be forced to discard — modifier is on Shadow's copy
		assertEquals(2, scn.GetFreepsHandCount());
		// Should be at regroup actions, not a discard decision
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
