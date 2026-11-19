package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_011_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Runner: [moria] Orc minion, strength 5, vitality 1 (its text answers only its own play)
					put("runner", "1_178");
					// Dunlending Brigand: [dunland] Man minion, strength 8, vitality 1, no text
					put("brigand", "4_10");
					// Aragorn, Ranger of the North: companion, strength 8; a maneuver ability only
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_11", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Runner: [moria] Orc minion, strength 5, vitality 1 (its text answers only its own play)
					put("runner", "1_178");
					// Dunlending Brigand: [dunland] Man minion, strength 8, vitality 1, no text
					put("brigand", "4_10");
					// Aragorn, Ranger of the North: companion, strength 8; a maneuver ability only
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_11"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_11
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: If the fellowship has moved more than once this turn, your minions are each strength +1.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_11", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void MinionNotBuffedAfterSingleMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// After a single move, no buff
		assertEquals(1, scn.GetMoveCount());
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void MinionBuffedAfterDoubleMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToMovementDecision();

		// Double move
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// After double move, runner should be strength 6
		assertEquals(2, scn.GetMoveCount());
		assertEquals(6, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachOfYourMinionsIsStrengthPlusOneFromTheSecondMoveAndACompanionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass(); // the fellowship's first move of the turn
		scn.MoveMinionsToTable(runner, brigand);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));

		// Nobody is assigned, so both minions are still there when the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(2, scn.GetMoveCount());
		// An Orc and a Man, of two cultures: each is +1. The Free Peoples player's companion is no minion.
		assertEquals(6, scn.GetStrength(runner));
		assertEquals(9, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnersMinionsAreStrengthPlusOneWhenPlayerTwosFellowshipMovesTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var runner = scn.GetFreepsCard("runner");
		var brigand = scn.GetFreepsCard("brigand");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass(); // player 2's fellowship makes its first move of the turn
		scn.MoveMinionsToTable(runner, brigand);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(brigand));

		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();

		assertEquals(2, scn.GetMoveCount());
		assertEquals(6, scn.GetStrength(runner));
		assertEquals(9, scn.GetStrength(brigand));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void DoesNotApplyWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// When freeps owns the meta-site, "your minions" refers to freeps minions,
		// which don't exist. Shadow's minions should NOT be buffed.
		var scn = GetFreepsScenario();

		var runner = scn.GetShadowCard("runner");
		// The owner's own companion: his, and no minion
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(runner);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToMovementDecision();

		// Double move
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Should NOT be buffed — freeps owns the modifier
		assertEquals(2, scn.GetMoveCount());
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
