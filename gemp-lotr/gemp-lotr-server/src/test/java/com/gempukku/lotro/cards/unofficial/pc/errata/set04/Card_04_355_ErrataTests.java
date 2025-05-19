package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_355_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "54_355");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CavernEntranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Cavern Entrance
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 7
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 7T
		 * Game Text: At the start of each skirmish, you may exert your character in that skirmish to prevent special abilities being used.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(7);
		var card = scn.GetFreepsCard("card");

		assertEquals("Cavern Entrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void CavernEntranceTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(7, scn.GetTwilight());
	}
}
