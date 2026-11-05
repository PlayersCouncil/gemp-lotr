package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: MarkGameEvents and GetGameEventsSinceMark, the reading of what the game sends each seat,
 * through a channel joined as the replay's recorder joins one (GameRecorder.recordGame).  What a seat is sent as the
 * channel joins (the whole board) is not counted; what the game sends after it is, in order, and stays to be read
 * again; a second mark starts afresh.  Read without a mark, it fails.
 */
public class GameEventMarkTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheEventsSentSinceTheMarkAreReadInOrderAndASecondMarkStartsAfresh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		scn.MarkGameEvents();
		assertTrue(scn.GetGameEventsSinceMark(scn.P1()).isEmpty());
		assertTrue(scn.GetGameEventsSinceMark(scn.P2()).isEmpty());

		scn.FreepsPlayCard(aragorn);
		var afterAragorn = scn.GetGameEventsSinceMark(scn.P1());
		assertTrue(afterAragorn.stream().anyMatch(event -> event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY
				&& event.getCardId() == aragorn.getCardId()));
		assertTrue(afterAragorn.stream().anyMatch(event -> event.getType() == GameEvent.Type.SEND_MESSAGE));
		assertFalse(scn.GetGameEventsSinceMark(scn.P2()).isEmpty());
		// Read again, the same.
		assertEquals(afterAragorn, scn.GetGameEventsSinceMark(scn.P1()));

		scn.FreepsPlayCard(gimli);
		var afterGimli = scn.GetGameEventsSinceMark(scn.P1());
		assertEquals(afterAragorn, afterGimli.subList(0, afterAragorn.size()));
		assertTrue(afterGimli.size() > afterAragorn.size());

		scn.MarkGameEvents();
		assertTrue(scn.GetGameEventsSinceMark(scn.P1()).isEmpty());
	}

	@Test
	public void TheEventsAreNotReadWithoutAMark() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		scn.StartGame();

		String refusal = null;
		try {
			scn.GetGameEventsSinceMark(scn.P1());
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		assertTrue(refusal, refusal.contains("call MarkGameEvents() first"));
	}
}
