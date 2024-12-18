package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_043_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("insurance", "102_43");
					put("runner", "1_178");

					put("event1", "1_121");
					put("event2", "1_122");
					put("event3", "1_123");
					put("event4", "1_124");
					put("event5", "1_126");

					put("fpevent1", "1_3");
					put("fpevent2", "1_4");

					put("bill", "3_106");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AllHopeofSecrecyLostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: All Hope of Secrecy Lost
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (4) to take up to 4 Shadow events into hand from your draw deck. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("All Hope of Secrecy Lost", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FloodInsuranceRequiresMinionIfFloodThresholdNotMetToPay3ToDraw2Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance, runner);

		scn.StartGame();
		scn.SetTwilight(8);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(insurance));
		scn.ShadowPlayCard(runner);
		assertTrue(scn.ShadowPlayAvailable(insurance));

		assertEquals(10, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(6, scn.GetShadowDeckCount());
	}

	@Test
	public void FloodInsuranceCannotPlayIfLessThan3TwilightWithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCharToTable(runner);

		var frodo = scn.GetRingBearer();
		scn.FreepsAttachCardsTo(frodo, "bill");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetTwilight());
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion1ToFetch4Events() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var event1 = scn.GetShadowCard("event1");
		var event2 = scn.GetShadowCard("event2");
		var event3 = scn.GetShadowCard("event3");
		var event4 = scn.GetShadowCard("event4");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();
		//8 cards, 7 events, but 2 are free peoples
		assertEquals(5, scn.GetShadowCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(event1, event2, event3, event4);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDeckCount());
	}

	@Test
	public void FloodInsuranceSpots18TwilightAndAMinionToOfferBothOptions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(2, scn.ShadowGetMultipleChoices().size());
	}

	@Test
	public void FloodInsuranceOnlyOffersBigAbilityIfPlayingFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		scn.ShadowDismissRevealedCards();
		assertEquals(5, scn.GetShadowCardChoiceCount());
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2ToFetch4Events() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill", "runner");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(14, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertEquals(5, scn.GetShadowCardChoiceCount());

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3ToFetch4Events() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill", "runner");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(21, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertEquals(5, scn.GetShadowCardChoiceCount());
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var event1 = scn.GetShadowCard("event1");
		var event2 = scn.GetShadowCard("event2");
		var event3 = scn.GetShadowCard("event3");
		var event4 = scn.GetShadowCard("event4");
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();
		//8 cards, 7 events, but 2 are free peoples
		assertEquals(5, scn.GetShadowCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(event1, event2, event3, event4);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDeckCount());

		assertEquals(Zone.DISCARD, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("event1", "event2", "event3", "event4", "event5",
				"fpevent1", "fpevent2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}

}
