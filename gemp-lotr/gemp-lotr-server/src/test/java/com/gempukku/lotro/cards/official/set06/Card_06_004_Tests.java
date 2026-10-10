package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "6_4");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("headman", "6_4");
					put("brigand1", "4_10");    // Dunlending Brigand: [dunland] Man with no text; the two other Men
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingHeadmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dunlending Headman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play this minion, you may spot 2 other [dunland] Men to take control of a site. The Free Peoples player may discard 2 cards from hand to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dunlending Headman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledPlayingTheHeadmanOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var headman = scn.GetShadowCard("headman");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.ShadowPlayCard(headman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(headman));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player holds the cards the turns before dealt him, and declines to discard 2 of them.
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand to prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledPlayingTheHeadmanOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var headman = scn.GetShadowCard("headman");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));

		scn.ShadowPlayCard(headman);
		// The Headman is in play beside 2 other [dunland] Men.
		assertInZone(Zone.SHADOW_CHARACTERS, headman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(headman));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
