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
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: Gondor companion (potential wound target)
					put("runner", "1_178"); // Goblin Runner: Moria Orc minion
					put("event", "1_116"); // Swordarm of the White Tower: skirmish event
					put("troop", "1_177"); // Goblin Patrol Troop: Moria Orc minion, strength 13, vitality 3
					put("drums", "1_168"); // Drums in the Deep: Shadow skirmish event
					put("gimli", "1_13"); // Gimli: second companion, for a second skirmish
					put("breath", "1_25"); // Still Draws Breath: a Response event (a Dwarf wins a skirmish: heal him)
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: Gondor companion (potential wound target)
					put("runner", "1_178"); // Goblin Runner: Moria Orc minion
					put("event", "1_116"); // Swordarm of the White Tower: skirmish event
					put("troop", "1_177"); // Goblin Patrol Troop: Moria Orc minion, strength 13, vitality 3
					put("drums", "1_168"); // Drums in the Deep: Shadow skirmish event
					put("gimli", "1_13"); // Gimli: second companion, for a second skirmish
					put("breath", "1_25"); // Still Draws Breath: a Response event (a Dwarf wins a skirmish: heal him)
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally
				}},
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
		 * Intensity: 6
		 * Game Text: Wound one of your characters after each skirmish in which you did not play a
		 * skirmish event.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(6, card.getBlueprint().getIntensity());
	}

	@Test
	public void WoundsWhenNoSkirmishEventPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Don't play any skirmish events — just pass
		scn.FreepsPass();
		scn.ShadowPass();

		// After skirmish resolves, the trigger fires — wound one of your characters
		// Aragorn has 0 wounds before, should be asked to choose a wound target
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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

		scn.ShadowPass();
		scn.FreepsPass();

		// No wound trigger should fire because we played a skirmish event
		// Should proceed past the skirmish without a wound choice
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
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

		scn.FreepsPass();
		scn.ShadowPass();

		// The Shadow player (who holds the modifier) wounds one of their own characters: a minion
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		// Aragorn only took the skirmish loss wound
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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

		scn.FreepsPass();
		scn.ShadowPlayCard(drums);
		scn.ShadowChooseCard(troop);
		scn.FreepsPass();
		scn.ShadowPass();

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
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.ShadowDecisionAvailable("Choose cards to wound"));
		scn.ShadowChooseCard(runner);
		// The Runner has 1 vitality: the wound kills it
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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
		scn.ShadowPass();
		scn.FreepsPass();

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
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p2Troop));
		scn.FreepsChooseCard(p1Aragorn);
		assertEquals(2, scn.GetWoundsOn(p1Aragorn));   // skirmish loss + the modifier

		// Finish turn 1 without moving again
		scn.SkipToOtherPlayersTurn();

		// Turn 2: P2 is Free Peoples, P1 (the holder) is the Shadow player
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action"));
		scn.MoveMinionsToTable(p1Troop, p1Runner);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowAssignToMinions(p2Aragorn, p1Troop);
		scn.FreepsDeclineAssignments();
		scn.ShadowResolveSkirmish(p2Aragorn);

		// Neither side plays a skirmish event
		scn.ShadowPass();
		scn.FreepsPass();

		// P1 wounds one of their own minions; nothing of P2's is offered
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(p1Troop, p1Runner));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p2Aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(p1Aragorn));
		scn.FreepsChooseCard(p1Troop);
		assertEquals(1, scn.GetWoundsOn(p1Troop));
		assertEquals(1, scn.GetWoundsOn(p2Aragorn));   // only the skirmish loss
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
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

		// Skirmish 1: Aragorn vs the runner, with a skirmish event -> no wound from the modifier
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(event);
		scn.ShadowPass();
		scn.FreepsPass();
		assertFalse(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Skirmish 2: Gimli vs the troop, no skirmish event -> the modifier wounds one of your characters
		// (Gimli, strength 6, is overwhelmed by the Troop's 13 and killed: the wound is Aragorn's to take)
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSkirmishWithNoSkirmishEventBringsAWoundOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, troop});

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Skirmish 1: Gimli (6) beats the Runner (5). Nothing is played: a wound, here put on Aragorn
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Skirmish 2: Aragorn (8) loses to the Troop (13) and takes the loser's wound. Nothing is played
		// again: a second wound from the modifier, on Aragorn again
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		scn.FreepsChooseCard(aragorn);

		// 1 from the first skirmish's end + 1 for losing the second + 1 from the second skirmish's end
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishAbilityAndAResponseEventAreNoSkirmishEvents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var breath = scn.GetFreepsCard("breath");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gimli);
		// Bounder is at home at site 2, and is left out of the skirmishes
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(breath);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		// A skirmish special ability: Gimli exerts to be strength +2
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.ShadowPass();
		scn.FreepsPass();

		// Gimli wins, and an event is played in the skirmish that is no skirmish event: a Response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, breath);

		// Neither spares the wound. It may go on a companion in the skirmish or out of it or on an ally,
		// and not on the other player's minion
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(bounder);

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
