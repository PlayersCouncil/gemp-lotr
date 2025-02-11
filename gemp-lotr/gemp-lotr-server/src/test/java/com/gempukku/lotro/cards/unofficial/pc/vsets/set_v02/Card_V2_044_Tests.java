package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_044_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("insurance", "102_44");
					put("runner", "1_178");

					put("sam", "1_311");
					put("merry", "1_303");
					put("pippin", "2_110");
					put("aragorn", "1_89");
					put("gandalf", "1_72");
					put("legolas", "1_50");
					put("gimli", "1_13");
					put("boromir", "1_97");

					put("bill", "3_106");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ThreatsonAllSidesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Threats on All Sides
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (9) to add up to 6 threats. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("Threats on All Sides", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
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
		assertEquals(9, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
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
	public void FloodInsuranceSpots18TwilightInRegion1ToAddUpTo6Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.FreepsMoveCharToTable("sam", "merry", "pippin", "gandalf", "aragorn",
				"legolas", "gimli", "boromir");

		scn.StartGame();

		scn.SetTwilight(7);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
		scn.ShadowChoose("6");
		assertEquals(9, scn.GetTwilight());
		assertEquals(6, scn.GetThreats());
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

		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2ToAddUpTo6Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
		scn.ShadowChoose("0");

		assertEquals(9, scn.GetTwilight());

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardToHand(insurance);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3ToAddUpTo6Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertEquals(16, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		var item1 = scn.GetShadowCard("item1");
		var item2 = scn.GetShadowCard("item2");
		var item3 = scn.GetShadowCard("item3");
		var item4 = scn.GetShadowCard("item4");
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowPlayCard(insurance);

		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
		scn.ShadowChoose("0");

		assertEquals(Zone.DISCARD, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}
}
