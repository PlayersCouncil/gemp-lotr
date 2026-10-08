package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: what it says when it is asked to answer or to read something that is not there.  An
 * answer for a player who is not being asked anything, an action of a card that is not on offer, and a reading of
 * a parameter the pending decision does not have each fail the test with a message that names the player, what was
 * attempted and what each player is in fact being asked, and leave whatever is pending as it was.  Also the one
 * reading that is no failure: the maximum of a number the game sets no upper bound on.
 */
public class RigFailureTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a maneuver ability only
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: a Wizard, for Gathering Wind
					put("gimli", "1_13");           // Gimli, Son of Gloin: companion, cost 2
					// Gathering Wind.  "Maneuver: Exert your Wizard to choose a number.  Make each minion with that
					// twilight cost strength -2 until the regroup phase."  The number has no upper bound.
					put("wind", "10_16");

					put("runner", "1_178");         // Goblin Runner: cost 1, strength 5
					put("troop", "1_177");          // Goblin Patrol Troop: cost 6, strength 13, no text
					put("slayer1", "3_93");         // Morgul Slayer: a [sauron] minion; a regroup ability only
					put("slayer2", "3_93");
					// Forces of Mordor.  "Shadow: Spot X [sauron] minions to add (X)."  X is bounded by the minions.
					put("forces", "1_248");
				}},
				// Site 1 is Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."  With
				// Frodo there it is an action on offer in every fellowship phase.
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
	public void AnAnswerForAPlayerWhoIsNotBeingAskedAnythingIsRefusedSayingWhatEachPlayerIsBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// In the fellowship phase the Shadow player is not being asked anything.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(() -> scn.ShadowAssignToMinions(aragorn, runner),
				"Shadow Player was to answer \"" + aragorn.getCardId() + " " + runner.getCardId() + "\", but is not being asked anything.",
				freepsAsked);
		assertRefused(() -> scn.ShadowChooseCard(aragorn),
				"Shadow Player was to answer \"" + aragorn.getCardId() + "\", but is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowChooseYes,
				"Shadow Player was to choose \"Yes\", but that player is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowDeclineOptionalTrigger,
				"Shadow Player was to pass or decline, but is not being asked anything.", freepsAsked);

		// Nothing has moved.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The plain passes are not answers of that kind: with nothing pending they do nothing and say nothing.
		scn.ShadowPass();
		scn.ShadowPass("Play Shadow action or Pass");
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second assignment by the Free Peoples player, who gave all of his in one answer: it is the Shadow
		// player who is being asked now, and the refusal says so.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertRefused(() -> scn.FreepsAssignToMinions(gandalf, troop),
				"Free Peoples Player was to answer \"" + gandalf.getCardId() + " " + troop.getCardId() + "\", but is not being asked anything.",
				"Shadow Player is being asked \"Assign minions to companions or allies at home\".");
		assertFalse(scn.IsCharAssigned(gandalf));

		// What the Shadow player is being asked is still there to be answered.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(gandalf, troop);
		assertTrue(scn.IsCharAssignedAgainst(gandalf, troop));
	}

	@Test
	public void ACardThatIsNotOnOfferIsRefusedByNameWithWhatIsOnOfferAndTheChoiceStaysPending() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var wind = scn.GetFreepsCard("wind");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli, wind);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		// Gimli can be played and Bag End used; a maneuver event cannot be played, and Aragorn has nothing to use.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsPlayAvailable(wind));
		assertFalse(scn.FreepsActionAvailable(aragorn));

		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [";
		assertRefused(() -> scn.FreepsPlayCard(wind),
				"Gathering Wind is not offering Free Peoples Player a play.", freepsAsked, "Use Bag End", "Play Gimli",
				"Shadow Player is not being asked anything.");
		assertRefused(() -> scn.FreepsUseCardAction(aragorn),
				"Aragorn, Ranger of the North is not offering Free Peoples Player an action.", freepsAsked, "Use Bag End");
		assertRefused(() -> scn.FreepsTransferCard(aragorn),
				"Aragorn, Ranger of the North is not offering Free Peoples Player a transfer.", freepsAsked);
		// The other player's card, while that player is not being asked anything at all.
		assertRefused(() -> scn.ShadowPlayCard(runner),
				"Goblin Runner is not offering Shadow Player a play.", "Shadow Player is not being asked anything.", freepsAsked);

		// The choice the Free Peoples player was facing is still pending, and is answered like any other.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertInZone(Zone.HAND, wind);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReadingAParameterTheDecisionDoesNotHaveIsRefusedSayingWhatIsBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var wind = scn.GetFreepsCard("wind");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// A choice of actions has no bounds and shows no cards.
		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(scn::FreepsGetChoiceMin,
				"The 'min' of Free Peoples Player's decision was to be read, but the decision in front of that player has no 'min'.",
				freepsAsked, "Shadow Player is not being asked anything.");
		assertRefused(scn::FreepsGetChoiceMax, "has no 'max'.", freepsAsked);
		assertRefused(scn::FreepsGetSelectableCount, "has no 'selectable'.", freepsAsked);
		// And a player who is not being asked anything has no decision to read.
		assertRefused(scn::ShadowGetChoiceMin,
				"The 'min' of Shadow Player's decision was to be read, but that player is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowGetSelectableCount, "that player is not being asked anything.", freepsAsked);

		// The list of actions is a reading that has an answer either way: those on offer, or none.
		assertEquals(1, scn.FreepsGetAvailableActions().size());
		assertTrue(scn.ShadowGetAvailableActions().isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A choice of a number is no choice of actions: none are listed, and a card is not played into it.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertTrue(scn.FreepsGetAvailableActions().isEmpty());
		assertFalse(scn.FreepsAnyActionsAvailable());
		assertFalse(scn.FreepsActionAvailable("Use"));
		assertRefused(scn::FreepsGetSelectableCount, "has no 'selectable'.", "Free Peoples Player is being asked \"Choose a minion cost\".");
		assertRefused(() -> scn.FreepsPlayCard(gimli),
				"Gimli, ", " is not offering Free Peoples Player a play.", "Free Peoples Player is being asked \"Choose a minion cost\".");
		assertRefused(scn::FreepsChooseYes,
				"Free Peoples Player was to choose \"Yes\", but the decision in front of that player has no 'results'.");

		// The number is still being asked for.
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertEquals(0, scn.FreepsGetChoiceMin());
	}

	@Test
	public void TheMaximumOfANumberWithNoUpperBoundReadsAsUncappedAndThatOfABoundedNumberAsItsBound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var troop = scn.GetShadowCard("troop");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var forces = scn.GetShadowCard("forces");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wind);
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, slayer1, slayer2);
		scn.FreepsPass();

		// "Spot X [sauron] minions": the two Morgul Slayers bound the number.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(forces);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertEquals(twilight + 2, scn.GetTwilight());

		// "Choose a number": the game names a least number and no greatest.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());

		// The number is asked and taken as before: the minion costing 6 is the one weakened.
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsChoose("6");
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(slayer1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
