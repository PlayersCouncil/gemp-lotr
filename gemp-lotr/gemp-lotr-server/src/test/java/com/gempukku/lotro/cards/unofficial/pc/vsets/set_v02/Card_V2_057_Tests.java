package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_057_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("insurance", "102_57");
					put("runner", "1_178");

					put("lurtz", "1_127"); //7
					put("army", "5_70");//7
					put("sauron", "9_48");//18

					put("bill", "3_106");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AttentionoftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Attention of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) to play up to 2 minions from your draw deck costing (5) or more. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("Attention of the Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
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
		assertEquals(4, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
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
	public void FloodInsuranceSpots18TwilightInRegion1ToPlayUpTo2BigMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var lurtz = scn.GetShadowCard("lurtz");
		var army = scn.GetShadowCard("army");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();

		//4 minions, but 1 is a runner and 1 is Sauron, who can't be played with roaming
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(lurtz);
		assertEquals(9, scn.GetTwilight()); // cost 7 + 2 roaming
		assertEquals(Zone.SHADOW_CHARACTERS, lurtz.getZone());

		scn.ShadowDismissRevealedCards();
		assertEquals(1, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(army);
		assertEquals(0, scn.GetTwilight()); // cost 7 + 2 roaming
		assertEquals(Zone.SHADOW_CHARACTERS, army.getZone());
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
		assertEquals(2, scn.GetShadowCardChoiceCount());
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2ToPlayUpTo2BigMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var lurtz = scn.GetShadowCard("lurtz");
		var army = scn.GetShadowCard("army");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		scn.ShadowDismissRevealedCards();
		assertEquals(2, scn.GetShadowCardChoiceCount());

		//Ensure that both minion plays are optional and independent of each other

		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowDeclineChoosing();
		assertEquals(18, scn.GetTwilight());

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.ShadowMoveCardToHand(insurance);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3ToPlayUpTo2BigMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var lurtz = scn.GetShadowCard("lurtz");
		var army = scn.GetShadowCard("army");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		scn.ShadowDismissRevealedCards();
		//4 minions, but 1 is a runner; Sauron is finally not roaming
		assertEquals(3, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(army);
		assertEquals(18, scn.GetTwilight());
		assertEquals(Zone.SHADOW_CHARACTERS, army.getZone());

		scn.ShadowDismissRevealedCards();
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(sauron);
		//Sauron's own game text makes him -1 for the burden
		assertEquals(1, scn.GetTwilight());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var lurtz = scn.GetShadowCard("lurtz");
		var army = scn.GetShadowCard("army");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		scn.ShadowDismissRevealedCards();
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();

		assertEquals(Zone.DISCARD, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(lurtz, army, sauron, runner);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}
}
