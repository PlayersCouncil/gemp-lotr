package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_337_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("fodder01", "5_1");
					put("fodder02", "5_2");
					put("fodder03", "5_3");
					put("fodder04", "5_4");
					put("fodder05", "5_5");
					put("fodder06", "5_6");
					put("fodder07", "5_7");
					put("fodder08", "5_8");
					put("fodder09", "5_9");
					put("fodder10", "5_10");
					put("fodder11", "5_11");
					put("fodder12", "5_12");
					put("fodder13", "5_13");
					put("fodder14", "5_14");
					put("fodder15", "5_15");
					put("fodder16", "5_16");
					put("fodder17", "5_17");
					put("fodder18", "5_18");
					put("fodder19", "5_19");
					put("fodder20", "5_20");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "4_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "101_61");
					put("site7", "3_118");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BarrowsofEdorasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Barrows of Edoras
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3T
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Place your hand beneath your draw deck and draw 4 cards.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsSite(3);
		//var card = scn.GetFreepsCard("barrows");

		assertEquals("Barrows of Edoras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BarrowsofEdorasAbilityPutsCardsOnBottomOfDeckAndDraws4TheFirstTimeItIsActivated() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var barrows = scn.GetShadowSite(3);

		var fodder01 = scn.GetFreepsCard("fodder01");
		var fodder02 = scn.GetFreepsCard("fodder02");
		var fodder03 = scn.GetFreepsCard("fodder03");
		var fodder04 = scn.GetFreepsCard("fodder04");
		var fodder05 = scn.GetFreepsCard("fodder05");
		var fodder06 = scn.GetFreepsCard("fodder06");
		var fodder07 = scn.GetFreepsCard("fodder07");
		var fodder08 = scn.GetFreepsCard("fodder08");
		var fodder09 = scn.GetFreepsCard("fodder09");
		var fodder10 = scn.GetFreepsCard("fodder10");
		var fodder11 = scn.GetFreepsCard("fodder11");
		var fodder12 = scn.GetFreepsCard("fodder12");
		var fodder13 = scn.GetFreepsCard("fodder13");
		var fodder14 = scn.GetFreepsCard("fodder14");
		var fodder15 = scn.GetFreepsCard("fodder15");
		var fodder16 = scn.GetFreepsCard("fodder16");
		var fodder17 = scn.GetFreepsCard("fodder17");
		var fodder18 = scn.GetFreepsCard("fodder18");
		var fodder19 = scn.GetFreepsCard("fodder19");
		var fodder20 = scn.GetFreepsCard("fodder20");

		scn.FreepsMoveCardToHand(fodder01, fodder02, fodder03, fodder04, fodder05, fodder06, fodder07, fodder08);

		scn.StartGame();
		scn.FreepsMoveCardsToTopOfDeck(fodder20, fodder19, fodder18, fodder17, fodder16, fodder15, fodder14, fodder13, fodder12, fodder11, fodder10, fodder09);

		assertEquals(8, scn.GetFreepsHandCount());

		scn.SkipToSite(3);

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(barrows, scn.GetCurrentSite());
		assertEquals(3, (long)scn.GetCurrentSite().getSiteNumber());

		assertTrue(scn.FreepsActionAvailable(barrows));

		scn.FreepsUseCardAction(barrows);

		scn.FreepsChooseCardBPFromSelection(fodder08);
		scn.FreepsChooseCardBPFromSelection(fodder07);
		scn.FreepsChooseCardBPFromSelection(fodder06);
		scn.FreepsChooseCardBPFromSelection(fodder05);
		scn.FreepsChooseCardBPFromSelection(fodder04);
		scn.FreepsChooseCardBPFromSelection(fodder03);
		scn.FreepsChooseCardBPFromSelection(fodder02);
		//No player choice for the last card as Gemp performs a choice of 1 automatically

		assertEquals(scn.GetFromBottomOfFreepsDeck(1), fodder01);
		assertEquals(scn.GetFromBottomOfFreepsDeck(2), fodder02);
		assertEquals(scn.GetFromBottomOfFreepsDeck(3), fodder03);
		assertEquals(scn.GetFromBottomOfFreepsDeck(4), fodder04);
		assertEquals(scn.GetFromBottomOfFreepsDeck(5), fodder05);
		assertEquals(scn.GetFromBottomOfFreepsDeck(6), fodder06);
		assertEquals(scn.GetFromBottomOfFreepsDeck(7), fodder07);
		assertEquals(scn.GetFromBottomOfFreepsDeck(8), fodder08);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(Zone.HAND, fodder09.getZone());
		assertEquals(Zone.HAND, fodder10.getZone());
		assertEquals(Zone.HAND, fodder11.getZone());
		assertEquals(Zone.HAND, fodder12.getZone());

		//Do it again, Sam

		assertTrue(scn.FreepsActionAvailable(barrows));

		scn.FreepsUseCardAction(barrows);

		scn.FreepsChooseCardBPFromSelection(fodder12);
		scn.FreepsChooseCardBPFromSelection(fodder11);
		scn.FreepsChooseCardBPFromSelection(fodder10);
		//No player choice for the last card as Gemp performs a choice of 1 automatically

		assertEquals(scn.GetFromBottomOfFreepsDeck(1), fodder09);
		assertEquals(scn.GetFromBottomOfFreepsDeck(2), fodder10);
		assertEquals(scn.GetFromBottomOfFreepsDeck(3), fodder11);
		assertEquals(scn.GetFromBottomOfFreepsDeck(4), fodder12);

		//Psyche!  Rule of 4 means you can only really do this once
		assertEquals(0, scn.GetFreepsHandCount());

		assertFalse(scn.FreepsActionAvailable(barrows));
	}
}
