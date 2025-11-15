package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("initiate", "103_43");
					// put other cards in here as needed for the test case
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "103_112");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DoomCausewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Doom Causeway
		 * Unique: false
		 * Side: 
		 * Culture: 
		 * Shadow Number: 9
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 9K
		 * Game Text: Mountain.  Each time a card is hindered, discard it.  Each time a character is exerted, wound it.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(9);
		var card = scn.GetFreepsCard("card");

		assertEquals("Doom Causeway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	
	@Test
	public void DoomCausewayDiscardsHinderedCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var causeway = scn.GetShadowSite(9);

		var initiate = scn.GetShadowCard("initiate");
		scn.MoveCardsToHand(initiate);

		scn.StartGame();
		
		scn.SkipToSite(8);
		scn.FreepsPass();

		assertEquals(scn.GetCurrentSite(), causeway);

		assertTrue(scn.ShadowPlayAvailable(initiate));
		scn.ShadowPlayCard(initiate);
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);

		//Desert Wind Initiate has the option to self-hinder when you play it
		scn.ShadowChoose("hinder");

		assertInZone(Zone.DISCARD, initiate);
	}
}
