package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_042_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("insurance", "102_42");
					put("runner", "1_178");

					put("condition1", "1_173");
					put("condition2", "1_175");
					put("condition3", "1_183");
					put("condition4", "1_188");
					put("condition5", "1_192");

					put("fpcondition1", "1_10");
					put("fpcondition2", "1_16");

					put("bill", "3_106");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ValiantbutFoolishStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Valiant but Foolish
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (6) to take up to 4 Shadow conditions into hand from your draw deck. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("Valiant but Foolish", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
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
	public void FloodInsuranceSpots18TwilightInRegion1ToFetch4Conditions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var condition1 = scn.GetShadowCard("condition1");
		var condition2 = scn.GetShadowCard("condition2");
		var condition3 = scn.GetShadowCard("condition3");
		var condition4 = scn.GetShadowCard("condition4");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();
		//8 cards, 7 conditions, but 2 are free peoples
		assertEquals(5, scn.ShadowGetCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(condition1, condition2, condition3, condition4);
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
		assertEquals(5, scn.ShadowGetCardChoiceCount());
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2ToFetch4Conditions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill", "runner");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(12, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertEquals(5, scn.ShadowGetCardChoiceCount());

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3ToFetch4Conditions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill", "runner");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(19, scn.GetTwilight());
		scn.ShadowDismissRevealedCards();
		assertEquals(5, scn.ShadowGetCardChoiceCount());
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var condition1 = scn.GetShadowCard("condition1");
		var condition2 = scn.GetShadowCard("condition2");
		var condition3 = scn.GetShadowCard("condition3");
		var condition4 = scn.GetShadowCard("condition4");
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();
		//8 cards, 7 conditions, but 2 are free peoples
		assertEquals(5, scn.ShadowGetCardChoiceCount());
		scn.ShadowDecisionAvailable("Choose cards from deck");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(condition1, condition2, condition3, condition4);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDeckCount());

		assertEquals(Zone.DISCARD, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("condition1", "condition2", "condition3", "condition4", "condition5",
				"fpcondition1", "fpcondition2", "bill", "runner");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}
}
