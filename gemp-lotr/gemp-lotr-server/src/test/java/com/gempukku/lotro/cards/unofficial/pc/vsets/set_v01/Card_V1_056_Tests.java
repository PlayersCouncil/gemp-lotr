
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_056_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("coming", "101_56");
					put("pippin", "1_306");
					put("merry", "1_302");
					put("gimli", "2_121");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void WereComingTooStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: We’re Coming, Too!
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 2
		* Type: event
		* Subtype: Fellowship
		* Game Text: Spot Merry and Pippin to heal two companions with the Frodo signet.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl coming = scn.GetFreepsCard("coming");

		assertFalse(coming.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, coming.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, coming.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, coming.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, coming.getBlueprint().getRace());
        assertTrue(scn.hasTimeword(coming, Timeword.FELLOWSHIP)); // test for keywords as needed
		assertEquals(2, coming.getBlueprint().getTwilightCost());
		//assertEquals(, coming.getBlueprint().getStrength());
		//assertEquals(, coming.getBlueprint().getVitality());
		//assertEquals(, coming.getBlueprint().getResistance());
		//assertEquals(Signet., coming.getBlueprint().getSignet());
		//assertEquals(, coming.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void WereComingTooTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl coming = scn.GetFreepsCard("coming");
		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl merry = scn.GetFreepsCard("merry");
		PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsMoveCardToHand(coming, merry, pippin);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(frodo, 1);

		assertFalse(scn.FreepsPlayAvailable(coming));
		scn.FreepsPlayCard(merry);
		assertFalse(scn.FreepsPlayAvailable(coming));
		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsPlayAvailable(coming));

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(coming);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

	}
}
