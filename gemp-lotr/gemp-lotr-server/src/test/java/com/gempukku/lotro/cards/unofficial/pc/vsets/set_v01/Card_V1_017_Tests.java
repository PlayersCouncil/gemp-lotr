
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

public class Card_V1_017_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("twoeyes", "101_17");
					put("gandalf", "101_14");
					put("frodo", "13_149");

					put("filler1", "1_7");
					put("filler2", "1_7");
					put("filler3", "1_7");
					put("filler4", "1_7");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.GimliRB,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TwoEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Two Eyes, as Often\as I Can Spare Them
		* Side: Free Peoples
		* Culture: gandalf
		* Twilight Cost: 3
		* Type: event
		* Subtype: Fellowship
		* Game Text: To play, spot Frodo and Gandalf.
		* 	Draw X cards, where X is Frodo's vitality.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl twoeyes = scn.GetFreepsCard("twoeyes");

		assertFalse(twoeyes.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, twoeyes.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, twoeyes.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, twoeyes.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, twoeyes.getBlueprint().getRace());
        assertTrue(scn.hasTimeword(twoeyes, Timeword.FELLOWSHIP)); // test for keywords as needed
		assertEquals(3, twoeyes.getBlueprint().getTwilightCost());
		//assertEquals(, twoeyes.getBlueprint().getStrength());
		//assertEquals(, twoeyes.getBlueprint().getVitality());
		//assertEquals(, twoeyes.getBlueprint().getResistance());
		//assertEquals(Signet., twoeyes.getBlueprint().getSignet());
		//assertEquals(, twoeyes.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void TwoEyesRequiresBothFrodoAndGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl twoeyes = scn.GetFreepsCard("twoeyes");
		PhysicalCardImpl frodo = scn.GetFreepsCard("frodo");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");

		scn.FreepsMoveCardToHand(twoeyes, frodo, gandalf);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(twoeyes));
		scn.FreepsPlayCard(gandalf);
		assertFalse(scn.FreepsPlayAvailable(twoeyes));
		scn.FreepsPlayCard(frodo);
		assertTrue(scn.FreepsPlayAvailable(twoeyes));
	}

	@Test
	public void TwoEyesDrawsBasedOnFrodosVitality() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl twoeyes = scn.GetFreepsCard("twoeyes");
		PhysicalCardImpl frodo = scn.GetFreepsCard("frodo");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");

		scn.FreepsMoveCharToTable(frodo, gandalf);
		scn.FreepsMoveCardToHand(twoeyes);

		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(twoeyes));
		assertEquals(3, scn.GetVitality(frodo));
		assertEquals(1, scn.GetFreepsHandCount()); //only Two Eyes itself
		assertEquals(4, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(twoeyes);

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
	}
}
