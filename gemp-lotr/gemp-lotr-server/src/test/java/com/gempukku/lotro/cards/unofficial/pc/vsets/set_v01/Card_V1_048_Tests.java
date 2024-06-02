
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

public class Card_V1_048_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("troop", "101_48");
					put("guard1", "1_7");
					put("guard2", "1_7");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void OrcAmbushTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Orc Ambush Troop
		* Side: Free Peoples
		* Culture: sauron
		* Twilight Cost: 3
		* Type: minion
		* Subtype: Orc
		* Strength: 8
		* Vitality: 2
		* Site Number: 6
		* Game Text: Tracker.
		* 	While you can spot an exhausted companion, this minion is an <b>archer</b>.
		* 	While you can spot 3 exhausted companions, this minion is strength +3.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl troop = scn.GetFreepsCard("troop");

		assertFalse(troop.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troop.getBlueprint().getSide());
		assertEquals(Culture.SAURON, troop.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troop.getBlueprint().getCardType());
		assertEquals(Race.ORC, troop.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(troop, Keyword.TRACKER)); // test for keywords as needed
		assertEquals(3, troop.getBlueprint().getTwilightCost());
		assertEquals(8, troop.getBlueprint().getStrength());
		assertEquals(2, troop.getBlueprint().getVitality());
		//assertEquals(, troop.getBlueprint().getResistance());
		//assertEquals(Signet., troop.getBlueprint().getSignet());
		assertEquals(6, troop.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void TroopIsArcherIfOneExhaustedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl troop = scn.GetShadowCard("troop");
		scn.ShadowMoveCharToTable(troop);

		scn.StartGame();

		assertFalse(scn.hasKeyword(troop, Keyword.ARCHER));
		scn.AddWoundsToChar(scn.GetRingBearer(), 3);
		assertTrue(scn.hasKeyword(troop, Keyword.ARCHER));
	}

	@Test
	public void TroopIsStrengthPlus3IfThreeExhaustedCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl troop = scn.GetShadowCard("troop");
		scn.ShadowMoveCharToTable(troop);

		PhysicalCardImpl guard1 = scn.GetFreepsCard("guard1");
		PhysicalCardImpl guard2 = scn.GetFreepsCard("guard2");
		scn.FreepsMoveCharToTable(guard1, guard2);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(troop));
		scn.AddWoundsToChar(scn.GetRingBearer(), 3);
		assertEquals(8, scn.GetStrength(troop));
		scn.AddWoundsToChar(guard1, 1);
		assertEquals(8, scn.GetStrength(troop));
		scn.AddWoundsToChar(guard2, 1);
		assertEquals(11, scn.GetStrength(troop));
	}
}
