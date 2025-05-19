package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_359_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_359");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShoresofNenHithoelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shores of Nen Hithoel
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: 
		 * Site Number: 8
		 * Game Text: <b>River</b>. <b>Shadow:</b> Spot 5 Orc minions to prevent the fellowship from moving again this turn.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(8);
		var card = scn.GetFreepsCard("card");

		assertEquals("Shores of Nen Hithoel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ShoresofNenHithoelTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(9, scn.GetTwilight());
	}
}
