
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
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

public class Card_V1_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");

				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "101_63");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NenHithoelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Nen Hithoel
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 7
		* Type: site
		* Subtype: 
		* Site Number: 8
		* Game Text: River. When the fellowship moves to Nen Hithoel, each shadow player may draw a card for each companion over 4.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl site8 = scn.GetFreepsSite(8);

		assertFalse(site8.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site8.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site8.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(site8, Keyword.RIVER)); // test for keywords as needed
		assertEquals(7, site8.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(8, site8.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void ShadowDraws1CardPerCompOver4WhenMovingTo() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl guard1 = scn.GetFreepsCard("guard1");
		PhysicalCardImpl guard2 = scn.GetFreepsCard("guard2");
		PhysicalCardImpl guard3 = scn.GetFreepsCard("guard3");
		PhysicalCardImpl guard4 = scn.GetFreepsCard("guard4");
		PhysicalCardImpl guard5 = scn.GetFreepsCard("guard5");
		PhysicalCardImpl guard6 = scn.GetFreepsCard("guard6");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, guard5, guard6);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		// 1 -> 3
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		//scn.ShadowDeclineReconciliation();
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

		scn.MoveCardsToBottomOfShadowDeck("guard1", "guard2", "guard3", "guard4", "guard5", "guard6");
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowHandCount());

	}
}
