
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

public class Card_V1_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("sam", "1_311");
					put("merry", "1_302");
					put("stealth1", "1_298");
					put("stealth2", "1_298");
					put("stealth3", "1_298");


					put("chieftain", "1_163");
					put("runner", "1_178");
					put("uruk", "1_151");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "101_60");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaCrossroadsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Moria Crossroads
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 3
		* Type: site
		* Subtype: 
		* Site Number: 4
		* Game Text: Underground. While you can spot a unique [moria] minion, skirmishes involving [moria] minions may not be cancelled.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl site4 = scn.GetFreepsSite(4);

		assertFalse(site4.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site4.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site4.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(site4, Keyword.UNDERGROUND)); // test for keywords as needed
		assertEquals(3, site4.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(4, site4.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void UniqueMoriaMinionBlocksSkirmishCancelingInMoriaSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl stealth1 = scn.GetFreepsCard("stealth1");
		PhysicalCardImpl stealth2 = scn.GetFreepsCard("stealth2");
		PhysicalCardImpl stealth3 = scn.GetFreepsCard("stealth3");

		scn.MoveCardsToHand(stealth1, stealth2, stealth3);
		scn.MoveCompanionToTable(sam, merry);

		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		PhysicalCardImpl chieftain = scn.GetShadowCard("chieftain");
		PhysicalCardImpl uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(runner, chieftain, uruk);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		//Site 2
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		if(scn.ShadowDecisionAvailable("reconcile"))
		{
			scn.ShadowDeclineReconciliation();
		}
		scn.FreepsChooseToMove();

		//Site 3
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		if(scn.ShadowDecisionAvailable("reconcile"))
		{
			scn.ShadowDeclineReconciliation();
		}
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, uruk},
				new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{frodo, chieftain}
		);

		// We cannot actually test ahead of time whether Stealth will be allowed to resolve, since per RAW effects
		// are to be attempted as much as possible and if they fail, they fail.
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPlayCard(stealth1);

		//non-moria skirmish stealth allowed, skipped to next skirmish
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(stealth2);
		//non-unique moria skirmish stealth BLOCKED, so shadow action next as normal
		assertTrue(scn.ShadowDecisionAvailable("Choose action to play or Pass"));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPlayCard(stealth3);
		//unique moria skirmish stealth BLOCKED, so shadow action next as normal
		assertTrue(scn.ShadowDecisionAvailable("Choose action to play or Pass"));
	}

	@Test
	public void NonUniqueMoriaMinionDoesNotBlocksSkirmishCanceling() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl stealth1 = scn.GetFreepsCard("stealth1");
		PhysicalCardImpl stealth2 = scn.GetFreepsCard("stealth2");

		scn.MoveCardsToHand(stealth1, stealth2);
		scn.MoveCompanionToTable(sam, merry);

		PhysicalCardImpl runner = scn.GetShadowCard("runner");
		PhysicalCardImpl uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(runner, uruk);

		//Max out the move limit so we don't have to juggle play back and forth
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 10));

		scn.StartGame();

		//Site 2
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		if(scn.ShadowDecisionAvailable("reconcile"))
		{
			scn.ShadowDeclineReconciliation();
		}
		scn.FreepsChooseToMove();

		//Site 3
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		if(scn.ShadowDecisionAvailable("reconcile"))
		{
			scn.ShadowDeclineReconciliation();
		}
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, uruk},
				new PhysicalCardImpl[]{sam, runner}
		);

		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPlayCard(stealth1);
		//non-moria skirmish stealth allowed, skipped to next skirmish
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));


		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(stealth2);
		//non-unique moria skirmish stealth ALLOWED
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
