
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("rit", "101_40");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingwraithinTwilightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Ringwraith in Twilight
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 4
		* Type: minion
		* Subtype: Nazgul
		* Strength: 9
		* Vitality: 3
		* Site Number: 3
		* Game Text: Twilight.
		* 	This minion is strength +1 for each wound on the Ring-bearer.
		* 	At the start of the maneuver phase, you may exert this minion to make it <b>fierce</b> until the regroup phase. The Free Peoples player may exert the Ring-bearer to prevent this.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl rit = scn.GetFreepsCard("rit");

		assertFalse(rit.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, rit.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, rit.getBlueprint().getCulture());
		assertEquals(CardType.MINION, rit.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, rit.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(rit, Keyword.TWILIGHT)); // test for keywords as needed
		assertEquals(4, rit.getBlueprint().getTwilightCost());
		assertEquals(9, rit.getBlueprint().getStrength());
		assertEquals(3, rit.getBlueprint().getVitality());
		//assertEquals(, rit.getBlueprint().getResistance());
		//assertEquals(Signet., rit.getBlueprint().getSignet());
		assertEquals(3, rit.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void RITIsStrengthPlusOnePerWoundOnTheRB() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl rit = scn.GetShadowCard("rit");
		scn.MoveMinionsToTable(rit);

		scn.StartGame();

		assertEquals(9, scn.GetStrength(rit));
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(10, scn.GetStrength(rit));
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(11, scn.GetStrength(rit));
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(12, scn.GetStrength(rit));
	}

	@Test
	public void ManeuverActionMakesRITFierce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl rit = scn.GetShadowCard("rit");
		scn.MoveMinionsToTable(rit);

		scn.StartGame();

		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();

		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(rit, Keyword.FIERCE));
	}

	@Test
	public void FreepsCanPreventManeuverActionByExertingRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl rit = scn.GetShadowCard("rit");
		scn.MoveMinionsToTable(rit);

		scn.StartGame();

		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertFalse(scn.HasKeyword(rit, Keyword.FIERCE));
	}
}
