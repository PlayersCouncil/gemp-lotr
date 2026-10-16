package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_234_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not the Ring-bearer, vitality 4; a maneuver ability never used
					put("sam", "1_311");        // Sam, Son of Hamfast: a second companion, vitality 4; a fellowship ability never used
					put("bounder", "1_286");    // Bounder: a [shire] ally, vitality 2: a Free Peoples character who is no companion; a skirmish ability never used
					put("guard", "10_81");      // Cirith Ungol Guard: a minion of vitality 2 whose whole text is Damage +1
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with the Crags in place of site 9, which no test reaches: the Crags are
					// put on the path as the next site wherever a test wants them, and take that site's number.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("crags", "11_234");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CragsofEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Crags of Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Mountain. If this site is in region 2 when the fellowship moves from it during the regroup
		 * 	phase, wound each companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("crags");

		assertEquals("Crags of Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void MovingFromTheCragsAtSiteFourInTheRegroupPhaseWoundsEachCompanionAndNoAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		// The Shadow player's copy, as the next site is his to play.
		var crags = scn.GetShadowSite("crags");
		scn.MoveCardToAdventurePath(crags);

		// The fellowship-phase move TO the Crags wounds nobody.
		scn.FreepsPass();
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		// A minion that is still in play in the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(guard);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(scn.GetShadowRingBearer()));

		scn.FreepsChooseToMove();
		// Site 4 is in region 2: one wound on each companion, the Ring-bearer among them.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(guard));
		// The other player's Ring-bearer is no companion of the fellowship that moved.
		assertEquals(0, scn.GetWoundsOn(scn.GetShadowRingBearer()));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromTheCragsAtSiteThreeInTheRegroupPhaseWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(aragorn);
		var crags = scn.GetShadowSite("crags");
		scn.MoveCardToAdventurePath(crags);
		scn.FreepsPass();
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseToMove();
		// Site 3 is the last site of region 1.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromTheCragsAtSiteSevenInTheRegroupPhaseWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(aragorn);
		var crags = scn.GetShadowSite("crags");
		scn.MoveCardToAdventurePath(crags);
		scn.FreepsPass();
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseToMove();
		// Site 7 is the first site of region 3.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheCragsInTheRegroupPhaseOrFromThemInTheFellowshipPhaseWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		var crags = scn.GetShadowSite("crags");
		scn.MoveCardToAdventurePath(crags);

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The regroup move TO the Crags, at site 4.
		scn.FreepsChooseToMove();
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The fellowship-phase move FROM the Crags, in region 2.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
