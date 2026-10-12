package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A cost of assigning: "the Free Peoples player must <pay> to assign <this minion> to a skirmish" (the definition
 * language's extraCostToAssign; Q-0221, and Q-0283 for when it is paid).
 *
 * At his own assigning the cost is paid AHEAD (Q-0283).  As he starts assigning, each minion with such a cost that
 * can be paid in full, and that he could then assign, is offered to him as an optional trigger of that minion; he
 * takes them in any order, paying for each as he takes it, and is offered the rest again, judged afresh, until he
 * passes.  A minion paid for is his to assign; one he did not pay for is not, and is left for the Shadow player.
 * Nothing is paid once he has answered, and a payment he does not use is lost.  Where a card has him assign (his
 * own or a Shadow card), which comes before his own assigning, he pays once the pair is chosen, and the minion is
 * his to take while the cost can be paid.  The Shadow player's assignments cost nothing.
 *
 * Shown with Morgul Spawn 7_200: "While you can spot a Nazgûl, the Free Peoples player must exert a companion to
 * assign this minion to a skirmish."  A cost that is PREVENTED (a burden, by Melilot Brandybuck 3_110), and one
 * that charges against some characters only, are shown in the tests of Ulaire Nertea 69_38, the one card whose
 * cost can be prevented and the one that names whom.  A cost that is REPEATED ("for each Nazgûl you can spot ...
 * must exert a companion") can be paid only if every repetition can, and is not offered otherwise (F-0425): the
 * tests of Morgul Regiment 7_197, the one card with one.  An exertion a card's action takes as its own cost is
 * counted before the cost of the assignment that action then makes as part of its price (F-0486): the tests of
 * Éowyn 17_96.
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
	public void AsHeStartsAssigningHeIsOfferedToPayForTheMinionAndPaidForItIsHisToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// Before he assigns anything, the Spawn is offered to him, and the Nazgûl, which costs nothing, is not
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsChooseOptionalTrigger(spawn);

		// "Exert a companion": either of the two, and one of them must
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Paid for, it is not offered again, and it is his to assign, for nothing more
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.FreepsCanAssign(spawn));
		scn.FreepsAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Nazgûl is left over for the Shadow player; the Spawn is not
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(spawn));
	}

	@Test
	public void AMinionHeDoesNotPayForIsNotHisToAssignAndTheShadowPlayerAssignsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		scn.FreepsDeclineOptionalTrigger();

		// Unpaid, the Spawn is not among the minions he may assign, though the cost could have been paid
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertFalse(scn.FreepsCanAssign(spawn));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, spawn));

		// The Nazgûl costs nothing to assign
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
	public void APaymentHeDoesNotUseIsLost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		scn.FreepsChooseOptionalTrigger(spawn);
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// He pays for the Spawn and then assigns only the Nazgûl
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsAssignToMinions(aragorn, lemenya);

		// The exertion stays paid, and the Spawn is the Shadow player's to assign like any leftover minion
		assertFalse(scn.IsCharAssigned(spawn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));
		scn.ShadowAssignToMinions(frodo, spawn);

		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WithNobodyAbleToPayTheMinionIsNotOfferedAndIsNotHisToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn1");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		// The Ring-bearer is the only companion, and exhausted
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToAssignments();

		// Nothing is offered: the first thing asked is his assigning
		assertEquals(1, scn.GetVitality(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(spawn));
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
	public void TwoChargedMinionsAreOfferedTogetherTakenInEitherOrderAndJudgedAfreshAfterEachPayment() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Each Spawn can be paid for by itself, so both are offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn2));

		// He takes the one with the higher card id first: the order is his
		var later = spawn1.getCardId() > spawn2.getCardId() ? spawn1 : spawn2;
		var earlier = later == spawn1 ? spawn2 : spawn1;
		scn.FreepsChooseOptionalTrigger(later);

		// The Ring-bearer, the one companion able to, exerts without a question; nobody is left to pay for the
		// other, so it is offered no more, and his assigning is asked
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(earlier));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(later));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(earlier));

		scn.FreepsAssignToMinions(guard, later);

		assertTrue(scn.IsCharAssignedAgainst(guard, later));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(earlier));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(later));
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
