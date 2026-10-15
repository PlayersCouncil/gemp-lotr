package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_279_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thin", "1_279");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; adds (2) only when a weapon is played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, an Orc who is not [sauron]; no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, so Frodo (3) loses to him without being overwhelmed

					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; [shire] Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, who may exert to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring answers no wound while it is not worn, so the Ring-bearer's own skirmish asks
				// nothing and no burden comes of it.
				VirtualTableScenario.GreatRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thin", "1_279");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the other companion; strength 8
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring answers no wound while it is not worn, so the Ring-bearer's own skirmish asks
				// nothing and no burden comes of it.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ThinandStretchedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Thin and Stretched
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays on the Ring-bearer.
		 * 	Add a burden at the end of each turn during which bearer was not assigned to a skirmish (and another companion was).
		*/

		var scn = GetScenario();

		var thin = scn.GetFreepsCard("thin");

		assertEquals("Thin and Stretched", thin.getBlueprint().getTitle());
		assertNull(thin.getBlueprint().getSubtitle());
		assertTrue(thin.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, thin.getBlueprint().getSide());
		assertEquals(Culture.SAURON, thin.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, thin.getBlueprint().getCardType());
		assertEquals(0, thin.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenAtTheEndOfATurnInWhichAnotherCompanionWasAssignedToASkirmishAndItsBearerWasNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner = scn.GetShadowCard("runner1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		assertAttachedTo(thin, frodo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(aragorn);

		// The last thing asked in the turn: no burden yet.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// The turn has ended and the other player's has begun: the burden came as it ended, and it is the first
		// player's.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		// Nothing is played in the other player's turn, and nobody is assigned to a skirmish in it: the first
		// player's burdens read the same as his next turn begins.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsNoBurdenAtTheEndOfATurnInWhichItsBearerWasAssignedToASkirmishAsWellAsAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		assertAttachedTo(thin, frodo);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { frodo, runner1 }, new PhysicalCardImpl[] { aragorn, runner2 });
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner2));
		// Frodo's 3 loses to the Runner's 5 for one wound; Aragorn's 8 kills the other Runner.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// Another companion was assigned, and so was the bearer: no burden as the turn ended.
		assertEquals(0, scn.FreepsGetBurdens());
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AddsNoBurdenAtTheEndOfATurnInWhichNobodyWasAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var thin = scn.GetShadowCard("thin");
		var runner = scn.GetShadowCard("runner1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		assertAttachedTo(thin, frodo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// Neither player assigns the Runner to anybody.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(aragorn));

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The bearer was not assigned, and no other companion was either: no burden as the turn ended.
		assertEquals(0, scn.FreepsGetBurdens());
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(thin, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsASauronOrcToPlayOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var shadowFrodo = scn.GetShadowRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(thin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, guard, troop);
		assertInHand(thin);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(thin));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(thin);
		// The exertion: either [sauron] Orc; not the [sauron] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// The one Ring-bearer takes it, unasked: not Sam, who is Ring-bound, nor Aragorn, nor the Shadow player's own
		// Ring-bearer, who is not active on this turn.
		assertAttachedTo(thin, frodo);
		assertNotAttachedTo(thin, sam);
		assertNotAttachedTo(thin, aragorn);
		assertNotAttachedTo(thin, shadowFrodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(thin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		// Vitality 2: one wound exhausts the only [sauron] Orc.  The Uruk-hai and the [moria] Orc could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInHand(thin);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(thin));
		assertInHand(thin);
	}

	@Test
	public void AddsNoBurdenWhenTheOnlyOtherCharacterAssignedIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(soldier);
		assertAttachedTo(thin, frodo);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, soldier);
		assertTrue(scn.IsCharAssignedAgainst(thrarin, soldier));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(aragorn));
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassCurrentPhaseActions();

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// An ally is no companion: no burden as the turn ended.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.FreepsGetBurdens());
		assertAttachedTo(thin, frodo);
	}

	@Test
	public void AddsNoBurdenWhenTheBearerIsAssignedOnlyInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var pursuer = scn.GetShadowCard("pursuer");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(soldier, pursuer);
		assertAttachedTo(thin, frodo);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		// The Pursuer is left over, and the Shadow player assigns him to nobody.
		scn.ShadowDeclineAssignments();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, soldier));
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The fierce round: the bearer is assigned now, to the fierce Pursuer.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, pursuer);
		assertTrue(scn.IsCharAssignedAgainst(frodo, pursuer));
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// The bearer was assigned to a skirmish during the turn, if only in the fierce round: no burden.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.FreepsGetBurdens());
		assertAttachedTo(thin, frodo);
	}

	@Test
	public void AddsABurdenAgainOnALaterTurnAndNoneForAnotherCompanionAssignedOnlyOnTheTurnBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(soldier);
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());

		// Turn 1: Aragorn is assigned, the bearer is not.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.FreepsGetBurdens());

		// Turn 3: no minion, so nobody is assigned; Aragorn's assignment of turn 1 does not count again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetBurdens());
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.FreepsGetBurdens());

		// Turn 5: Sam, a companion of another culture and race, is assigned, the bearer is not: a second burden.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(thin, frodo);
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, chieftain);
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheBearersAssignmentOnTheTurnBeforeDoesNotStopTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thin = scn.GetShadowCard("thin");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AttachCardsTo(frodo, thin);
		scn.MoveMinionsToTable(soldier, runner);
		assertAttachedTo(thin, frodo);
		assertEquals(0, scn.GetBurdens());

		// Turn 1: the bearer and Aragorn are both assigned: no burden.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { frodo, runner }, new PhysicalCardImpl[] { aragorn, soldier });
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.SkipToOtherPlayersTurn();
		assertEquals(0, scn.FreepsGetBurdens());

		// Turn 3: Aragorn alone is assigned; the bearer's assignment of turn 1 does not count.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(thin, frodo);
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
