
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_051_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("griffo", "101_51");
					put("farmer1", "1_295");
					put("farmer2", "1_295");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GriffoBoffinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Griffo Boffin, Uncouth Fellow
		* Side: Free Peoples
		* Culture: shire
		* Twilight Cost: 1
		* Type: ally
		* Subtype: Hobbit
		* Strength: 3
		* Vitality: 2
		* Site Number: 1
		* Game Text: Each time you play a [shire] ally, you may add (1) to draw a card.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl griffo = scn.GetFreepsCard("griffo");

		assertTrue(griffo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, griffo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, griffo.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, griffo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, griffo.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(griffo, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, griffo.getBlueprint().getTwilightCost());
		assertEquals(3, griffo.getBlueprint().getStrength());
		assertEquals(2, griffo.getBlueprint().getVitality());
		//assertEquals(, griffo.getBlueprint().getResistance());
		//assertEquals(Signet., griffo.getBlueprint().getSignet());
		assertTrue(griffo.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void WhenShireAllyIsPlayedAdd1ToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl griffo = scn.GetFreepsCard("griffo");
		PhysicalCardImpl farmer1 = scn.GetFreepsCard("farmer1");
		PhysicalCardImpl farmer2 = scn.GetFreepsCard("farmer2");
		scn.FreepsMoveCharToTable(griffo);
		scn.FreepsMoveCardToHand(farmer1);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertEquals(Zone.DECK, farmer2.getZone());

		scn.FreepsPlayCard(farmer1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight()); // +1 from playing farmer, +1 from Griffo's ability
		assertEquals(Zone.HAND, farmer2.getZone());

	}
}
