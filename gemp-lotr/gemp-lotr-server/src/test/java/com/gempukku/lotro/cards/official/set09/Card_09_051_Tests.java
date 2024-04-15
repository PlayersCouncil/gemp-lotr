package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_051_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "9_51");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GoldberryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Goldberry, River-daughter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: 
		 * Strength: 4
		 * Vitality: 6

		 * Site Number: 2
		 * Game Text: Each time the fellowship moves to a river, heal Goldberry and Tom Bombadil.<br><b>Skirmish:</b> If the fellowship is at a river, exert Goldberry to make a companion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Goldberry", card.getBlueprint().getTitle());
		assertEquals("River-daughter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(6, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getAllyHomeSiteNumbers()[0]);
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getAllyHomeSiteBlock());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GoldberryTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
