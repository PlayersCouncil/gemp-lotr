package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: the reading of the game log, which is how a test sees a card that the game makes known
 * by a line in the log and by nothing else (a card taken from a draw deck by its type, a card revealed from a hand
 * to a player who is shown no pop-up).
 */
public class GameLogReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: cost 2
					put("legolas", "1_50");     // Legolas, Greenleaf: stays in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheLogNamesTheCardsPlayedSinceTheMarkAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(aragorn, gimli, legolas);

		scn.StartGame();

		scn.MarkGameLog();
		assertTrue(scn.GetGameLogSinceMark().isEmpty());
		assertFalse(scn.GameLogSinceMarkMentions(aragorn));

		scn.FreepsPlayCard(aragorn);
		assertFalse(scn.GetGameLogSinceMark().isEmpty());
		assertTrue(scn.GameLogSinceMarkMentions(aragorn));
		assertFalse(scn.GameLogSinceMarkMentions(gimli));
		assertFalse(scn.GameLogSinceMarkMentions(legolas));
		// Every card asked about has to be named.
		assertFalse(scn.GameLogSinceMarkMentions(aragorn, gimli));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.GameLogSinceMarkMentions(aragorn, gimli));
		assertFalse(scn.GameLogSinceMarkMentions(legolas));
		// A card is named by its blueprint and title: the other player's copy answers the same.
		assertTrue(scn.GameLogSinceMarkMentions(scn.GetShadowCard("gimli")));
	}

	@Test
	public void ANewMarkDropsWhatWasReadBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		scn.MarkGameLog();
		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.GameLogSinceMarkMentions(aragorn));

		scn.MarkGameLog();
		assertTrue(scn.GetGameLogSinceMark().isEmpty());
		assertFalse(scn.GameLogSinceMarkMentions(aragorn));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.GameLogSinceMarkMentions(gimli));
		assertFalse(scn.GameLogSinceMarkMentions(aragorn));
	}

	@Test
	public void ReadingTheLogWithNoMarkSetFailsTheTest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.FreepsPlayCard(aragorn);

		// With no mark nothing was kept, and "not named" would be the answer whatever the game had said.
		assertThrows(AssertionError.class, () -> scn.GameLogSinceMarkMentions(aragorn));
		assertThrows(AssertionError.class, scn::GetGameLogSinceMark);
	}
}
