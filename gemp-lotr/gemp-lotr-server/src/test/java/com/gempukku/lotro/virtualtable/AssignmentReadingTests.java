package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: FreepsCanAssignAgainst and ShadowCanAssignAgainst, which read whether one character may
 * be assigned to one minion, and InFierceRound, which tells the fierce assignment and skirmish phases from the
 * first.
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
