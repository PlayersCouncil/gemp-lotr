package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath1", "102_5");
					put("oath2", "102_5");
					put("oath3", "102_5");

					// Constantly Threatening: a Shadow condition of cost 1 for the support area, played to take one
					// token out of the pool. Its own ability needs the fellowship to have moved twice in the turn.
					put("threat", "4_6");
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
		 * Subtype: Support Area
		 * Game Text: While you can spot 18 twilight tokens in region 1, this card can be played from your draw deck.
		 * 	Shadow: Spot 18 twilight tokens (or 25 if in regions 2 or 3) and remove (7) to add a [dunland] token here.
		 * 	Shadow: Spot a [dunland] token here to take control of 2 sites.  Discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("oath1");

		assertEquals("Ritual Oath of Enmity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForNothingToTheSupportAreaAndUsedWithExactly18TwilightTokensAtSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		scn.MoveCardsToHand(oath1);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 15 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.HAND, oath1);
		assertTrue(scn.ShadowPlayAvailable(oath1));
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(oath1);

		assertInZone(Zone.SUPPORT, oath1);
		// It costs nothing.
		assertEquals(18, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(oath1));
		// Nobody looked through the draw deck.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));

		scn.ShadowUseCardAction(oath1, "Remove (7)");

		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(oath1));
		assertFalse(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void With18TwilightTokensAtSite3ItCanBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		var oath2 = scn.GetShadowCard("oath2");
		var oath3 = scn.GetShadowCard("oath3");
		var threat = scn.GetShadowCard("threat");

		scn.StartGame();
		scn.SkipToSite(2);
		// The turn skipped drew the Shadow player's small deck into his hand, so the cards are placed now.
		scn.MoveCardsToTopOfDeck(oath1, oath2);
		scn.MoveCardsToDiscard(oath3);
		scn.MoveCardsToHand(threat);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// Site 3 is the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 17 set + 1 for Frodo + 0 for the shadow number of site 3
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.DECK, oath1, oath2);
		assertInZone(Zone.DISCARD, oath3);
		assertTrue(scn.ShadowActionAvailable(oath1));
		assertTrue(scn.ShadowActionAvailable(oath2));
		// The draw deck, and not the discard pile
		assertFalse(scn.ShadowActionAvailable(oath3));
		assertFalse(scn.ShadowPlayAvailable(oath3));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(oath1);

		assertInZone(Zone.SUPPORT, oath1);
		// It costs nothing.
		assertEquals(18, scn.GetTwilight());
		// The draw deck was looked through for it, so it is shuffled afterwards.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		// The card is not unique: with 18 still in the pool the second copy can follow the first.
		assertInZone(Zone.DECK, oath2);
		assertTrue(scn.ShadowActionAvailable(oath2));

		scn.ShadowPlayCard(threat);
		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(oath2));
		assertInZone(Zone.DECK, oath2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite3Spots18TwilightTokensAndRemoves7ToAddADunlandTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		var oath2 = scn.GetShadowCard("oath2");
		var threat = scn.GetShadowCard("threat");

		scn.StartGame();
		scn.SkipToSite(2);
		// The turn skipped drew the Shadow player's small deck into his hand, so the cards are placed now.
		scn.MoveCardsToSupportArea(oath1, oath2);
		scn.MoveCardsToHand(threat);
		scn.SetTwilight(24);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// Site 3 is the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 24 set + 1 for Frodo + 0 for the shadow number of site 3
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, oath1, oath2);
		assertEquals(0, scn.GetCultureTokensOn(oath1));
		assertEquals(0, scn.GetCultureTokensOn(oath2));
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertTrue(scn.ShadowActionAvailable(oath2, "Remove (7)"));
		// With no token on it, neither copy offers its second ability.
		assertFalse(scn.ShadowActionAvailable(oath1, "take control"));
		assertFalse(scn.ShadowActionAvailable(oath2, "take control"));

		scn.ShadowUseCardAction(oath1, "Remove (7)");

		assertEquals(18, scn.GetTwilight());
		// The token is on the copy that was used.
		assertEquals(1, scn.GetCultureTokensOn(oath1));
		assertEquals(0, scn.GetCultureTokensOn(oath2));
		// Exactly 18 is enough in region 1.
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertTrue(scn.ShadowActionAvailable(oath2, "Remove (7)"));
		// The copy with the token now offers both of its abilities.
		assertTrue(scn.ShadowActionAvailable(oath1, "take control"));

		scn.ShadowPlayCard(threat);
		// 17 would pay the 7, but 18 cannot be spotted.
		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertFalse(scn.ShadowActionAvailable(oath2, "Remove (7)"));

		// The token is "here": only the copy bearing it offers the second ability.
		assertTrue(scn.ShadowActionAvailable(oath1, "take control"));
		assertFalse(scn.ShadowActionAvailable(oath2, "take control"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite4Spots25TwilightTokensAndCannotBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		var oath2 = scn.GetShadowCard("oath2");
		var threat = scn.GetShadowCard("threat");

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns skipped drew the Shadow player's small deck into his hand, so the cards are placed now.
		scn.MoveCardsToSupportArea(oath1);
		scn.MoveCardsToTopOfDeck(oath2);
		scn.MoveCardsToHand(threat);
		scn.SetTwilight(28);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Site 4 is the first site of region 2.
		// 28 set + 1 for Frodo + 3 for the shadow number of site 4
		assertEquals(32, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, oath1);
		assertInZone(Zone.DECK, oath2);
		assertEquals(0, scn.GetCultureTokensOn(oath1));
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		// 18 and more, but not in region 1
		assertFalse(scn.ShadowActionAvailable(oath2));

		scn.ShadowUseCardAction(oath1, "Remove (7)");

		assertEquals(25, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(oath1));
		// Exactly 25 is enough in region 2.
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertFalse(scn.ShadowActionAvailable(oath2));

		scn.ShadowPlayCard(threat);
		// 24 would do in region 1.
		assertEquals(24, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertFalse(scn.ShadowActionAvailable(oath2));
		assertInZone(Zone.DECK, oath2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7Spots25TwilightTokensAndCannotBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		var oath2 = scn.GetShadowCard("oath2");
		var threat = scn.GetShadowCard("threat");

		scn.StartGame();
		scn.SkipToSite(6);
		// The turns skipped drew the Shadow player's small deck into his hand, so the cards are placed now.
		scn.MoveCardsToSupportArea(oath1);
		scn.MoveCardsToTopOfDeck(oath2);
		scn.MoveCardsToHand(threat);
		scn.SetTwilight(25);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Site 7 is the first site of region 3.
		// 25 set + 1 for Frodo + 6 for the shadow number of site 7
		assertEquals(32, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, oath1);
		assertInZone(Zone.DECK, oath2);
		assertEquals(0, scn.GetCultureTokensOn(oath1));
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		// 18 and more, but not in region 1
		assertFalse(scn.ShadowActionAvailable(oath2));

		scn.ShadowUseCardAction(oath1, "Remove (7)");

		assertEquals(25, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(oath1));
		// Exactly 25 is enough in region 3.
		assertTrue(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertFalse(scn.ShadowActionAvailable(oath2));

		scn.ShadowPlayCard(threat);
		// 24 would do in region 1.
		assertEquals(24, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(oath1, "Remove (7)"));
		assertFalse(scn.ShadowActionAvailable(oath2));
		assertInZone(Zone.DECK, oath2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpotsADunlandTokenHereToTakeControlOfTheTwoLowestSitesAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		var oath2 = scn.GetShadowCard("oath2");
		var oath3 = scn.GetShadowCard("oath3");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(oath1, oath2, oath3);
		scn.AddTokensToCard(oath1, 1);
		scn.AddTokensToCard(oath2, 1);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// The fellowship is at site 5 and the other player's marker at site 4: sites 1, 2 and 3 are below both.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertFalse(scn.IsSiteControlled(site3));
		assertInZone(Zone.SUPPORT, oath1, oath2, oath3);
		assertTrue(scn.ShadowActionAvailable(oath1, "take control"));
		assertTrue(scn.ShadowActionAvailable(oath2, "take control"));
		// The copy with no token on it
		assertFalse(scn.ShadowActionAvailable(oath3, "take control"));

		scn.ShadowUseCardAction(oath1, "take control");

		// Two sites, the lowest two, and no more
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertFalse(scn.IsSiteControlled(site3));
		assertInZone(Zone.DISCARD, oath1);
		// The other copies stay, and the one with a token keeps it.
		assertInZone(Zone.SUPPORT, oath2, oath3);
		assertEquals(1, scn.GetCultureTokensOn(oath2));
		assertTrue(scn.ShadowActionAvailable(oath2, "take control"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneSiteBelowBothPlayersItTakesThatOneAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(oath1);
		scn.AddTokensToCard(oath1, 1);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// The fellowship is at site 3 and the other player's marker at site 2: only site 1 is below both.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SUPPORT, oath1);
		assertTrue(scn.ShadowActionAvailable(oath1, "take control"));

		scn.ShadowUseCardAction(oath1, "take control");

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.DISCARD, oath1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteBelowBothPlayersItIsStillOfferedTakesNothingAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath1 = scn.GetShadowCard("oath1");
		scn.MoveCardsToSupportArea(oath1);

		scn.StartGame();
		scn.AddTokensToCard(oath1, 1);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// The fellowship is at site 2 and the other player's marker still at site 1.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SUPPORT, oath1);
		assertEquals(1, scn.GetCultureTokensOn(oath1));
		assertTrue(scn.ShadowActionAvailable(oath1, "take control"));

		scn.ShadowUseCardAction(oath1, "take control");

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.DISCARD, oath1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
