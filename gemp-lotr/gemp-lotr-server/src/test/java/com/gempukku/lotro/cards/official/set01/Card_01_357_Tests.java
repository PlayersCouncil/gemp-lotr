package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_357_Tests
{

	// The Fellowship sites, with Brown Lands as site 8 in place of Anduin Banks.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4; his maneuver ability is not used
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: a companion archer (fellowship archery total 1); his bonus is against Nazgûl only
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, cost 3; Archer is its whole text
					put("bowman1", "2_60");     // Goblin Bowman: [moria] Orc archer, cost 2; Archer is its whole text
					put("bowman2", "2_60");
					put("bowman3", "2_60");
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer, cost 4; its play trigger needs a machine
					put("patrol", "1_177");     // Goblin Patrol Troop: a minion with no text, and no archer
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site8", "1_357");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrownLandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Brown Lands
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. For each minion archer at Brown Lands, the minion archery total is +1 (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Brown Lands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtBrownLandsOneMinionArcherMakesTheMinionArcheryTotalTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Brown Lands", scn.GetCurrentSite().getBlueprint().getTitle());

		// 1 for the Marksman's own Archer + 1 for the one minion archer at Brown Lands
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The Free Peoples player places both wounds.
		scn.PassArcheryActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachMinionArcherPlayedAddsOneMoreAndNeitherAMinionWhoIsNoArcherNorACompanionArcherAddsAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var patrol = scn.GetShadowCard("patrol");
		var marksman = scn.GetShadowCard("marksman");
		var crossbow = scn.GetShadowCard("crossbow");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(marksman, crossbow);

		// The move to Brown Lands: (6) for the site and 2 for Frodo and Legolas.
		scn.FreepsPass();
		assertEquals("Brown Lands", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(8, scn.GetTwilight());
		scn.MoveMinionsToTable(patrol);
		scn.ShadowRefreshDecision();

		// A minion who is no archer, and an archer who is no minion: the minion total is 0, and Legolas's 1 is
		// the fellowship's.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A [moria] Orc archer: its own 1 and 1 more.
		scn.ShadowPlayCard(marksman);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// An [isengard] Uruk-hai archer: its own 1 and 1 more again.
		scn.ShadowPlayCard(crossbow);
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlusFourWithAFifthMinionArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		var bowman1 = scn.GetShadowCard("bowman1");
		var bowman2 = scn.GetShadowCard("bowman2");
		var bowman3 = scn.GetShadowCard("bowman3");
		var crossbow = scn.GetShadowCard("crossbow");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(marksman, bowman1, bowman2, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Brown Lands", scn.GetCurrentSite().getBlueprint().getTitle());

		// Four minion archers of two cultures: 4 of their own + 4
		assertEquals(8, scn.GetShadowArcheryTotal());

		// A fifth: 5 of their own + 4, the limit
		scn.MoveMinionsToTable(bowman3);
		assertEquals(9, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromBrownLandsAMinionArcherAddsOnlyItsOwnOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(marksman);

		// At Brown Lands: 1 + 1.
		assertEquals("Brown Lands", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The same archer at site 9, Brown Lands behind the fellowship: only its own 1.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
