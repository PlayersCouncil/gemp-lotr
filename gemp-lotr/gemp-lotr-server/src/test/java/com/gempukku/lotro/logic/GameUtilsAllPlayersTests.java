package com.gempukku.lotro.logic;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of GameUtils.getAllPlayers: a piece of the server, and no behaviour of any card.
 *
 * The list is the one that an effect "for each player" walks (the definitions' ForEachPlayer), so its order is the
 * order in which the players are asked and its length is how many of them are: every player once, beginning with
 * the player whose turn it is.  (From there it goes round counter-clockwise, as the rules have the Shadow players
 * act: Comprehensive Rules 4.2, "assignment phase", "The first Shadow player on your right ... and so on,
 * counter-clockwise."  Two players cannot show a direction.)
 *
 * The method takes a game, and the logic module, where GameUtils and its other test are, has no card library to
 * build one from.  So this test is here, on the test rig, and hands the rig's game to the method: that is the one
 * thing it reaches past the rig for.
 */
public class GameUtilsAllPlayersTests
{
	// No card is named: the list is about the players, and two fellowships of a Ring-bearer alone play their
	// turns out with nothing to stop for.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllPlayersAreListedOnceStartingWithThePlayerWhoseTurnItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.StartGame();
		var game = scn.game(); // lint-ok: the method under test takes the game, and no rig method reads this list

		// Player 1's turn.  Here the list is also the seating order, which is all the old test read.
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertArrayEquals(new String[] { scn.P1(), scn.P2() }, GameUtils.getAllPlayers(game));

		// Player 2's turn: he is first now, which a list kept in seating order would not show.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertArrayEquals(new String[] { scn.P2(), scn.P1() }, GameUtils.getAllPlayers(game));

		// And player 1's again: the list follows the turn and is not turned about once and left.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertArrayEquals(new String[] { scn.P1(), scn.P2() }, GameUtils.getAllPlayers(game));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
