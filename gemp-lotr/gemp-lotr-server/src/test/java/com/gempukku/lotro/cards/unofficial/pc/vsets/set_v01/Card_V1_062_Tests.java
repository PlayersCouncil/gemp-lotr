
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

public class Card_V1_062_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("runner1", "1_178");
                    put("runner2", "1_178");

                    put("savage1", "1_151");
                    put("savage2", "1_151");
                }},
                new HashMap<>() {{
                    put("site1", "1_319");
                    put("site2", "1_327");
                    put("site3", "1_337");
                    put("site4", "1_343");
                    put("site5", "1_349");
                    put("site6", "1_350");
                    put("site7", "101_62");
                    put("site8", "1_356");
                    put("site9", "1_360");
                }},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ValleyoftheAnduinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Valley of the Anduin
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 6
		* Type: site
		* Subtype: 
		* Site Number: 7
		* Game Text: River. At the start of each skirmish phase, wound each unwounded character in that skirmish.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl site7 = scn.GetFreepsSite(7);

		assertFalse(site7.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site7.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site7.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(site7, Keyword.RIVER)); // test for keywords as needed
		assertEquals(6, site7.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(7, site7.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void EveryUnwoundedCharacterGetsWoundedAtStartOfSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");
		PhysicalCardImpl savage1 = scn.GetShadowCard("savage1");
		PhysicalCardImpl savage2 = scn.GetShadowCard("savage2");
		scn.ShadowMoveCardToHand(runner1, runner2, savage1, savage2);

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

		scn.ShadowMoveCharToTable(runner1, runner2, savage1, savage2);
		scn.AddWoundsToChar(savage1, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsPassCurrentPhaseAction(); // skipping freeps assignments

		scn.ShadowAssignToMinions(frodo, runner1, runner2, savage1, savage2);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(Zone.SHADOW_CHARACTERS, runner1.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, runner2.getZone());
		scn.FreepsResolveSkirmish(frodo);

		scn.FreepsDeclineOptionalTrigger(); // one ring response

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertEquals(Zone.DISCARD, runner1.getZone());
		assertEquals(Zone.DISCARD, runner2.getZone());

	}
}
