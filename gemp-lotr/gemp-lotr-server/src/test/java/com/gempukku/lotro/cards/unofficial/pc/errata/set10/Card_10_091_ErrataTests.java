package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_10_091_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("card", "60_91");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uncomment both @Test markers below once this is ready to be used

	//@Test
	public void MordorFiendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 10
		* Title: Mordor Fiend
		* Side: Free Peoples
		* Culture: Sauron
		* Twilight Cost: 3
		* Type: minion
		* Subtype: Orc
		* Strength: 9
		* Vitality: 3
		* Site Number: 6
		* Game Text: When you play this minion, if you cannot spot another minion and there are 3 or fewer twilight tokens, you may add (3) (or (5) if this minion is roaming).
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
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(6, card.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	//@Test
	public void MordorFiendTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
