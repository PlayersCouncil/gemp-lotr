package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_10");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shelob, Terror of Cirith Ungol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Fierce.  Enduring.
		* 	Each time a skirmish involving Shelob ends, you may hinder each character she was skirmishing.
		* 	Skirmish: If Gollum is skrimishing, exert Gollum and Shelob to hinder a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Terror of Cirith Ungol", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ShelobTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		//var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		
		assertFalse(true);
	}
}
