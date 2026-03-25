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
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		put("freepscard", "1_3");
		put("shadowcard", "1_151");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_2"
		);
	}

	@Test
	public void SearchDeckStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_2
		 * Type: MetaSite
		 * Game Text: At the start of each of your fellowship phases, you may take any card into hand from your draw deck.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsGetsOptionalSearchAtStartOfFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: "At the start of each of your fellowship phases, you may take any card
		// into hand from your draw deck."
		// FP should get an optional trigger at the start of fellowship to search deck.

		var scn = GetFreepsScenario();

		var freepsCard = scn.GetFreepsCard("freepscard");
		var shadowCard = scn.GetFreepsCard("shadowcard");

		scn.StartGame();

		// At the start of the fellowship phase, FP should have an optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(freepsCard, shadowCard));
		scn.FreepsChooseCard(freepsCard);
		assertInZone(Zone.HAND, freepsCard);
	}

	@Test
	public void ShadowDoesNotGetSearchAtStartOfFreepsFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_2: Should only trigger for the FP player's fellowship phase.

		var scn = GetShadowScenario();

		scn.StartGame();

		// Shadow should NOT get an optional trigger from their copy
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetShadowHandCount());
	}
}
