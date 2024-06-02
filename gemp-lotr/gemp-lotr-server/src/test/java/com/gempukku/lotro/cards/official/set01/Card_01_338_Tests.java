package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_338_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("twk", "1_237");
					put("lemenya", "1_232");
					// put other cards in here as needed for the test case
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FordofBruinenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ford of Bruinen
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3
		 * Game Text: <b>River</b>. <b>Sanctuary</b>. The twilight cost of the first Nazgûl played to Ford of Bruinen each turn is -5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Ford of Bruinen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.RIVER));
		assertTrue(scn.hasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FordofBruinenOnlyDiscountsTheFirstNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ford = scn.GetShadowSite(3);

		var twk = scn.GetShadowCard("twk");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.ShadowMoveCardToHand(twk, lemenya);

		scn.StartGame();
		//Start our test on 2 so that moving to 3 is the first thing we do
		scn.SkipToSite(2);

		scn.SetTwilight(9);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(ford, scn.GetCurrentSite());

		//9 manual + 1 from the fellowship moving
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(twk);
		//TWK costs 8 (no roaming), discounted by 5 to 3 total
		//10 - 3
		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(lemenya);
		//Lemenya costs 4, should have no discount
		//7 - 4
		assertEquals(3, scn.GetTwilight());
	}
}
