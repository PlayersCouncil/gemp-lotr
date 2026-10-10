package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The keyword defender +X.
 *
 * Comprehensive Rules 4.2, "defender +X": "During the assignment phase, if your assigned companion has the keyword
 * defender +1, you may assign that character to one additional unassigned minion."  Its example: "Frodo and
 * Aragorn face two Uruk-hai.  The Free Peoples player could assign Aragorn to one and Frodo to the other.  However,
 * this version of Aragorn has defender +1, so he may be assigned to defend against both minions".
 *
 * "Assignment phase": "All assignments of characters are on a one-to-one basis, with the following two exceptions:
 * Companions who have the defender +X keyword", and the Shadow player's assigning of leftover minions, which is in
 * AssignmentPhaseTests.
 *
 * The keyword is the Free Peoples player's own, when he assigns.  An assignment a card makes is not his assigning:
 * "Many assignment actions assign a minion to a companion.  You cannot do this unless both of them are unassigned"
 * ("assignment phase"), whatever the companion's defender bonus and whichever player's action it is (F-0355).
 *
 * The Aragorn of the rules' example is Aragorn, Ranger of the North 1_89, who makes himself defender +1 with his
 * own maneuver ability; that is how the keyword comes onto the table here.
 */
public class DefenderTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("aragorn", "1_89");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion with no defender; a skirmish ability, not used here
					// Unexpected Visitor (bearer must be a [gondor] companion) and Reckless Pride (a [dwarven] one).
					// Each: 'Each minion gains this ability: "Assignment: Assign this minion to bearer of <this card>."'
					// Their regroup abilities are not used here.
					put("visitor", "7_126");
					put("pride", "7_13");
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACompanionWithDefenderPlusOneIsAssignedTwoMinionsByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(3, scn.FreepsGetShadowAssignmentTargetCount());
		assertFalse(scn.IsCharAssigned(aragorn));

		// Two minions on the one companion, in the one answer.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop1, troop2));
		scn.FreepsAssignToMinions(aragorn, troop1, troop2);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertFalse(scn.IsCharAssigned(troop3));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(frodo));

		// The third minion is left over, and is the Shadow player's to assign.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop3));
		scn.ShadowAssignToMinions(gimli, troop3);

		// Aragorn's two minions are one skirmish.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionWhoAlreadyHasAMinionIsOfferedForAnotherOnlyIfHeHasDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var visitor = scn.GetFreepsCard("visitor");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, visitor);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With assignment actions the Shadow player puts one minion on each of the two companions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();
		scn.ShadowUseCardAction(troop1, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		scn.FreepsPass();
		scn.ShadowUseCardAction(troop2, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop2));
		scn.FreepsPass();
		scn.ShadowPass();

		// Each has one minion.  The Free Peoples player's assigning is one to one, so Gimli is not his to assign
		// again; Aragorn, with defender +1, is, to one additional unassigned minion.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop3));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gimli));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		scn.FreepsAssignToMinions(aragorn, troop3);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop3));
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop2));
		assertFalse(scn.IsCharAssignedAgainst(gimli, troop3));
		assertFalse(scn.IsCharAssigned(frodo));
		// No minion is left over, so the Shadow player is asked nothing.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ACompanionWithoutDefenderIsNotAssignedTwoMinionsByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(gimli, Keyword.DEFENDER));

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gimli));

		// One to one: either minion, and not the two together.  The same two are Aragorn's to take.
		assertTrue(scn.FreepsCanAssignAgainst(gimli, troop1));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, troop2));
		assertFalse(scn.FreepsCanAssignAgainst(gimli, troop1, troop2));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop1, troop2));

		scn.FreepsAssignToMinions(gimli, troop1);

		// With his one minion Gimli is the Free Peoples player's to assign no longer.
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop1));
		assertFalse(scn.FreepsCanAssign(gimli));
		assertFalse(scn.FreepsCanAssignAgainst(gimli, troop2));

		// The Shadow player's assigning of what is left over is the other exception, and has no such count.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(gimli, troop2, troop3));
		scn.ShadowAssignToMinions(gimli, troop2, troop3);

		assertTrue(scn.IsCharAssignedAgainst(gimli, troop1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop2));
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop3));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DefenderPlusOneIsOneAdditionalMinionAndNotAnyNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		// One minion, or two; not all three.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop3));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop1, troop2));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, troop1, troop2, troop3));

		scn.FreepsAssignToMinions(aragorn, troop1, troop2);

		// He has his two, and the third is not the Free Peoples player's to give him.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertFalse(scn.FreepsCanAssign(aragorn));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, troop3));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, troop3));

		// It is the Shadow player's, as a leftover minion.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, troop3));
		scn.ShadowAssignToMinions(aragorn, troop3);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop3));
		assertFalse(scn.IsCharAssigned(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// CR 4.2 "assignment phase": an assignment action assigns a minion to a companion only if "both of them are
	// unassigned".  Defender +1 is one more minion for the Free Peoples player to give when he assigns; it gives an
	// assignment action nothing, so the action that would assign nobody is not offered (Q-0034, F-0355).
	@Test
	public void AnAssignmentActionGivesACompanionWithDefenderNoSecondMinionAndTheFreePeoplesPlayerMayGiveHimOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));
		scn.ShadowUseCardAction(troop1, "Unexpected Visitor");
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));

		scn.FreepsPass();

		// Aragorn is assigned, so no other minion's action can assign to him, defender +1 or not.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));
		assertFalse(scn.ShadowActionAvailable(troop3, "Unexpected Visitor"));
		scn.ShadowPass();

		// The Free Peoples player's own assigning is where the keyword counts: one more minion, the one Aragorn
		// already has counted, and not two more.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop2));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, troop2, troop3));

		scn.FreepsAssignToMinions(aragorn, troop2);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertFalse(scn.IsCharAssigned(troop3));
		assertFalse(scn.FreepsCanAssign(aragorn));
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Eowyn, Northwoman: "Assignment: Exert Eowyn and assign a minion to the Ring-bearer."  The
					// Free Peoples player's own assignment action.  Her other texts need her in a skirmish, or
					// the minion she assigned.
					put("eowyn", "17_96");
					// Disquiet of Our People: "Maneuver: Exert a Dwarf to make that Dwarf defender +1 (or defender
					// +2 if you spot an Orc) until the regroup phase."
					put("disquiet", "2_2");
					// Reckless Pride (bearer must be a [dwarven] companion): each minion's own assignment action,
					// which is the Shadow player's.
					put("pride", "7_13");
					put("savage1", "1_151");    // Uruk Savage: an Uruk-hai, so no Orc is spotted; damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,       // Gimli, Bearer of Grudges: the only Dwarf, so the one Disquiet exerts
				VirtualTableScenario.RulingRing
		);
	}

	// The same of the Free Peoples player's own assignment action: it is an action and not his assigning, so the
	// Ring-bearer's defender +1 gives it no second minion to assign to him.
	@Test
	public void TheFreePeoplesPlayersOwnAssignmentActionGivesACompanionWithDefenderNoSecondMinionEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var disquiet = scn.GetFreepsCard("disquiet");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(gimli, pride);
		scn.MoveCardsToHand(disquiet);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(disquiet);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With the Ring-bearer unassigned Eowyn's action is hers to use.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsPass();

		scn.ShadowUseCardAction(savage1, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage1));

		// Now he is assigned, and there is an unassigned minion: the action would assign nobody, and is not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(savage2));
		assertFalse(scn.FreepsActionAvailable(eowyn));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		scn.FreepsPass();
		scn.ShadowPass();

		// In his own assigning the Free Peoples player may give the Ring-bearer the other minion.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, savage2));
		scn.FreepsAssignToMinions(gimli, savage2);
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage2));
	}
}
