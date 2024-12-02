package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_022_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("folly", "102_22");
					put("uruk", "1_151");

					put("item1", "1_160");
					put("item2", "3_67");
					put("item3", "4_142");
					put("item4", "4_166");
					put("item5", "5_59");

					put("fpitem1", "1_31");
					put("fpitem2", "3_23");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FellfromWisdomintoFollyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Fell from Wisdom into Folly
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (5) to take up to 4 Shadow items into hand from your draw deck. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("folly");

		assertEquals("Fell from Wisdom into Folly", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellfromWisdomintoFollySmallAbilitySpotsMinionAndRemoves3ToDraw2Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardToHand(folly);
		scn.ShadowMoveCharToTable(uruk);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(3, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(folly));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(folly);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDeckCount());

		assertEquals(Zone.DISCARD, folly.getZone());
	}

	@Test
	public void FellfromWisdomintoFollyTutors4ShadowItemsFromDrawDeckInRegion1If18Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var item1 = scn.GetShadowCard("item1");
		var item2 = scn.GetShadowCard("item2");
		var item3 = scn.GetShadowCard("item3");
		var item4 = scn.GetShadowCard("item4");
		scn.ShadowMoveCardToHand(folly);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(folly));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(folly);
		//8 cards, 7 items, but 2 are free peoples
		assertEquals(5, scn.GetShadowCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(item1, item2, item3, item4);
		scn.ShadowDismissRevealedCards();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
	}

	@Test
	public void FellfromWisdomintoFollyFunctionsWhenPlayedFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var item1 = scn.GetShadowCard("item1");
		var item2 = scn.GetShadowCard("item2");
		var item3 = scn.GetShadowCard("item3");
		var item4 = scn.GetShadowCard("item4");
		scn.ShadowMoveCardsToTopOfDeck(folly);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(folly));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(9, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(folly);
		//8 cards, 7 items, but 2 are free peoples
		assertEquals(5, scn.GetShadowCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(item1, item2, item3, item4);
		scn.ShadowDismissRevealedCards();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
	}
}
