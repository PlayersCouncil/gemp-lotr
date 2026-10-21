package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_116_Tests
{

	// Hobbiton Woods is site 1; the rest of the path is that of FellowshipSites (site 2 is Ettenmoors).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitonWoodsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hobbiton Woods
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Forest</b>. <b>Fellowship:</b> Exert a Hobbit to play your site 2 (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Hobbiton Woods", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void FellowshipActionExertsAHobbitToPlayHisOwnSite2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		var woods = scn.GetCurrentSite();
		assertEquals("Hobbiton Woods", woods.getBlueprint().getTitle());
		// Only site 1 is on the adventure path; each player's site 2 is in his adventure deck.
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2, shadowSite2);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(woods));

		scn.FreepsUseCardAction(woods);

		// A Hobbit of either culture, companion or ally, may exert; the companions who are no Hobbits may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// His own site 2 is laid, where a move would have had the Shadow player lay his; the other player's
		// stays in his adventure deck.  Site 3 is still unlaid, and the fellowship has not moved.
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersFirstTurnHisSite2ReplacesTheFirstPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var shadowFrodo = scn.GetShadowRingBearer();
		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		// The first player lays his own Ettenmoors with the Woods on his first turn, and moves to it.
		var woods = scn.GetCurrentSite();
		scn.FreepsUseCardAction(woods);
		// Frodo, his only Hobbit, exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertSame(site2, scn.GetSite(2));

		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship is at the Woods, and the site 2 that lies next is the first player's.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(woods, scn.GetCurrentSite());
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));
		assertTrue(scn.ShadowActionAvailable(woods));

		scn.ShadowUseCardAction(woods);

		// His Frodo, his only Hobbit, exerts.  His own site 2 replaces the first player's, which goes back to
		// its owner's adventure deck.  Both players hold the same nine sites, so it is replaced by its twin.
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, shadowSite2);
		assertInZone(Zone.ADVENTURE_DECK, site2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithHisOwnSite2AlreadyLaidTheHobbitExertsAndNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var site2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		var woods = scn.GetCurrentSite();
		scn.FreepsUseCardAction(woods);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertSame(site2, scn.GetSite(2));

		// The action is used again with his own site 2 lying next: the exertion is paid, and he has no other
		// site 2 to play.
		assertTrue(scn.FreepsActionAvailable(woods));
		scn.FreepsUseCardAction(woods);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Every Hobbit at one vitality; Aragorn and Smeagol, who are no Hobbits, could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(pippin));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertNull(scn.GetSite(2));
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
