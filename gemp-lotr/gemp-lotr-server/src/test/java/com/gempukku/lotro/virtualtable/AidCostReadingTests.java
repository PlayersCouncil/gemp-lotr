package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: GetAidTwilightCost, which reads the N of a follower's printed "Aid - (N)" off the card.
 * The number read is the number an Aid moves when it is taken: a Free Peoples follower's Aid adds it to the
 * twilight pool and a Shadow follower's removes it.  A follower whose Aid is paid in anything but twilight has no
 * such number, and the rig says so; so does a card that is no follower.
 */
public class AidCostReadingTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pallando, Far-travelling One: [gandalf] follower, strength +1, "Aid - (2)."  Transferred to a
					// [gandalf] companion his own text asks nothing.
					put("pallando", "13_37");
					// Rohan Worker: [rohan] follower, "Aid - (1)."; its definition gives the cost no amount
					put("worker", "15_135");
					// Dinendal, Mirkwood Archer: [elven] follower, "Aid - (3)."
					put("dinendal", "15_12");
					// Saruman, Servant of Sauron: [isengard] follower of the Shadow side, strength +2, "Aid - (1)."
					// (the Shadow player removes it); his other text needs his bearer to win a skirmish
					put("saruman", "17_38");
					// Elendil's Army: "Aid - Exert a [gondor] companion."
					put("army", "18_47");
					// Deagol, Fateful Finder: "Aid - Add a burden."
					put("deagol", "13_46");
					// Gwaihir, The Windlord: "Aid - Add a threat."
					put("gwaihir", "17_20");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion to bear Pallando
					put("troop", "1_177");      // Goblin Patrol Troop: no text at all
				}},
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
	public void TheTwilightCostOfAnAidIsReadOffTheFollowerAndIsWhatTakingTheAidAdds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var troop = scn.GetShadowCard("troop");

		// The card is read wherever it lies: these three are in the draw deck.
		assertEquals(2, scn.GetAidTwilightCost(pallando));
		assertEquals(1, scn.GetAidTwilightCost(scn.GetFreepsCard("worker")));
		assertEquals(3, scn.GetAidTwilightCost(scn.GetFreepsCard("dinendal")));

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Reading the cost took no Aid and paid nothing.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando, "Aid"));
		assertEquals(2, scn.GetAidTwilightCost(pallando));
		assertEquals(pool, scn.GetTwilight());

		// Taken, the Aid adds what was read.
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(pallando, gandalf);
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(2, scn.GetAidTwilightCost(pallando));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowFollowersAidRemovesTheTwilightCostThatIsRead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(saruman);

		assertEquals(1, scn.GetAidTwilightCost(saruman));

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Free Peoples player has no follower, so the Shadow player's Aid is all that is asked.
		int pool = scn.GetTwilight();
		assertTrue(pool >= 1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman, "Aid"));

		// The Troop is the Shadow player's only character, so nothing more is asked.
		scn.ShadowAcceptOptionalTrigger();

		assertAttachedTo(saruman, troop);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAidThatIsNotPaidInTwilightHasNoTwilightCostAndTheRigSaysSo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var deagol = scn.GetFreepsCard("deagol");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();

		// An exertion, a burden, a threat.
		assertRefused(() -> scn.GetAidTwilightCost(army),
				"The Aid of Elendil's Army is not a plain amount of twilight, so it has no twilight cost to read.");
		assertRefused(() -> scn.GetAidTwilightCost(deagol), "The Aid of", "is not a plain amount of twilight");
		assertRefused(() -> scn.GetAidTwilightCost(gwaihir), "The Aid of Gwaihir", "is not a plain amount of twilight");

		// A card that is no follower has no Aid at all.
		assertRefused(() -> scn.GetAidTwilightCost(gandalf),
				"Gandalf, Friend of the Shirefolk is not a follower, so it has no Aid whose twilight cost could be read.");
		assertRefused(() -> scn.GetAidTwilightCost(null), "Nothing is not a follower");

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
