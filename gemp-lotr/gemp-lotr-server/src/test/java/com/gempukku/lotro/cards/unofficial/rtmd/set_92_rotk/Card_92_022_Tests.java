package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_92_022_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Saruman's Frost (1_135): Isengard condition, Weather, twilight 2, extra cost: exert an Isengard minion
		put("frost", "1_135");
		// Uruk Savage (1_151): Isengard Uruk-hai minion, twilight 2, vitality 2
		put("savage", "1_151");
		put("filler1", "1_152");
		put("filler2", "1_153");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_22", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_22"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_22
		 * Type: MetaSite
		 * Game Text: Shadow: Play a weather condition to draw a card; its twilight cost is -2.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_22", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlayWeatherConditionWithDiscountAndDraw() throws DecisionResultInvalidException, CardNotFoundException {
		// Frost costs 2, discount of 2 means it costs 0.
		// Playing it should also draw a card.

		var scn = GetShadowScenario();

		var frost = scn.GetShadowCard("frost");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseAnyCard(); //Any site

		// Drew a card after playing
		assertEquals(handBefore, scn.GetShadowHandCount());
		// handBefore - 1 (played frost) + 1 (drew card) = handBefore
	}

	@Test
	public void NotOwnerGated() throws DecisionResultInvalidException, CardNotFoundException {
		// Freeps owns the modifier; Shadow should still be able to use it

		var scn = GetFreepsScenario();

		var frost = scn.GetShadowCard("frost");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(frost);
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.ShadowActionAvailable(mod));
	}
}
