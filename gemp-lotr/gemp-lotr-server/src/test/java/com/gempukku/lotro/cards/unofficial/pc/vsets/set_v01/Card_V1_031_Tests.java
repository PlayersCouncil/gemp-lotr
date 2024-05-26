
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_031_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("purpose", "101_31");
                    put("merry", "1_303");
                    put("pippin", "1_306");
                    put("aragorn", "1_89");

                    put("ftentacle1", "2_58");
                    put("ftentacle2", "2_58");
                    put("ftentacle3", "2_58");
                    put("ftentacle4", "2_58");
                    put("htentacle", "2_66");

                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void PurposeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Guided by One Purpose
		 * Side: Free Peoples
		 * Culture: moria
		 * Twilight Cost: 0
		 * Type: condition
		 * Subtype: Support Area
		 * Game Text: Each time a tentacle wins a skirmish you may stack it here.
		 * 	Assignment: Spot 4 tentacles here and discard this condition to assign a [moria] creature to the Ring-bearer. The Free Peoples player may exert the Ring-bearer twice to prevent this.
		 */

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl purpose = scn.GetFreepsCard("purpose");

		assertFalse(purpose.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(purpose, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(0, purpose.getBlueprint().getTwilightCost());
		assertEquals(CardType.CONDITION, purpose.getBlueprint().getCardType());
		assertEquals(Culture.MORIA, purpose.getBlueprint().getCulture());
		assertEquals(Side.SHADOW, purpose.getBlueprint().getSide());
	}

	@Test
	public void PurposeStacksMinionsThatWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(merry, aragorn);

		PhysicalCardImpl purpose = scn.GetShadowCard("purpose");
		PhysicalCardImpl ftentacle1 = scn.GetShadowCard("ftentacle1");
		PhysicalCardImpl ftentacle2 = scn.GetShadowCard("ftentacle2");
		scn.ShadowMoveCardToHand(purpose, ftentacle1, ftentacle2);

		scn.StartGame();
		scn.SetTwilight(8);
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(purpose);
		scn.ShadowPlayCard(ftentacle1);
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPlayCard(ftentacle2);
		scn.ShadowDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.ShadowActionAvailable("One Purpose"));
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, ftentacle1}, new PhysicalCardImpl[]{aragorn,ftentacle2});

		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(purpose).size());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetStackedCards(purpose).size());

	}

	@Test
	public void PurposeSpots4StackedTentaclesToAssignToRBIfPreventionDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(merry, aragorn);

		PhysicalCardImpl purpose = scn.GetShadowCard("purpose");
		PhysicalCardImpl htentacle = scn.GetShadowCard("htentacle");
		scn.ShadowMoveCardToHand(htentacle);
		scn.ShadowMoveCardToSupportArea(purpose);
		scn.StackCardsOn(purpose,scn.GetShadowCard("ftentacle1"), scn.GetShadowCard("ftentacle2"), scn.GetShadowCard("ftentacle3"), scn.GetShadowCard("ftentacle4"));

		scn.StartGame();
		scn.SetTwilight(4);
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(htentacle);
		scn.ShadowDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable("One Purpose"));
		scn.ShadowUseCardAction(purpose);

		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssigned(scn.GetRingBearer()));
		//should now see One Purpose and the four tentacles stacked on it in the discard pile
		assertEquals(5, scn.GetShadowDiscardCount());
	}

	@Test
	public void PurposeRBAssignmentCanBePreventedByExertingRBTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var frodo = scn.GetRingBearer();
		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(merry, aragorn);

		PhysicalCardImpl purpose = scn.GetShadowCard("purpose");
		PhysicalCardImpl htentacle = scn.GetShadowCard("htentacle");
		scn.ShadowMoveCardToHand(htentacle);
		scn.ShadowMoveCardToSupportArea(purpose);
		scn.StackCardsOn(purpose,scn.GetShadowCard("ftentacle1"), scn.GetShadowCard("ftentacle2"), scn.GetShadowCard("ftentacle3"), scn.GetShadowCard("ftentacle4"));

		scn.StartGame();
		scn.SetTwilight(4);
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(htentacle);
		scn.ShadowDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable("One Purpose"));
		scn.ShadowUseCardAction(purpose);

		assertEquals(4, scn.GetVitality(frodo));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetVitality(frodo));
		assertFalse(scn.IsCharAssigned(scn.GetRingBearer()));
		//should now see One Purpose and the four tentacles stacked on it in the discard pile
		assertEquals(5, scn.GetShadowDiscardCount());
	}
}
