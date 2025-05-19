package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_08_102_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("card", "58_102");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uncomment both @Test markers below once this is ready to be used

	//@Test
	public void GreatHillTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 8
		* Title: Great Hill Troll
		* Side: Free Peoples
		* Culture: Sauron
		* Twilight Cost: 9
		* Type: minion
		* Subtype: Troll
		* Strength: 16
		* Vitality: 4
		* Site Number: 5
		* Game Text: <b>Besieger.</b> <b>Fierce.</b> <br>The twilight cost of this minion is -1 for each [Sauron] engine you can spot (limit -5). <br>When you play this minion at any site 5 or higher, you may discard an engine to discard a Free Peoples condition.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");

		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(16, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(5, card.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	//@Test
	public void GreatHillTrollTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(9, scn.GetTwilight());
	}
}
