package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("insurance", "102_5");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RitualOathofEnmityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Ritual Oath of Enmity
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: While you can spot 18 twilight tokens in region 1, this card can be played from your draw deck.
		* 	Shadow: Spot 18 twilight tokens (or 25 if in region 3) and remove (7) to add a [dunland] token here.
		* 	Shadow: Spot a [dunland] token here to take control of 2 sites.  Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurance");

		assertEquals("Ritual Oath of Enmity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion1AndRemoves7ToAddToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.MoveCardsToHand(insurance);

		scn.StartGame();

		scn.SetTwilight(15);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowPlayAvailable(insurance));

		scn.ShadowPlayCard(insurance);
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(insurance));
		assertTrue(scn.ShadowActionAvailable("Remove (7) to add a"));

		scn.ShadowUseCardAction(insurance);
		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(insurance));
	}

	@Test
	public void FloodInsuranceSpots18TwilightAndATokenToOfferBothOptions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.MoveCardsToSupportArea(insurance);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.AddTokensToCard(insurance, 1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(18, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable("Remove (7) to add a"));
		assertTrue(scn.ShadowActionAvailable("Discard this to take control of 2 sites"));
	}

	@Test
	public void FloodInsuranceSpots18TwilightInRegion2AndRemoves7ToAddToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.MoveCardsToSupportArea(insurance);

		scn.StartGame();

		scn.SkipToSite(3);

		scn.SetTwilight(14);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(insurance));
		assertTrue(scn.ShadowActionAvailable("Remove (7) to add a"));

		scn.ShadowUseCardAction(insurance);
		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(insurance));

		//Also ensure that region 3 does not trigger at 18 twilight

		scn.SkipToSite(6);
		scn.SetTwilight(11);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable("Remove (7) to add a"));
	}

	@Test
	public void FloodInsuranceSpots25TwilightInRegion3AndRemoves7ToAddToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.MoveCardsToSupportArea(insurance);

		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetShadowSite(2);
		var site3 = scn.GetShadowSite(3);

		scn.StartGame();

		scn.SkipToSite(6);

		scn.SetTwilight(18);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(25, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(insurance));
		assertTrue(scn.ShadowActionAvailable("Remove (7) to add a"));

		scn.ShadowUseCardAction(insurance);
		assertEquals(18, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(insurance));

		assertFalse(scn.ShadowActionAvailable("Remove (7) to add a"));
	}

	@Test
	public void FloodInsuranceSpotsCultureTokenAndSelfDiscardsToControl2Sites() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");
		scn.MoveCardsToSupportArea(insurance);

		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetShadowSite(2);
		var site3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.AddTokensToCard(insurance, 1);
		scn.SkipToSite(6);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable("Remove (7) to add a"));
		assertTrue(scn.ShadowActionAvailable("Discard this to take control of 2 sites"));

		assertEquals(Zone.SUPPORT, insurance.getZone());
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertFalse(scn.IsSiteControlled(site3));

		scn.ShadowUseCardAction(insurance);
		assertEquals(Zone.DISCARD, insurance.getZone());
		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.IsSiteControlled(site2));
		assertFalse(scn.IsSiteControlled(site3));
	}

	@Test
	public void FloodInsurancePlaysFromDrawDeckInRegion1ButNot2Or3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var insurance = scn.GetShadowCard("insurance");

		scn.StartGame();
		scn.SkipToSite(2);

		scn.SetTwilight(17);
		scn.MoveCardsToTopOfDeck(insurance);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(18, scn.GetTwilight());
		//It's an ability being activated and not a "play", which is only true for cards
		// currently in hand.
		assertTrue(scn.ShadowActionAvailable(insurance));

		scn.ShadowUseCardAction(insurance);

		assertEquals(18, scn.GetTwilight());
		assertEquals(Zone.SUPPORT, insurance.getZone());

		//Also ensure that region 2 does not play from draw deck

		scn.SkipToSite(4);

		scn.SetTwilight(11);
		scn.MoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));

		//Also ensure that region 3 does not play from draw deck

		scn.SkipToSite(6);

		scn.SetTwilight(11);
		scn.MoveCardsToTopOfDeck(insurance);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(insurance));
	}
}
