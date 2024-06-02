package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_350_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("troop", "1_177");
					put("flankers", "2_61");
					put("redeye", "1_266");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DimrillDaleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dimrill Dale
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 6
		 * Game Text: <b>Sanctuary</b>. The twilight cost of the first [moria] Orc played each Shadow phase is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Dimrill Dale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DimrillDaleOnlyDiscountsFirstMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dale = scn.GetShadowSite(6);

		var troop = scn.GetShadowCard("troop");
		var flankers = scn.GetShadowCard("flankers");
		var redeye = scn.GetShadowCard("redeye");
		scn.ShadowMoveCardToHand(troop, redeye);

		scn.StartGame();
		//Start our test on 5 so that moving to 6 is the first thing we do
		scn.SkipToSite(5);

		scn.SetTwilight(11);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(dale, scn.GetCurrentSite());

		//11 manual + 4 from the fellowship moving
		assertEquals(15, scn.GetTwilight());

		scn.ShadowPlayCard(redeye);
		//Orc Chieftain costs 2, should have had 0 discount
		//15 - 2
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(troop);
		//Troop costs 6 (no roaming), discounted by 2 to 4 total
		//13 - 4
		assertEquals(9, scn.GetTwilight());

		scn.ShadowPlayCard(flankers);
		//flankers costs 5, should have no discount
		//9 - 5
		assertEquals(4, scn.GetTwilight());
	}
}
