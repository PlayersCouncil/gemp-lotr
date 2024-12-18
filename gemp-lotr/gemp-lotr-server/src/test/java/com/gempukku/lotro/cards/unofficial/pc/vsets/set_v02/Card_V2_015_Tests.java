package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_015_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("insurance", "102_15");
					put("runner", "1_178");

					put("filler1", "1_181");
					put("filler2", "1_182");
					put("filler3", "1_183");
					put("filler4", "1_184");
					put("filler5", "1_185");
					put("filler6", "1_186");
					put("filler7", "1_187");
					put("filler8", "1_188");
					put("filler9", "1_189");

					put("bill", "3_106");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FloodInsuranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Watching and Waiting
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Spot your minion and remove (3) to draw 2 cards.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (3) to draw 8 cards. If in region 1, this action can be played from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("Watching and Waiting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
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
		assertEquals(10, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(7, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
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
	public void FloodInsuranceSpots18TwilightInRegion1ToDraw8Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
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

		assertEquals(8, scn.GetShadowHandCount());
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2ToDraw8Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardToHand(insurance);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(insurance));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3ToDraw8Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(insurance));

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(22, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.ShadowMoveCardToHand(insurance);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertEquals(Zone.DECK, insurance.getZone());
		assertTrue(scn.ShadowActionAvailable(insurance));

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(12, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(insurance);

		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(Zone.DISCARD, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.ShadowMoveCardsToTopOfDeck(insurance);
		scn.ShadowMoveCardsToTopOfDeck("filler1", "filler2", "filler3", "filler4",
				"filler5", "filler6", "filler7", "filler8", "filler9", "bill");

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}
}
