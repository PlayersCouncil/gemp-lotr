package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of the rig itself: FreepsDeclineSanctuaryHealing declines the sanctuary heals that are still to be chosen
 * and nothing else.  At the start of a turn at a sanctuary the game asks "Sanctuary healing - Choose companion to
 * heal" five times, each a choice of one companion or none.  The method used to send five empty answers whatever
 * was being asked, so with one heal taken (four left to decline) its fifth answer passed the fellowship phase and
 * the fellowship moved.
 */
public class SanctuaryHealingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
				}},
				// Site 3 is Rivendell Valley, a sanctuary.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
	public void EveryHealIsDeclinedAndTheFellowshipPhaseIsNotPassed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		// The turn at the sanctuary begins with the first of the five heals.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));

		scn.FreepsDeclineSanctuaryHealing();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSomeHealsTakenTheRestAreDeclinedAndTheFellowshipPhaseIsNotPassed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		// One heal for Aragorn; four are left, and both companions could still take one.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 4"));

		// Four answers and no fifth: five used to be sent, and the last passed the fellowship phase.
		scn.FreepsDeclineSanctuaryHealing();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhereNoHealIsBeingAskedDecliningIsRefusedSayingWhatIsAskedAndNothingIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		// At a site that is no sanctuary
		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertRefused(scn::FreepsDeclineSanctuaryHealing,
				scn.P1() + " was to decline sanctuary healing, but is not being asked to choose a sanctuary heal.",
				scn.P1() + " is being asked \"Play Fellowship action or Pass\"");
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// At the sanctuary, once the only wound there was has been healed: the game asks no more, and the
		// fellowship phase that has begun is not passed by declining.
		scn.SkipToSite(3);
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		assertRefused(scn::FreepsDeclineSanctuaryHealing,
				scn.P1() + " was to decline sanctuary healing, but is not being asked to choose a sanctuary heal.");
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
