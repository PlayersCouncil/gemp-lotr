
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

public class Card_V1_046_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("iseeyou", "101_46");
					put("orc", "1_271");
					put("orc2", "1_271");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.GreatRing
		);
	}

	@Test
	public void ISeeYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: I See You
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 0
		* Type: condition
		* Subtype: Support Area
		* Game Text: The site number of each [Sauron] minion is -1 for each burden you can spot.
		* 	Each [sauron] minion with a site number of 1 or less is strength +1.
		* 	 When the Ring-bearer takes off The One Ring, discard this condition.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl iseeyou = scn.GetFreepsCard("iseeyou");

		assertFalse(iseeyou.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, iseeyou.getBlueprint().getSide());
		assertEquals(Culture.SAURON, iseeyou.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, iseeyou.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, iseeyou.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(iseeyou, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(0, iseeyou.getBlueprint().getTwilightCost());
		//assertEquals(, iseeyou.getBlueprint().getStrength());
		//assertEquals(, iseeyou.getBlueprint().getVitality());
		//assertEquals(, iseeyou.getBlueprint().getResistance());
		//assertEquals(Signet., iseeyou.getBlueprint().getSignet());
		//assertEquals(, iseeyou.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void SiteNumberIsReducedPerBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl iseeyou = scn.GetShadowCard("iseeyou");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		PhysicalCardImpl orc2 = scn.GetShadowCard("orc2");
		scn.ShadowMoveCardToSupportArea(iseeyou);
		scn.ShadowMoveCharToTable(orc);
		scn.ShadowMoveCardToHand(orc2);

		scn.StartGame();

		scn.RemoveBurdens(1);
		assertEquals(6, scn.GetMinionSiteNumber(orc));
		scn.AddBurdens(1);
		assertEquals(5, scn.GetMinionSiteNumber(orc));
		scn.AddBurdens(1);
		assertEquals(4, scn.GetMinionSiteNumber(orc));
		scn.AddBurdens(1);
		assertEquals(3, scn.GetMinionSiteNumber(orc));
		scn.AddBurdens(1);
		assertEquals(2, scn.GetMinionSiteNumber(orc));

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetTwilight()); // 1 from comp, 2 from site, not enough to play soldier normally

		assertTrue(scn.ShadowPlayAvailable(orc2)); //home site reduced to 2, doesn't pay roaming and so can play
	}

	@Test
	public void SauronMinionsWithSiteNumber1OrLessAreStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl iseeyou = scn.GetShadowCard("iseeyou");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToSupportArea(iseeyou);
		scn.ShadowMoveCharToTable(orc);

		scn.StartGame();

		scn.RemoveBurdens(1);
		assertEquals(7, scn.GetStrength(orc)); // site 6
		scn.AddBurdens(1);
		assertEquals(7, scn.GetStrength(orc)); // site 5
		scn.AddBurdens(1);
		assertEquals(7, scn.GetStrength(orc)); // site 4
		scn.AddBurdens(1);
		assertEquals(7, scn.GetStrength(orc)); // site 3
		scn.AddBurdens(1);
		assertEquals(7, scn.GetStrength(orc)); // site 2
		scn.AddBurdens(1);
		assertEquals(8, scn.GetStrength(orc)); // site 1, +1 str
		scn.AddBurdens(1);
		assertEquals(8, scn.GetStrength(orc)); // site 0, +1 str
		scn.AddBurdens(1);
		assertEquals(8, scn.GetStrength(orc)); // site -1 (rounds to 0), +1 str

	}

	@Test
	public void DiscardsWhenRingRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl iseeyou = scn.GetShadowCard("iseeyou");
		PhysicalCardImpl orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToSupportArea(iseeyou);
		scn.ShadowMoveCharToTable(orc);

		PhysicalCardImpl ring = scn.GetFreepsCard(GenericCardTestHelper.GreatRing);
		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsChooseAction("The One Ring");

		assertEquals(Zone.SUPPORT, iseeyou.getZone());
		scn.SkipToPhase(Phase.REGROUP); // great ring automatically removes itself
		assertEquals(Zone.DISCARD, iseeyou.getZone());
	}
}
