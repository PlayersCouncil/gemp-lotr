package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_004_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn: Gondor companion (potential wound target)
		put("runner", "1_178"); // Goblin Runner: Moria Orc minion
		put("event", "1_116"); // Swordarm of the White Tower: skirmish event
		put("troop", "1_177"); // Goblin Patrol Troop: Moria Orc minion, strength 13, vitality 3
		put("drums", "1_168"); // Drums in the Deep: Shadow skirmish event
		put("gimli", "1_13"); // Gimli: second companion, for a second skirmish
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_4"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_4
		 * Type: MetaSite
		 * Game Text: Wound one of your characters after each skirmish in which you did not play a
		 * skirmish event.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void WoundsWhenNoSkirmishEventPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Don't play any skirmish events — just pass
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// After skirmish resolves, the trigger fires — wound one of your characters
		// Aragorn has 0 wounds before, should be asked to choose a wound target
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void NoWoundWhenSkirmishEventPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var event = scn.GetFreepsCard("event");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// Play the skirmish event
		scn.FreepsPlayCard(event);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// No wound trigger should fire because we played a skirmish event
		// Should proceed past the skirmish without a wound choice
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Issue #1099. "Your characters" follows the player who holds the modifier, not a side: while that player is the
	// Shadow player their characters are their minions, and "you did not play a skirmish event" is about them alone.
	@Test
	public void ShadowOwnerWoundsOwnMinionWhenTheyPlayedNoSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		// The Shadow player (who holds the modifier) wounds one of their own characters: a minion
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		// Aragorn only took the skirmish loss wound
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void ShadowOwnerIsNotWoundedWhenTheyPlayedASkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(drums);
		if (scn.ShadowDecisionAvailable("Choose cards"))
			scn.ShadowChooseCard(troop);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		assertFalse(scn.ShadowDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowOwnerIsStillWoundedWhenOnlyTheFreepsPlayerPlayedASkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var event = scn.GetFreepsCard("event");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		scn.FreepsPlayCard(event);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowDecisionAvailable("Choose cards to wound"));
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
	}

	@Test
	public void FreepsOwnerIsNotWoundedForTheShadowPlayersMissingSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		// The Free Peoples holder plays a skirmish event; the Shadow player plays none. Only "you" counts.
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var event = scn.GetFreepsCard("event");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		scn.FreepsPlayCard(event);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertFalse(scn.ShadowDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SameHolderWoundsCompanionOnTheirTurnAndMinionOnTheOpponentsTurn() throws DecisionResultInvalidException, CardNotFoundException {
		// P1 holds the modifier throughout. Turn 1 P1 is the Free Peoples player; turn 2 the roles swap and P1 is the
		// Shadow player, so "your characters" are P1's minions.
		var scn = GetFreepsScenario();

		var p1Aragorn = scn.GetFreepsCard("aragorn");
		var p1Troop = scn.GetFreepsCard("troop");
		var p1Runner = scn.GetFreepsCard("runner");
		var p2Aragorn = scn.GetShadowCard("aragorn");
		var p2Troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(p1Aragorn, p2Aragorn);

		scn.StartGame();

		// Turn 1: P1 is Free Peoples
		scn.MoveMinionsToTable(p2Troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(p1Aragorn, p2Troop);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p2Troop));
		scn.FreepsChooseCard(p1Aragorn);
		assertEquals(2, scn.GetWoundsOn(p1Aragorn));   // skirmish loss + the modifier

		// Finish turn 1 without moving again
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		if (scn.FreepsDecisionAvailable("reconcile")) scn.FreepsDeclineReconciliation();
		while (scn.FreepsDecisionAvailable("discard down")) scn.FreepsChooseCard((PhysicalCardImpl) scn.GetFreepsHand().getFirst());
		if (scn.ShadowDecisionAvailable("reconcile")) scn.ShadowDeclineReconciliation();
		while (scn.ShadowDecisionAvailable("discard down")) scn.ShadowChooseCard((PhysicalCardImpl) scn.GetShadowHand().getFirst());

		// Turn 2: P2 is Free Peoples, P1 (the holder) is the Shadow player
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action"));
		scn.MoveMinionsToTable(p1Troop, p1Runner);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowAssignToMinions(p2Aragorn, p1Troop);
		if (scn.FreepsDecisionAvailable("Assign minions"))
			scn.FreepsDeclineAssignments();
		if (scn.ShadowDecisionAvailable("Choose next skirmish"))
			scn.ShadowChooseCard(p2Aragorn);

		// Neither side plays a skirmish event
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// P1 wounds one of their own minions; nothing of P2's is offered
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(p1Troop, p1Runner));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p2Aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p1Aragorn));
		scn.FreepsChooseCard(p1Troop);
		assertEquals(1, scn.GetWoundsOn(p1Troop));
		assertEquals(1, scn.GetWoundsOn(p2Aragorn));   // only the skirmish loss
	}

	@Test
	public void EachSkirmishIsJudgedSeparately() throws DecisionResultInvalidException, CardNotFoundException {
		// A skirmish event played in the first skirmish does not cover the second one.
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var event = scn.GetFreepsCard("event");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{gimli, troop});
		if (scn.ShadowDecisionAvailable("Assign minions"))
			scn.ShadowDeclineAssignments();

		// Skirmish 1: Aragorn vs the runner, with a skirmish event -> no wound from the modifier
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(event);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Skirmish 2: Gimli vs the troop, no skirmish event -> the modifier wounds one of your characters
		if (scn.FreepsDecisionAvailable("Choose next skirmish"))
			scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}
}
