package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_329_Tests
{

	// The Towers path with Western Emyn Muil as its site 1.  Site 2 is Eastfold (a Shadow action that needs 3
	// [isengard] trackers) and site 3 Barrows of Edoras (a Fellowship action used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound by rule, not the Ring-bearer
					put("faramir", "4_117");    // Faramir, Son of Denethor: [gondor] Man companion, printed Ring-bound; a bar on Shadow skirmish actions only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, unbound; a Maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "4_329");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WesternEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Western Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: When the Fellowship moves from Western Emyn Muil, each Ring-bound companion must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Western Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingFromWesternEmynMuilExertsEachRingBoundCompanionAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowFrodo = scn.GetShadowRingBearer();
		scn.MoveCompanionsToTable(sam, faramir, aragorn);

		scn.StartGame();
		assertEquals("Western Emyn Muil", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));

		scn.FreepsPass();

		// The Ring-bearer, Sam (Ring-bound by rule) and Faramir (a Ring-bound Man of [gondor]) exert; Aragorn is
		// unbound, and the other player's Frodo is no companion of the fellowship that moves.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondPlayersMoveFromWesternEmynMuilExertsHisRingBoundCompanionAndNotTheFirstPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var shadowFrodo = scn.GetShadowRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));

		// "The Fellowship" is whichever moves: the second player's from the same site 1.
		scn.ShadowPass();
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void TheMoveOnFromSite2ExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(sam, faramir);

		scn.StartGame();
		scn.FreepsPass();
		// With no minion the turn goes on to the regroup phase at site 2, where the fellowship may move again.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(faramir));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
