package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: FreepsCanAssignAgainst and ShadowCanAssignAgainst, which read whether one character may
 * be assigned to one minion or to several in one answer, and InFierceRound, which tells the fierce assignment and
 * skirmish phases from the first.
 */
public class AssignmentReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");            // Dwarf Guard: strength 4
					// The Balrog, Terror of Flame and Shadow: strength 17.  "The Balrog cannot be assigned to
					// skirmish companions or allies of strength less than 6."  A restriction on a pair, which
					// names no player.
					put("balrog", "6_76");
					put("runner", "1_178");         // Goblin Runner: strength 5
					// Ulaire Toldea, Wraith on Wings: fierce, strength 12.  Its other text is a twilight
					// discount, which does nothing to a minion that is cheated in.
					put("toldea", "7_219");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Runs the call and says whether it was refused: by the rig (a failed check) or by the game (an answer the
	// decision would not take, which the rig throws on).
	private static boolean Refused(Runnable call) {
		try {
			call.run();
		}
		catch (AssertionError | RuntimeException refusal) {
			return true;
		}
		return false;
	}

	@Test
	public void WhetherOneCharacterMayBeAssignedToOneMinionIsReadForEitherPlayerAndAgreesWithTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, runner);
		scn.SkipToAssignments();

		// The restriction is on a pair, so each card by itself may be assigned and the decision lists them all.
		assertTrue(scn.FreepsCanAssign(balrog));
		assertTrue(scn.FreepsCanAssign(guard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(balrog));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(guard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		// Strength 8 may face The Balrog; 4 and 3 may not, and may face another minion.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, balrog));
		assertFalse(scn.FreepsCanAssignAgainst(guard, balrog));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(guard, runner));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, runner));
		// The Balrog's text binds the Shadow player as well.
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, balrog));
		assertFalse(scn.ShadowCanAssignAgainst(guard, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(guard, runner));

		// It is asked of a companion or ally and then a minion, and of nothing else.
		assertTrue(Refused(() -> scn.FreepsCanAssignAgainst(balrog, aragorn)));
		assertTrue(Refused(() -> scn.ShadowCanAssignAgainst(aragorn, guard)));

		// The game agrees: it will not take the pair that reads false, and takes the one that reads true.
		assertTrue(Refused(() -> scn.FreepsAssignToMinions(guard, balrog)));
		assertFalse(scn.IsCharAssigned(balrog));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(balrog));
		scn.FreepsAssignToMinions(aragorn, balrog);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, balrog));

		// Now Aragorn has his one minion and The Balrog its skirmish.  The Free Peoples player could give him no
		// second; the Shadow player may add the unassigned Runner to him, and cannot assign The Balrog again.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, runner));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, runner));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(guard, runner));

		scn.ShadowAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, balrog));
	}

	// Comprehensive Rules 4.2, "assignment phase": the Free Peoples player's assignments are one to one, but for
	// "defender +X" (one more minion for each +1); the Shadow player may add any leftover minions to any companion.
	@Test
	public void SeveralMinionsAgainstOneCharacterAreReadForEitherPlayerAndAgreeWithTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var toldea = scn.GetShadowCard("toldea");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, runner, toldea);
		scn.SkipToPhase(Phase.MANEUVER);

		// "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));

		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		// The Free Peoples player: two minions for the companion with defender +1 and not three; one for the
		// companion without.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, runner, toldea));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, runner, toldea, balrog));
		assertTrue(scn.FreepsCanAssignAgainst(guard, runner));
		assertFalse(scn.FreepsCanAssignAgainst(guard, runner, toldea));
		// The Shadow player has no such count; a restriction on a pair still refuses the group it is in.
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, runner, toldea, balrog));
		assertTrue(scn.ShadowCanAssignAgainst(guard, runner, toldea));
		assertFalse(scn.ShadowCanAssignAgainst(guard, runner, balrog));

		// It is asked of a companion or ally and then of minions: at least one, each named once.
		assertTrue(Refused(() -> scn.FreepsCanAssignAgainst(aragorn)));
		assertTrue(Refused(() -> scn.FreepsCanAssignAgainst(aragorn, runner, runner)));
		assertTrue(Refused(() -> scn.FreepsCanAssignAgainst(aragorn, runner, guard)));

		// The game agrees: it takes neither group that reads false, and takes the one that reads true.
		assertTrue(Refused(() -> scn.FreepsAssignToMinions(guard, runner, toldea)));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(Refused(() -> scn.FreepsAssignToMinions(aragorn, runner, toldea, balrog)));
		assertFalse(scn.IsCharAssigned(aragorn));
		scn.FreepsAssignToMinions(aragorn, runner, toldea);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));

		// Aragorn has his two.  The minions he has are counted, so the Free Peoples player could give him no
		// third (F-0355: this read true); the Shadow player may, and cannot assign a minion that is assigned.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssign(aragorn));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, balrog));
		assertTrue(scn.FreepsCanAssign(guard));
		assertTrue(scn.ShadowCanAssign(aragorn));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, balrog));
		assertFalse(scn.ShadowCanAssignAgainst(aragorn, balrog, runner));

		scn.ShadowAssignToMinions(aragorn, balrog);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, balrog));
	}

	@Test
	public void TheFierceRoundIsToldFromTheFirstThroughItsAssignmentAndSkirmishPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var toldea = scn.GetShadowCard("toldea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		assertFalse(scn.InFierceRound());

		// The first round: the assignment phase and the skirmish.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.InFierceRound());

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, toldea);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());

		// 12 against 8: Aragorn loses and lives, and a fierce minion is left in play.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The phase reads as it did the first time; the round does not.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		assertTrue(scn.InFierceRound());
		scn.FreepsAssignAndResolve(aragorn, toldea);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());

		// When the fierce skirmish is over, so is the round.
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
