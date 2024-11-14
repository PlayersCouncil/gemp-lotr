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

					put("gandalf", "1_72");
					put("sam", "1_311");
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
		 * Subtype: Response
		 * Game Text: At the start of the Shadow phase, spot 25 twilight tokens and remove 10 to exert each character once (or twice if it costs (4) or more), then remove this from the game. This event may be played from your draw deck or discard pile.
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
	public void FellfromWisdomintoFollyExertsOnceOrTwiceWhenPlayedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardToHand(folly);
		scn.ShadowMoveCharToTable(uruk);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.FreepsMoveCharToTable(sam, gandalf);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(25, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(folly));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(uruk));

		scn.ShadowPlayCard(folly);
		assertEquals(15, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		//Gandalf costs 4 or more, so he gets 2
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(uruk));

		assertEquals(Zone.REMOVED, folly.getZone());
	}

	@Test
	public void FellfromWisdomintoFollyFunctionsWhenPlayedFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardToDiscard(folly);
		scn.ShadowMoveCharToTable(uruk);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.FreepsMoveCharToTable(sam, gandalf);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(25, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(folly));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(uruk));

		scn.ShadowUseCardAction(folly);
		assertEquals(15, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		//Gandalf costs 4 or more, so he gets 2
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(uruk));

		assertEquals(Zone.REMOVED, folly.getZone());
	}

	@Test
	public void FellfromWisdomintoFollyFunctionsWhenPlayedFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var folly = scn.GetShadowCard("folly");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardsToTopOfDeck(folly);
		scn.ShadowMoveCharToTable(uruk);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.FreepsMoveCharToTable(sam, gandalf);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(25, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(folly));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(uruk));

		scn.ShadowUseCardAction(folly);
		assertEquals(15, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		//Gandalf costs 4 or more, so he gets 2
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(uruk));

		assertEquals(Zone.REMOVED, folly.getZone());
	}
}
