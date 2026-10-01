package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Card_94_005_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: fills out the fellowship, no game text in play
		put("gimli", "1_13"); // Gimli, Son of Gloin: second companion, keeps the deck from being sites-only
	}};

	private static final RTMDGameInfo.LeaguePlacement TOP_OF_TEN = new RTMDGameInfo.LeaguePlacement(1, 10);
	private static final RTMDGameInfo.LeaguePlacement MIDDLE_OF_TEN = new RTMDGameInfo.LeaguePlacement(5, 10);

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetFreepsScenario(null, null);
	}

	protected VirtualTableScenario GetFreepsScenario(RTMDGameInfo.LeaguePlacement p1Placement,
			RTMDGameInfo.LeaguePlacement p2Placement) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				"94_5", null, null,
				Placements(p1Placement, p2Placement)
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetShadowScenario(null, null);
	}

	protected VirtualTableScenario GetShadowScenario(RTMDGameInfo.LeaguePlacement p1Placement,
			RTMDGameInfo.LeaguePlacement p2Placement) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, "94_5", null,
				Placements(p1Placement, p2Placement)
		);
	}

	private static Map<String, RTMDGameInfo.LeaguePlacement> Placements(RTMDGameInfo.LeaguePlacement p1Placement,
			RTMDGameInfo.LeaguePlacement p2Placement) {
		var placements = new HashMap<String, RTMDGameInfo.LeaguePlacement>();
		if (p1Placement != null)
			placements.put(VirtualTableScenario.P1, p1Placement);
		if (p2Placement != null)
			placements.put(VirtualTableScenario.P2, p2Placement);
		return placements.isEmpty() ? null : placements;
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_5
		 * Type: MetaSite
		 * Game Text: If your opponent is the Dark Lord or a World Champion, or if they are placed
		 * in the top 10% of the current league, they cannot choose to move during the regroup
		 * phase in region 3.
		 */

		assertEquals("Race Text 94_5", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void TopTenPercentOpponentCannotChooseToMoveInRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario(null, TOP_OF_TEN);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertEquals(8, (int) scn.GetCurrentSite().getSiteNumber()); // region 3
		assertFalse(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void OpponentOutsideTheTopTenPercentCanStillChooseToMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario(null, MIDDLE_OF_TEN);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void UntitledUnplacedOpponentCanStillChooseToMoveInRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		// The two PlayerIs name clauses list accounts the test players are not, so all three clauses are false here.
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void TopTenPercentOpponentCanStillChooseToMoveOutsideRegionThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario(null, TOP_OF_TEN);
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
		var scn = GetFreepsScenario(TOP_OF_TEN, TOP_OF_TEN);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToMovementDecision();

		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void ScopingWhenShadowOwnsModTheOtherPlayerIsTheOneBlocked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(TOP_OF_TEN, null);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);

		// The mod's owner is player 2, so it is player 1 who is now "your opponent"
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void ScopingWhenShadowOwnsModItsOwnerCanStillMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(TOP_OF_TEN, TOP_OF_TEN);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();

		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void PlacementOnTheModOwnerThemselvesDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario(null, TOP_OF_TEN);
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToMovementDecision();

		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
