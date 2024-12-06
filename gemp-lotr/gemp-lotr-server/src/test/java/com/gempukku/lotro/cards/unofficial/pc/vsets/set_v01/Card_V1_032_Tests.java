
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_032_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("terror", "101_32");
                    put("balrog", "2_51");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: *Terror at Its Coming
		 * Side: Free Peoples
		 * Culture: moria
		 * Twilight Cost: 0
		 * Type: condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves you may reveal The Balrog from your hand to add (2).
		 */

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl terror = scn.GetFreepsCard("terror");

		assertTrue(terror.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(terror, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(0, terror.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, terror.getBlueprint().getCardType());
		assertEquals(Culture.MORIA, terror.getBlueprint().getCulture());
		assertEquals(Side.SHADOW, terror.getBlueprint().getSide());
	}

	@Test
	public void TerrorDoesNothingIfNoBalrog() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl terror = scn.GetShadowCard("terror");
		scn.ShadowMoveCardToSupportArea(terror);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void TerrorAddsTwilightDuringEachMoveIfBalrogPresent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl terror = scn.GetShadowCard("terror");
		PhysicalCardImpl balrog = scn.GetShadowCard("balrog");
		scn.ShadowMoveCardToSupportArea(terror);
		scn.ShadowMoveCardToHand(balrog);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		// 2 for the site, 1 for companions, 2 for Terror
		assertEquals(5, scn.GetTwilight());
	}
}
