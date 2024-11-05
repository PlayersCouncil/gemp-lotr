
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_064_Tests
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
                    put("site9", "101_64");
                }},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AmonHenRuinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Amon Hen Ruins
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 9
		* Type: site
		* Subtype: 
		* Site Number: 9
		* Game Text: Forest. Each minion is damage +1 per wound on that minion.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl site9 = scn.GetFreepsSite(9);

		assertFalse(site9.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site9.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site9.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(site9, Keyword.FOREST)); // test for keywords as needed
		assertEquals(9, site9.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(9, site9.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void WoundedMinionsAreDamagePlusOnePerWound() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.ShadowMoveCharToTable(moriatroop1, moriatroop2, moriatroop3, shelob);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.hasKeyword(moriatroop1, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(moriatroop2, Keyword.DAMAGE));
		assertFalse(scn.hasKeyword(moriatroop3, Keyword.DAMAGE));

		scn.AddWoundsToChar(moriatroop2, 1);
		scn.AddWoundsToChar(moriatroop3, 2);
		scn.AddWoundsToChar(shelob, 7);

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(moriatroop1));
		assertEquals(1, scn.GetWoundsOn(moriatroop2));
		assertEquals(2, scn.GetWoundsOn(moriatroop3));
		assertEquals(7, scn.GetWoundsOn(shelob));

		assertFalse(scn.hasKeyword(moriatroop1, Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(moriatroop2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(moriatroop2,  Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(moriatroop3, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(moriatroop3,  Keyword.DAMAGE));
		assertTrue(scn.hasKeyword(shelob, Keyword.DAMAGE));
		assertEquals(7, scn.GetKeywordCount(shelob,  Keyword.DAMAGE));

	}
}
