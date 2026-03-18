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
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_6");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BurdenOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_6
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Each time your fellowship moves, add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_6", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsAddsBurdenWhenMoving() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_6: "Each time your fellowship moves, add a burden."
		// When FP moves, a burden should be added.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		scn.MoveCardsToSupportArea(freepsMod);

		scn.StartGame();

		int burdensBefore = scn.GetBurdens();

		scn.FreepsPass(); // move to site 2

		// Burden should have been added (mandatory trigger)
		assertEquals(burdensBefore + 1, scn.GetBurdens());
	}

	@Test
	public void ShadowCopyDoesNotAddBurdenWhenFreepsMoves() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_6: "Each time your fellowship moves, add a burden."
		// BUG: Shadow's copy also triggers when FP moves, doubling the burden.
		// Only the FP player's copy should fire.

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		scn.MoveCardsToSupportArea(shadowMod);

		scn.StartGame();

		int burdensBefore = scn.GetBurdens();

		scn.FreepsPass(); // move to site 2

		// Shadow's copy should NOT add a burden
		assertEquals(burdensBefore, scn.GetBurdens());
	}
}
