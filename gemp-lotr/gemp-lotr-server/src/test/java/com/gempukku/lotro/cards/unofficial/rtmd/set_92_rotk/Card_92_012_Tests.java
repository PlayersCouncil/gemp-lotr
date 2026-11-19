package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_012_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Runner: [moria] Orc minion, strength 5, vitality 1 (its text answers only its own play)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_12", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Runner: [moria] Orc minion, strength 5, vitality 1 (its text answers only its own play)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_12"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_12
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: While you are the Shadow player, skip the archery phase.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_12", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void ArcheryPhaseSkippedWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		// Should skip archery and land in assignment
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryPhaseSkippedOnPlayerTwosTurnWhenOwnedByPlayerOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var runner = scn.GetFreepsCard("runner");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass(); // player 2's fellowship moves
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhaseInverted(Phase.MANEUVER);
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		// Player 2, the Free Peoples player of this turn, is asked first in the assignment phase.
		assertTrue(scn.ShadowDecisionAvailable("Play Assignment action or Pass"));
	}

	@Test
	public void ArcheryPhaseNotSkippedWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		// Archery should NOT be skipped — freeps owns the modifier
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
