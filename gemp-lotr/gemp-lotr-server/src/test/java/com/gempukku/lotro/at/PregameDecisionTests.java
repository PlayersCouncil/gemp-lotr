package com.gempukku.lotro.at;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class PregameDecisionTests
{
	@Test
	public void StaleBidAnswerToTheSeatingDecisionIsRejectedInsteadOfCancellingTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		// #1023: pre-game decisions all reuse decision id 1, so a late or duplicated answer to the bidding
		// decision ("2") can arrive while the "Choose one" seating decision (Go first / Go second) is pending
		// for the same player.  It passes the id check and used to index straight into the two seating
		// options, throwing an ArrayIndexOutOfBoundsException that cancelled the whole game.
		// It must instead be treated like any other invalid answer: rejected, with the decision re-offered.
		new VirtualTableScenario(new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null,
				scn -> {
					scn.FreepsDecided("2");
					scn.ShadowDecided("0");

					// Freeps won the bid and is now being asked where to sit
					assertTrue(scn.FreepsDecisionAvailable("Choose one"));
					assertEquals(2, scn.FreepsGetMultipleChoiceCount());

					try {
						scn.FreepsDecided("2");
						fail("An out-of-range answer must be rejected");
					} catch (RuntimeException exp) {
						assertTrue(exp.getCause() instanceof DecisionResultInvalidException);
					}

					// The seating decision is still pending and can be answered properly
					assertTrue(scn.FreepsDecisionAvailable("Choose one"));
					scn.FreepsDecided("0");

					assertTrue(scn.FreepsDecisionAvailable("mulligan"));
				}
		);
	}
}
