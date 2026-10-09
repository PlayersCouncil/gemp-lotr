package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_025_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: [dwarven] companion, not unique; its only text is what it takes to play it,
					// and it is never played here. Five of them beside Frodo are six companions: the dead pile
					// holds no more threats than there are companions, so five fit and a sixth would show.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_25", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: [dwarven] companion, not unique; its only text is what it takes to play it,
					// and it is never played here. Five of them beside Frodo are six companions: the dead pile
					// holds no more threats than there are companions, so five fit and a sixth would show.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_25"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_25
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each time your fellowship moves during the regroup phase in region 3, add 5
		 * threats.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_25", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void MovingOutOfRegion3AddsFiveThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(7);

		// This turn's fellowship-phase move was site 7 to 8
		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// Departs site 8 (region 3) via the Regroup-phase "another move?" decision.
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore + 5, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOutOfRegion2DoesNotAddThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(5);

		// This turn's fellowship-phase move was site 5 to 6
		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// Departs site 6 (region 2), the last site before region 3, and arrives at site 7: the fellowship
		// moves in the region it is leaving
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipPhaseMoveDoesNotAddThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// The mandatory once-per-turn move (site 7 -> 8) runs automatically inside the ordinary
		// Fellowship phase, not the Regroup phase, so it must not add threats.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupPhaseMoveAfterFirstMoveAddsThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(6);

		// This turn's mandatory Fellowship-phase move (site 6 -> 7) already happened, landing on
		// the Regroup "another move?" decision at site 7 with one move already spent this turn.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// The optional Regroup-phase move (site 7 -> 8) departs a region-3 site during Phase.REGROUP,
		// exactly what the game text describes.
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore + 5, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondRegroupMoveInRegion3AddsFiveThreatsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(6);

		// A third move this turn, as a card that raises the move limit would allow
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		// Site 6 to 7 in the fellowship phase, then the first regroup move: site 7 to 8
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetThreats());

		// Six companions hold six threats: the first five are taken off so that a second five can be read
		scn.RemoveThreats(5);
		assertEquals(0, scn.GetThreats());

		// The second regroup move of the turn: site 8 to 9
		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayerTwosRegroupMoveInRegion3AddsFiveThreatsWhenHeOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		// Player 2's own fellowship: his Frodo and five Dwarf Guards
		scn.MoveCompanionsToTable(
				scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"));

		scn.StartGame();
		// Both players move once a turn on the way: when player 1 has begun his turn at site 6 and ended
		// it, player 2 begins his own at site 6
		scn.SkipToSite(6);
		scn.SkipToOtherPlayersTurn();
		assertEquals(6, scn.GetCurrentSiteNumber());

		// Site 6 to 7 in his fellowship phase
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetThreats());

		// His regroup move departs site 7 (region 3)
		scn.ShadowChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void DoesNotFireWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		scn.MoveCompanionsToTable(
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));

		scn.StartGame();
		scn.SkipToSite(7);

		// This turn's fellowship-phase move was site 7 to 8
		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetCurrentSiteNumber());
		int threatsBefore = scn.GetThreats();

		// The mod is player 2's, and the fellowship that moves is player 1's: not "your fellowship".
		// Six companions could take five threats.
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(threatsBefore, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
