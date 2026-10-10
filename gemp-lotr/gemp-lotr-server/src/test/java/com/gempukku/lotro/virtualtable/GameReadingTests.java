package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: readings of the game that go by the player and not by whose turn it is.  Each player's
 * burdens whoever has the turn (FreepsGetBurdens, ShadowGetBurdens; GetBurdens() alone is the burdens of the
 * player whose turn it is); who has won and who has lost a game that is over (FreepsHasWon, ShadowHasWon,
 * FreepsHasLost, ShadowHasLost; GameIsFinished() alone says only that it is over); and which player is being asked
 * to choose the next skirmish (FreepsAwaitingSkirmishChoice, ShadowAwaitingSkirmishChoice).  Also what the rig
 * says when a helper that plays a turn out is asked to play on through the turn that ends the game.
 */
public class GameReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // resistance 10
				VirtualTableScenario.RulingRing
		);
	}

	// Player 1 bids 4 burdens and player 2 bids 2; player 1, the higher bidder, goes first.
	protected VirtualTableScenario GetBidScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // a companion in the draw deck, so that a starting fellowship is asked for
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null,
				scn -> {
					scn.FreepsChoose("4");
					scn.ShadowChoose("2");
					scn.FreepsChoose("Go first");
				}
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void EachPlayersBurdensAreReadWhoeverHasTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		// On player 1's turn the burdens the rig lays and reads are player 1's.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertEquals(2, scn.GetBurdens(scn.P1()));
		assertEquals(0, scn.GetBurdens(scn.P2()));

		// On player 2's turn they are player 2's; player 1's are still there to be read.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.ShadowGetBurdens());

		// And back: each player has kept his own.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(3, scn.ShadowGetBurdens());

		// A name that is nobody's at the table is refused, not read as no burdens.
		assertRefused(() -> scn.GetBurdens("Nobody"), "GetBurdens was asked for the burdens of 'Nobody', who is not a player at this table.");
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachPlayersBidIsReadAsHisBurdensBeforeTheGameStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBidScenario();

		// Set-up has reached player 1's starting fellowship: each bid is on its own player's Ring-bearer.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(4, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
	}

	@Test
	public void NobodyHasWonOrLostWhileTheGameIsGoingOnAndTheCorruptedPlayerHasLostWhenItIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		assertFalse(scn.GameIsFinished());
		assertFalse(scn.FreepsHasWon());
		assertFalse(scn.FreepsHasLost());
		assertFalse(scn.ShadowHasWon());
		assertFalse(scn.ShadowHasLost());

		// Ten burdens against Frodo's resistance of 10: the game sees it at its next step.
		scn.AddBurdens(10);
		assertFalse(scn.GameIsFinished());
		assertFalse(scn.FreepsHasLost());
		scn.FreepsPass();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.ShadowHasWon());
		assertFalse(scn.FreepsHasWon());
		assertFalse(scn.ShadowHasLost());
		assertTrue(scn.PlayerHasLost(scn.P1()));
		assertTrue(scn.PlayerHasWon(scn.P2()));
		assertRefused(() -> scn.PlayerHasWon("Nobody"), "PlayerHasWon was asked about 'Nobody', who is not a player at this table.");
		assertRefused(() -> scn.PlayerHasLost("Nobody"), "PlayerHasLost was asked about 'Nobody', who is not a player at this table.");
	}

	@Test
	public void WinningAndLosingGoByThePlayerAndNotByTheSideHeHasThatTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();

		// Player 2 is the Free Peoples player of this turn, and it is his Ring-bearer who is corrupted.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.AddBurdens(10);
		scn.ShadowPass();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.ShadowHasLost());
		assertTrue(scn.FreepsHasWon());
		assertFalse(scn.ShadowHasWon());
		assertFalse(scn.FreepsHasLost());
	}

	@Test
	public void TheFellowshipThatLivesThroughSiteNineHasWonAndPlayingOnPastTheEndOfTheGameIsRefusedSayingSo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The turn that is played out here takes the fellowship to site 9 and through it: the game ends in the
		// regroup phase with nobody asked anything, and the helper that was playing on says why it stopped.
		assertRefused(scn::SkipToOtherPlayersTurn,
				"Nobody is being asked anything",
				"the game is over (" + scn.P1() + " has won)",
				"cannot play on through the turn that ends the game");

		assertTrue(scn.GameIsFinished());
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasWon());
		assertTrue(scn.ShadowHasLost());
		assertFalse(scn.FreepsHasLost());
		assertFalse(scn.ShadowHasWon());

		// Any later pass says the same.
		assertRefused(scn::PassCurrentPhaseActions, "Nobody is being asked anything", "the game is over");
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void TheChoiceOfTheNextSkirmishIsReadForThePlayerWhoseFellowshipIsFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Not while the assignment is still to be made
		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.ShadowAwaitingSkirmishChoice());

		// With every minion assigned, player 1 is asked, though there is only the one skirmish to choose
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.ShadowAwaitingSkirmishChoice());
		assertTrue(scn.AwaitingSkirmishChoice(scn.P1()));
		assertFalse(scn.AwaitingSkirmishChoice(scn.P2()));

		// Once it is chosen the skirmish actions are what is asked
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 kills the Runner; the turn is played out and player 2's fellowship moves
		scn.PassSkirmishActions();
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		// On player 2's turn it is player 2 who is asked: his Frodo against player 1's Runner
		var shadowFrodo = scn.GetShadowRingBearer();
		var freepsRunner = scn.GetFreepsCard("runner");
		scn.MoveMinionsToTable(freepsRunner);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowAwaitingSkirmishChoice());
		scn.ShadowAssignToMinions(shadowFrodo, freepsRunner);

		assertTrue(scn.ShadowAwaitingSkirmishChoice());
		assertFalse(scn.FreepsAwaitingSkirmishChoice());
	}
}
