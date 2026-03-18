package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_91_002_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_2");
					put("freepscard", "1_3");
					put("shadowcard", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SearchDeckStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_2
		 * Type: MetaSite
		 * Intensity: 6
		 * Game Text: At the start of each of your fellowship phases, you may take any card into hand from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsGetsOptionalSearchAtStartOfFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: "At the start of each of your fellowship phases, you may take any card
		// into hand from your draw deck."
		// FP should get an optional trigger at the start of fellowship to search deck.

		var scn = GetScenario();

		var mod = scn.GetFreepsCard("mod");
		var freepsCard = scn.GetFreepsCard("freepscard");
		var shadowCard = scn.GetFreepsCard("shadowcard");
		scn.MoveCardsToSupportArea(mod);

		scn.StartGame();

		// At the start of the fellowship phase, FP should have an optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoicesAvailable(freepsCard, shadowCard));
		scn.FreepsChooseCard(freepsCard);
		assertInZone(Zone.HAND, freepsCard);
	}

	@Test
	public void ShadowDoesNotGetSearchAtStartOfFreepsFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: Should only trigger for the FP player's fellowship phase.
		// BUG: Shadow's copy may also trigger at the start of fellowship.

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		scn.MoveCardsToSupportArea(shadowMod);

		scn.StartGame();

		// Shadow should NOT get an optional trigger from their copy
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetShadowHandCount());
	}
}
