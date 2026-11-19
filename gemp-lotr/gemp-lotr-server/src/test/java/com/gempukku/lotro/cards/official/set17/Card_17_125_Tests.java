package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enforcer", "17_125");
					put("enforcer2", "17_125");
					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, cost 4, site 5; hunter 6 is its whole text
				}},
				// Site 2 is Ettenmoors (Shadow number 2), site 3 Rivendell Valley (0), site 4 Balin's Tomb (3).
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandEnforcerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Enforcer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may liberate a site to add (X), where X is the Shadow number of the liberated site.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("enforcer");

		assertEquals("White Hand Enforcer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingItLiberatesTheSiteOfHisOwnHeChoosesToAddItsShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		scn.MoveCardsToHand(enforcer);
		assertInHand(enforcer);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		scn.FreepsPass();

		// The Shadow phase at site 5, where a site 5 minion is not roaming.
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(enforcer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		scn.ShadowAcceptOptionalTrigger();

		// Both of his own sites are offered, and the one he chooses goes back on the path (Ketura's ruling on
		// Q-0370: his choice, never the opponent's site): the lower, Bag End.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(site1);
		assertInZone(Zone.SHADOW_CHARACTERS, enforcer);
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		// 3 to play, then (0) for Bag End's Shadow number; the higher Ettenmoors would have added 2.
		assertEquals(pool - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void XIsTheShadowNumberOfTheSiteLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		scn.MoveCardsToHand(enforcer);
		assertInHand(enforcer);

		scn.StartGame();
		scn.SkipToSite(5);
		var site4 = scn.GetSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(4, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site4));
		scn.FreepsPass();

		// The Shadow phase at site 6.
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(enforcer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(site4);

		assertFalse(scn.IsSiteControlled(site4));
		assertInZone(Zone.ADVENTURE_PATH, site4);
		assertEquals(3, scn.GetShadowControlledSiteCount());
		// 3 to play, then (3) for Balin's Tomb: the pool where it was. Ettenmoors' 2 would read 1 less, a site
		// of Shadow number 0 3 less.
		assertEquals(pool - 3 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesHisSitesAndAnotherMinionPlayedAfterItAnswersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		var enforcer2 = scn.GetShadowCard("enforcer2");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCardsToHand(enforcer, enforcer2, butcher);
		assertInHand(enforcer);
		assertInHand(enforcer2);
		assertInHand(butcher);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(enforcer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		// The 3 to play and nothing more.
		assertEquals(pool - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [uruk-hai] minion played with the Enforcer in play: nothing is offered.
		scn.ShadowPlayCard(butcher);
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowControls(site2));
		assertEquals(pool - 3 - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer, and none from the first.
		scn.ShadowPlayCard(enforcer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enforcer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteControlledNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		scn.MoveCardsToHand(enforcer);
		assertInHand(enforcer);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(enforcer);
		// Nothing to liberate: the cost cannot be paid.
		assertInZone(Zone.SHADOW_CHARACTERS, enforcer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyTheFreePeoplesPlayerControllingASiteNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		scn.MoveCardsToHand(enforcer);
		assertInHand(enforcer);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		// As if taken on one of the other player's turns.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.FreepsPass();

		scn.ShadowPlayCard(enforcer);
		// As built (Q-0370): a bare "liberate a site" on the Free Peoples player's turn never takes his site.
		assertInZone(Zone.SHADOW_CHARACTERS, enforcer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHigherSiteTheFreePeoplesPlayerControlsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enforcer = scn.GetShadowCard("enforcer");
		scn.MoveCardsToHand(enforcer);
		assertInHand(enforcer);

		scn.StartGame();
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(enforcer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enforcer));
		scn.ShadowAcceptOptionalTrigger();

		// His own two sites are offered, and not the Free Peoples player's higher site 3 (Ketura's ruling on
		// Q-0370: no reaching over to liberate the opponent's site).
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3));
		scn.ShadowChooseCard(site2);
		assertTrue(scn.FreepsControls(site3));
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertEquals(pool - 3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
