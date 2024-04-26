
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_363_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("greenleaf", "1_50");
					put("moriatroop1", "1_177");
					put("moriatroop2", "1_177");
					put("moriatroop3", "1_177");
					put("shelob", "8_26");
                }},
                new HashMap<>() {{
                    put("site1", "1_319");
                    put("site2", "1_327");
                    put("site3", "1_337");
                    put("site4", "1_343");
                    put("site5", "1_349");
                    put("site6", "1_350");
                    put("site7", "1_353");
                    put("site8", "1_356");
                    put("site9", "1_363");
                }},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TolBrandirRuinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Tol Brandir
		* Shadow Number: 9
		* Type: Site
		* Site Number: 9
		* Game Text: River.  Shadow: Play up to 3 trackers from your discard pile; end your Shadow phase.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl site9 = scn.GetFreepsSite(9);

		assertFalse(site9.getBlueprint().isUnique());
		assertEquals(CardType.SITE, site9.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(site9, Keyword.RIVER)); // test for keywords as needed
		assertEquals(9, site9.getBlueprint().getTwilightCost());
		assertEquals(9, site9.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
	}

	@Test
	public void TolBrandirActionPlays3TrackersAndEndsShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl moriatroop1 = scn.GetShadowCard("moriatroop1");
		PhysicalCardImpl moriatroop2 = scn.GetShadowCard("moriatroop2");
		PhysicalCardImpl moriatroop3 = scn.GetShadowCard("moriatroop3");
		PhysicalCardImpl shelob = scn.GetShadowCard("shelob");
		scn.ShadowMoveCardToHand(moriatroop1, moriatroop2, moriatroop3, shelob);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		// 1 -> 3
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 3 -> 4
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 4 -> 5
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 5 -> 6
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 6 -> 7
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 7 -> 8
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		// 8 -> 9
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		//TODO: actually finish this

	}
}
