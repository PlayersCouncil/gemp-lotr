package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_005_Tests
{
	// Player 1 has the modifier.  Each player's place in the league's standings is given as he sits, or null
	// for a player who has none.
	protected VirtualTableScenario GetFreepsScenario(RTMDGameInfo.LeaguePlacement p1Placement, RTMDGameInfo.LeaguePlacement p2Placement) throws CardNotFoundException, DecisionResultInvalidException {
		var placements = new HashMap<String, RTMDGameInfo.LeaguePlacement>();
		if (p1Placement != null)
			placements.put(VirtualTableScenario.P1, p1Placement);
		if (p2Placement != null)
			placements.put(VirtualTableScenario.P2, p2Placement);

		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: fills out the fellowship, no game text in play
					put("gimli", "1_13");       // Gimli, Son of Glóin: never played; he is drawn, so the mover has a hand to reconcile when his turn ends
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				"94_5", null, null,
				placements
		);
	}

	// Player 2 has the modifier.
	protected VirtualTableScenario GetShadowScenario(RTMDGameInfo.LeaguePlacement p1Placement, RTMDGameInfo.LeaguePlacement p2Placement) throws CardNotFoundException, DecisionResultInvalidException {
		var placements = new HashMap<String, RTMDGameInfo.LeaguePlacement>();
		if (p1Placement != null)
			placements.put(VirtualTableScenario.P1, p1Placement);
		if (p2Placement != null)
			placements.put(VirtualTableScenario.P2, p2Placement);

		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: fills out the fellowship, no game text in play
					put("gimli", "1_13");       // Gimli, Son of Glóin: never played; he is drawn, so the mover has a hand to reconcile when his turn ends
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, "94_5", null,
				placements
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_5
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: If your opponent is the Dark Lord or a World Champion, or if they are placed in the top 10%
		 * of the current league, they cannot choose to move during the regroup phase in region 3.
		 */

		var scn = GetFreepsScenario(null, null);

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_5", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void TopTenPercentOpponentCannotChooseToMoveInRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		// Tenth of a hundred in the standings: the top 10% exactly, with no tenth of a place to round. In a league
		// of ten the first place is every percentage from 1 to 10, so only a larger league fixes the number.
		var scn = GetFreepsScenario(null, new RTMDGameInfo.LeaguePlacement(10, 100));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		// Player 2's fellowship has made its fellowship-phase move from site 7 to site 8, both in region 3.
		assertEquals(8, (int) scn.GetCurrentSite().getSiteNumber()); // region 3
		// One move made of the two the move limit allows: it is the modifier that withholds the question.
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.ShadowDecisionAvailable("another move"));
		// The regroup phase has gone on to the mover's reconcile, which follows the question.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
	}

	@Test
	public void OpponentOutsideTheTopTenPercentCanStillChooseToMove() throws DecisionResultInvalidException, CardNotFoundException {
		// Eleventh of a hundred: the nearest place outside the top 10%.
		var scn = GetFreepsScenario(null, new RTMDGameInfo.LeaguePlacement(11, 100));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void UntitledUnplacedOpponentCanStillChooseToMoveInRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		// Neither of the rig's players holds a title, and neither has a place in any standings here.
		var scn = GetFreepsScenario(null, null);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void TopTenPercentOpponentCanStillChooseToMoveOutsideRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario(null, new RTMDGameInfo.LeaguePlacement(1, 10));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(4);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertEquals(5, (int) scn.GetCurrentSite().getSiteNumber()); // region 2
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void OwnerIsNeverBlockedByTheirOwnModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario(new RTMDGameInfo.LeaguePlacement(1, 10), new RTMDGameInfo.LeaguePlacement(1, 10));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void ScopingWhenShadowOwnsModTheOtherPlayerIsTheOneBlocked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(new RTMDGameInfo.LeaguePlacement(1, 10), null);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);

		// The mod's owner is player 2, so it is player 1 who is now "your opponent"
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		// One move made of the two the move limit allows: it is the modifier that withholds the question.
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		// The regroup phase has gone on to the mover's reconcile, which follows the question.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}

	@Test
	public void ScopingWhenShadowOwnsModItsOwnerCanStillMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(new RTMDGameInfo.LeaguePlacement(1, 10), new RTMDGameInfo.LeaguePlacement(1, 10));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void PlacementOnTheModOwnerThemselvesDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(null, new RTMDGameInfo.LeaguePlacement(1, 10));
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToMovementDecision();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
