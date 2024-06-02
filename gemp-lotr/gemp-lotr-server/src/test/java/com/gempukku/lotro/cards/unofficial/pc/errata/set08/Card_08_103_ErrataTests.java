package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_08_103_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<String, String>()
				{{
					put("grond", "58_103");
					put("soldier1", "1_271");
					put("soldier2", "1_271");
					put("soldier3", "1_271");

					put("arwen", "3_7");
					put("elrond", "1_40");
					put("gwemegil", "1_47");
					put("bow", "1_41");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 8
		* Title: *Grond, Hammer of the Underworld
		* Side: Free Peoples
		* Culture: Sauron
		* Twilight Cost: 3
		* Type: possession
		* Subtype: Support Area
		* Game Text: <b>Engine.</b> <b>Shadow:</b> Play a [sauron] minion to add a [sauron] token here.
		* 	 Regroup: Discard a [sauron] minion and remove X [sauron] tokens here to discard a Free Peoples grond (except a companion) with a twilight cost of X. If that grond is not in the support area, discard this possession.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl grond = scn.GetFreepsCard("grond");

		assertTrue(grond.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, grond.getBlueprint().getSide());
		assertEquals(Culture.SAURON, grond.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, grond.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(grond, Keyword.SUPPORT_AREA));
		assertEquals(3, grond.getBlueprint().getTwilightCost());
	}

	@Test
	public void GrondAddsTokensWhenAbilityUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl grond = scn.GetShadowCard("grond");
		PhysicalCardImpl soldier1 = scn.GetShadowCard("soldier1");
		PhysicalCardImpl soldier2 = scn.GetShadowCard("soldier2");
		PhysicalCardImpl soldier3 = scn.GetShadowCard("soldier3");
		scn.ShadowMoveCardToSupportArea(grond);
		scn.ShadowMoveCharToTable(soldier1, soldier2, soldier3);

		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl gwemegil = scn.GetFreepsCard("gwemegil");
		PhysicalCardImpl bow = scn.GetFreepsCard("bow");

		scn.FreepsMoveCardToHand(arwen, elrond, gwemegil, bow);

		scn.StartGame();

		scn.FreepsMoveCharToTable(arwen);
		scn.FreepsMoveCardToSupportArea(elrond);
		scn.FreepsAttachCardsTo(arwen, gwemegil);
		scn.FreepsAttachCardsTo(elrond, bow);

		scn.AddTokensToCard(grond, 10);

		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup, round 1
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertEquals(Zone.ATTACHED, bow.getZone());
		scn.ShadowChoose("1");
		scn.ShadowChooseCard(soldier1);
		//Only one possible card matches the above cost
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertEquals(Zone.DISCARD, bow.getZone());

		//Regroup, round 2
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertEquals(Zone.SUPPORT, elrond.getZone());
		scn.ShadowChoose("4");
		scn.ShadowChooseCard(soldier2);
		//Only one possible card matches the above cost
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertEquals(Zone.DISCARD, elrond.getZone());

		//Regroup, round 3
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		assertEquals(Zone.SUPPORT, grond.getZone());
		assertEquals(Zone.ATTACHED, gwemegil.getZone());
		scn.ShadowChoose("2");
		//scn.ShadowChooseCard(soldier3);
		//Only one possible card matches the above cost
		assertEquals(Zone.DISCARD, grond.getZone());
		assertEquals(Zone.DISCARD, gwemegil.getZone());
	}
}
