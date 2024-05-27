package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_235_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("dontlook", "6_39");
					put("smeagol", "7_71");
					put("boat", "13_48");
					put("slippery", "6_43");
					put("clever", "7_54");
				}},
				new HashMap<>() {{
					put("site1", "11_235");
					put("site2", "18_138");
					put("site3", "18_138");
					put("site4", "18_138");
					put("site5", "18_138");
					put("site6", "18_138");
					put("site7", "18_138");
					put("site8", "18_138");
					put("site9", "18_138");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				"open"
		);
	}

	@Test
	public void DammedGatestreamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dammed Gate-stream
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Marsh</b>. At the start of your fellowship phase, you may play a [gollum] Free Peoples card from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Dammed Gate-stream");

		assertEquals("Dammed Gate-stream", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
		assertEquals(3, card.getBlueprint().getTwilightCost());

	}

	@Test
	public void StartOfFellowshipPhaseActionAllowsPlayFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dontlook = scn.GetFreepsCard("dontlook");
		var smeagol = scn.GetFreepsCard("smeagol");
		var slippery = scn.GetFreepsCard("slippery");
		var boat = scn.GetFreepsCard("boat");
		var clever = scn.GetFreepsCard("clever");
		var site1 = scn.GetFreepsSite("Dammed Gate-stream");

		scn.FreepsChooseCardBPFromSelection(site1);
		scn.SkipStartingFellowships();
		//Apparently it's drawing cards now.  It doesn't do this in other formats.
		scn.FreepsMoveCardsToBottomOfDeck(dontlook, smeagol, slippery, boat, clever);
		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		//Not Listening is a response event, it shouldn't show up
		//Clever Hobbits is a skirmish event, also shouldn't show
		//Smeagol and Don't Look At Them are valid.  Fishing Boat would theoretically be valid if Smeagol were in play.
        assertEquals(2, scn.FreepsGetSelectableCount());

		assertEquals(Zone.DECK, dontlook.getZone());
		assertEquals(Zone.DECK, smeagol.getZone());
		scn.FreepsChooseCardBPFromSelection(dontlook);

		assertEquals(Zone.SUPPORT, dontlook.getZone());
		assertEquals(Zone.DECK, smeagol.getZone());
	}
}
