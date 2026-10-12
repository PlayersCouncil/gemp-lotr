package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A cost of assigning: "the Free Peoples player must <pay> to assign <this minion> to a skirmish" (the definition
 * language's extraCostToAssign; Q-0221).
 *
 * The cost is paid as the assignment is made and is not asked for ahead of it.  At the Free Peoples player's own
 * assigning every charged assignment of his answer is paid for, one after another in the order of the minions'
 * card ids, and then all the assignments that were paid for or cost nothing are made at once.  Where a card has
 * him assign (his own or a Shadow card), he pays once the pair is chosen.  A cost that is not paid means that one
 * assignment is not made: at his own assigning the minion is then a leftover minion, the Shadow player's to
 * assign.  While the cost cannot be paid the minion is not the Free Peoples player's to assign at all.  The Shadow
 * player's assignments cost nothing.
 *
 * Shown with Morgul Spawn 7_200: "While you can spot a Nazgûl, the Free Peoples player must exert a companion to
 * assign this minion to a skirmish."  A cost that is PREVENTED (a burden, by Melilot Brandybuck 3_110) is shown
 * in the tests of Ulaire Nertea 69_38, the one card whose cost can be.  A cost that is REPEATED ("for each Nazgûl
 * you can spot ... must exert a companion") can be paid only if every repetition can, and is not begun otherwise
 * (F-0425): the tests of Morgul Regiment 7_197, the one card with one.  An exertion a card's action takes as its own
 * cost is counted before the cost of the assignment that action then makes as part of its price (F-0486): the
 * tests of Éowyn 17_96.
 */
public class CostOfAssigningTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Morgul Spawn: [wraith] Orc, strength 9, vitality 2; the text above is its whole text
					put("spawn1", "7_200");
					put("spawn2", "7_200");
					// Úlairë Lemenya, Lieutenant of Morgul: the Nazgûl to spot. Strength 9, vitality 2, not fierce;
					// her text is about companions bearing [wraith] conditions
					put("lemenya", "1_232");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("guard", "1_7");        // Dwarf Guard: vitality 2, so one wound exhausts him
					// Éowyn, Northwoman: vitality 3. "Assignment: Exert Éowyn and assign a minion to the
					// Ring-bearer to make that minion lose all game text keywords ... until the regroup phase."
					put("eowyn", "17_96");

					// The End Comes: [moria] event. "Assignment: Spot 5 [moria] minions to make the Free Peoples
					// player assign the Ring-bearer to a skirmish."
					put("endcomes", "1_169");
					// Goblin Runner: a [moria] minion, harmless when cheated in
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AtHisOwnAssigningTheCostIsPaidAsTheAssignmentIsMadeAndNotAskedAhead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// Nothing is asked and nobody has exerted; the Spawn is his to assign
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.FreepsCanAssign(spawn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsAssignToMinions(aragorn, spawn);

		// "Exert a companion": either of the two, one of them must, and the Spawn is not assigned until one has
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(spawn));

		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));

		// The Nazgûl is left over for the Shadow player; the Spawn is not
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(spawn));
	}

	@Test
	public void AMinionHeLeavesUnassignedCostsNothingAndTheShadowPlayerAssignsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// The Nazgûl costs nothing to assign, and nobody is asked to exert
		scn.FreepsAssignToMinions(aragorn, lemenya);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));

		// The cost is the Free Peoples player's alone
		scn.ShadowAssignToMinions(frodo, spawn);

		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithNobodyAbleToPayTheMinionIsNotHisToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		// The Ring-bearer is the only companion, and exhausted
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToAssignments();

		assertEquals(1, scn.GetVitality(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertFalse(scn.FreepsCanAssign(spawn));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, spawn));

		scn.FreepsDeclineAssignments();

		// It is the Shadow player's to assign, for nothing
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));
		scn.ShadowAssignToMinions(frodo, spawn);

		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwoChargedAssignmentsArePaidForInTurnAndTheOneLeftUnpaidIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn1 = scn.GetShadowCard("spawn1");
		var spawn2 = scn.GetShadowCard("spawn2");
		var lemenya = scn.GetShadowCard("lemenya");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn1, spawn2, lemenya);
		// One exertion is left among the companions: the Ring-bearer's
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToAssignments();

		// Each Spawn can be paid for by itself, so both are his to assign
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn1));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn2));

		// The payments are made in the order of the minions' card ids
		var first = spawn1.getCardId() < spawn2.getCardId() ? spawn1 : spawn2;
		var second = first == spawn1 ? spawn2 : spawn1;

		scn.MarkGameLog();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, second}, new PhysicalCardImpl[]{guard, first});

		// The Ring-bearer, the one companion able to, exerts for the first; nobody is left to exert for the second
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.IsCharAssignedAgainst(guard, first));
		assertFalse(scn.IsCharAssigned(second));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.GameLogSinceMarkSays("did not pay to assign"));

		// He is not asked to assign again: the unpaid Spawn is a leftover minion like the Nazgûl
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(second));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(first));

		scn.ShadowAssignToMinions(frodo, second);

		assertTrue(scn.IsCharAssignedAgainst(frodo, second));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void HisOwnCardsAssignmentIsPaidForOnceThePairIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var eowyn = scn.GetFreepsCard("eowyn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);

		// Her own exertion first, then the minion: the Spawn may be taken
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(spawn, lemenya));
		scn.FreepsChooseCard(spawn);

		// The Spawn's cost, asked now that it is the minion chosen
		assertTrue(scn.FreepsHasCardChoiceAvailable(eowyn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(spawn));

		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void ACompanionsLastExertionPaysAndTheAssignmentStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var eowyn = scn.GetFreepsCard("eowyn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		// Éowyn has the one exertion her own action takes, and the Ring-bearer one more
		scn.AddWoundsToChar(eowyn, 1);
		scn.AddWoundsToChar(frodo, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(spawn, lemenya));
		scn.FreepsChooseCard(spawn);

		// The Ring-bearer, the one companion still able to exert, pays without a question; with nobody able to
		// exert again the assignment paid for is made all the same
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void ACardCannotHaveHimAssignAMinionHeCannotPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var eowyn = scn.GetFreepsCard("eowyn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		// Éowyn has the one exertion her own action takes; the Ring-bearer is exhausted
		scn.AddWoundsToChar(eowyn, 1);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);

		// With her exertion spent nobody could exert for the Spawn, so the Nazgûl is the one minion to assign
		// and is assigned without a choice
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertFalse(scn.IsCharAssigned(spawn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AShadowCardThatHasHimAssignChargesHimTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var endcomes = scn.GetShadowCard("endcomes");
		var runner1 = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(endcomes);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya, runner1, scn.GetShadowCard("runner2"), scn.GetShadowCard("runner3"),
				scn.GetShadowCard("runner4"), scn.GetShadowCard("runner5"));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The Free Peoples player is the one assigning: he chooses the minion, and the Spawn is among them
		assertTrue(scn.FreepsHasCardChoiceAvailable(spawn, lemenya, runner1));
		scn.FreepsChooseCard(spawn);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertFalse(scn.IsCharAssigned(spawn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
