package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_345_Tests
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

					put("runner", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "4_337");
					put("site4", "1_343");
					put("site5", "7_345");
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
	public void PelennorFlatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pelennor Flat
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 6
		 * Type: Site
		 * Subtype: 
		 * Site Number: 5K
		 * Game Text: <b>Plains</b>. At the start of the maneuver phase, the Free Peoples player must discard his or her hand or add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Pelennor Flat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.PLAINS));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PelennorFlatFreepsCanChooseToDiscardHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var flat = scn.GetShadowSite(5);

		var fodder01 = scn.GetFreepsCard("fodder01");
		var fodder02 = scn.GetFreepsCard("fodder02");
		var fodder03 = scn.GetFreepsCard("fodder03");
		var fodder04 = scn.GetFreepsCard("fodder04");
		var fodder05 = scn.GetFreepsCard("fodder05");
		var fodder06 = scn.GetFreepsCard("fodder06");
		var fodder07 = scn.GetFreepsCard("fodder07");
		var fodder08 = scn.GetFreepsCard("fodder08");

		scn.FreepsMoveCardToHand(fodder01, fodder02, fodder03, fodder04, fodder05, fodder06, fodder07, fodder08);

		scn.StartGame();
		assertEquals(8, scn.GetFreepsHandCount());

		scn.SkipToSite(4);

		scn.ShadowMoveCharToTable("runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(flat, scn.GetCurrentSite());
		assertEquals(5, (long)scn.GetCurrentSite().getSiteNumber());

		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		var choices = scn.FreepsGetMultipleChoices();
		assertTrue(choices.getFirst().contains("Discard"));
		assertTrue(choices.getLast().contains("burden"));

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.HAND, fodder01.getZone());
		assertEquals(Zone.HAND, fodder02.getZone());
		assertEquals(Zone.HAND, fodder03.getZone());
		assertEquals(Zone.HAND, fodder04.getZone());
		assertEquals(Zone.HAND, fodder05.getZone());
		assertEquals(Zone.HAND, fodder06.getZone());
		assertEquals(Zone.HAND, fodder07.getZone());
		assertEquals(Zone.HAND, fodder08.getZone());

		scn.FreepsChooseMultipleChoiceOption("Discard");

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.DISCARD, fodder01.getZone());
		assertEquals(Zone.DISCARD, fodder02.getZone());
		assertEquals(Zone.DISCARD, fodder03.getZone());
		assertEquals(Zone.DISCARD, fodder04.getZone());
		assertEquals(Zone.DISCARD, fodder05.getZone());
		assertEquals(Zone.DISCARD, fodder06.getZone());
		assertEquals(Zone.DISCARD, fodder07.getZone());
		assertEquals(Zone.DISCARD, fodder08.getZone());
	}

	@Test
	public void PelennorFlatFreepsCanChooseToAdd2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var flat = scn.GetShadowSite(5);

		var fodder01 = scn.GetFreepsCard("fodder01");
		var fodder02 = scn.GetFreepsCard("fodder02");
		var fodder03 = scn.GetFreepsCard("fodder03");
		var fodder04 = scn.GetFreepsCard("fodder04");
		var fodder05 = scn.GetFreepsCard("fodder05");
		var fodder06 = scn.GetFreepsCard("fodder06");
		var fodder07 = scn.GetFreepsCard("fodder07");
		var fodder08 = scn.GetFreepsCard("fodder08");

		scn.FreepsMoveCardToHand(fodder01, fodder02, fodder03, fodder04, fodder05, fodder06, fodder07, fodder08);

		scn.StartGame();
		assertEquals(8, scn.GetFreepsHandCount());

		scn.SkipToSite(4);

		scn.ShadowMoveCharToTable("runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(flat, scn.GetCurrentSite());
		assertEquals(5, (long)scn.GetCurrentSite().getSiteNumber());

		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		var choices = scn.FreepsGetMultipleChoices();
		assertTrue(choices.getFirst().contains("Discard"));
		assertTrue(choices.getLast().contains("burden"));

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.HAND, fodder01.getZone());
		assertEquals(Zone.HAND, fodder02.getZone());
		assertEquals(Zone.HAND, fodder03.getZone());
		assertEquals(Zone.HAND, fodder04.getZone());
		assertEquals(Zone.HAND, fodder05.getZone());
		assertEquals(Zone.HAND, fodder06.getZone());
		assertEquals(Zone.HAND, fodder07.getZone());
		assertEquals(Zone.HAND, fodder08.getZone());

		scn.FreepsChooseMultipleChoiceOption("burden");

		assertEquals(3, scn.GetBurdens());
		assertEquals(Zone.HAND, fodder01.getZone());
		assertEquals(Zone.HAND, fodder02.getZone());
		assertEquals(Zone.HAND, fodder03.getZone());
		assertEquals(Zone.HAND, fodder04.getZone());
		assertEquals(Zone.HAND, fodder05.getZone());
		assertEquals(Zone.HAND, fodder06.getZone());
		assertEquals(Zone.HAND, fodder07.getZone());
		assertEquals(Zone.HAND, fodder08.getZone());
	}
}
