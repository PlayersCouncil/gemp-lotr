package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_334_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "1_313");
					put("toby", "1_305");

					put("filler1", "1_310");
					put("filler2", "1_311");
					put("filler3", "1_312");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_334");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollshawForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Trollshaw Forest
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 1
		 * Type: Site
		 * Subtype: 
		 * Site Number: 2
		 * Game Text: <b>Forest</b>. Each time you play a possession or artifact on your companion, draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Trollshaw Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TrollshawForestDrawsFromAttachedItemsButNotSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var trollshaw = scn.GetShadowSite(2);

		var sting = scn.GetFreepsCard("sting");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(sting, toby);

		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		scn.MoveCardsToTopOfDeck(filler1, filler2, filler3);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals(trollshaw, scn.GetCurrentSite());
		scn.MoveCardsToTopOfDeck(filler1, filler2, filler3);
		assertEquals(3, scn.GetFreepsDeckCount());

		//Playing a card attached to a companion should draw
		scn.FreepsPlayCard(sting);
		assertEquals(2, scn.GetFreepsDeckCount());

		//Playing a support area possession should not
		scn.FreepsPlayCard(toby);
		scn.FreepsDeclineOptionalTrigger(); //toby's own text
		assertEquals(2, scn.GetFreepsDeckCount());
	}
}
