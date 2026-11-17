package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keeper1", "10_86");
					put("keeper2", "10_86");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothKeeperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gorgoroth Keeper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: Besieger.
		 * 	While you control a site, this minion's twilight cost is -2.
		 * 	Regroup: Stack this minion on a site you control.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("keeper1");

		assertEquals("Gorgoroth Keeper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedForFourWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keeper = scn.GetShadowCard("keeper1");

		scn.StartGame();
		scn.SkipToSite(2);
		// The other player controls a site; the Keeper's owner controls none.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(keeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 0 for site 3
		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowPlayAvailable(keeper));

		scn.ShadowPlayCard(keeper);

		// 4 + 2 for roaming: site number 5 at site 3
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, keeper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsTwoLessWhileItsOwnerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keeper = scn.GetShadowCard("keeper1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(keeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 0 for site 3
		assertEquals(11, scn.GetTwilight());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowPlayAvailable(keeper));

		scn.ShadowPlayCard(keeper);

		// 4 - 2 for the site controlled + 2 for roaming
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, keeper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keeper = scn.GetShadowCard("keeper1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsTakeControlOfSite();
		scn.MoveMinionsToTable(keeper);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowActionAvailable(keeper));
		assertInZone(Zone.SHADOW_CHARACTERS, keeper);
	}

	@Test
	public void RegroupActionStacksThisMinionOnTheOneSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keeper = scn.GetShadowCard("keeper1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(keeper);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowActionAvailable(keeper));
		assertInZone(Zone.SHADOW_CHARACTERS, keeper);

		scn.ShadowUseCardAction(keeper);

		assertInZone(Zone.STACKED, keeper);
		assertTrue(scn.GetStackedCards(site1).contains(keeper));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionStacksThisMinionOnEitherSiteItsOwnerControlsAndOnNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keeper = scn.GetShadowCard("keeper1");
		var other = scn.GetShadowCard("keeper2");

		scn.StartGame();
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		// The Keeper's owner controls sites 1 and 3, the other player site 2, and nobody site 4.
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.ShadowTakeControlOfSite(site3);
		scn.MoveMinionsToTable(keeper, other);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertFalse(scn.IsSiteControlled(site4));
		assertTrue(scn.ShadowActionAvailable(keeper));
		assertTrue(scn.ShadowActionAvailable(other));

		scn.ShadowUseCardAction(keeper);

		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site2, site4, scn.GetCurrentSite()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site3);

		// The copy whose action was used is the one stacked; the copy beside it stays where it was.
		assertInZone(Zone.STACKED, keeper);
		assertTrue(scn.GetStackedCards(site3).contains(keeper));
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertInZone(Zone.SHADOW_CHARACTERS, other);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
