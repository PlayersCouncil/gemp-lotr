package com.gempukku.lotro.mechanics.skirmishes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A character who may not be assigned to a skirmish, or not against a given character, is not assigned so by any
 * assigning.
 *
 * Comprehensive Rules 4.2, "assignment phase": the Free Peoples player assigns minions to his characters, and the
 * Shadow player then assigns the minions left over; a card may assign a character too.  The bars are the
 * definition words `cantBeAssignedToSkirmish` (Saruman, Black Traitor 4_173: "Saruman ... may not be assigned to a
 * skirmish") and `cantBeAssignedToSkirmishAgainst` (Uruk-hai Guard 12_156: "Assignment: Exert this minion and spot
 * a companion to prevent the Free Peoples player from assigning that companion to this minion").  Each is asked by
 * every assigning, through `Filters.assignableToSkirmish` and `ModifiersQuerying.isValidAssignments`: the Free
 * Peoples player's assignment (`FreePeoplePlayerAssignsMinionsGameProcess`), the Shadow player's
 * (`ShadowPlayerAssignsHisMinionsGameProcess`), and a card's (`assignFpCharacterToSkirmish`).  Each is here once,
 * with a printed card and a control: another character on the same board who is offered.
 *
 * <ul>
 * <li>The Free Peoples player's assignment, under each bar.</li>
 * <li>The Shadow player's assignment, under Saruman's.  (Uruk-hai Guard's bar names the Free Peoples player.)</li>
 * <li>A card's `assignFpCharacterToSkirmish`, under each bar: Crashing Cavalry 13_13 ("Assignment: Discard this
 * from play and assign a minion to an [elven] companion").</li>
 * </ul>
 *
 * Not here: `startSkirmish`, whose one user is A Dark Shape Sprang 10_19 ("... Begin a skirmish phase involving
 * Gollum and that companion"), asks the same filter of Gollum and the companion; whether a skirmish a card begins
 * is an assignment for a bar on being assigned is not ruled, and the board is not laid.
 *
 * Ketura on Q-0264 (2026-10-12): mechanics tests "should exist that do such a thing", "which will then ensure that
 * future cards using the bar only need to act as A": a card that lays this bar is tested with one assignment it
 * stops and its control.
 */
public class ABarOnBeingAssignedToASkirmishTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Saruman, Black Traitor: a Wizard minion, strength 8.  "Saruman may not take wounds during the
					// archery phase and may not be assigned to a skirmish."  His other texts are about being played
					// and a Shadow ability, never used.
					put("saruman", "4_173");
					// Uruk-hai Guard: an [uruk-hai] Uruk-hai, vitality 3; damage +1.  "Assignment: Exert this minion
					// and spot a companion to prevent the Free Peoples player from assigning that companion to this
					// minion."
					put("guard", "12_156");
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used
					// Arwen, Daughter of Elrond: an [elven] companion; her text is about skirmishing a Nazgûl.
					put("arwen", "1_30");
					// Crashing Cavalry: an [elven] condition of the support area.  "Assignment: Discard this from
					// play and assign a minion to an [elven] companion.  Those characters cannot use skirmish special
					// abilities until the start of the regroup phase."
					put("cavalry", "13_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFreePeoplesPlayerIsNotOfferedAMinionWhoCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1);
		scn.SkipToAssignments();

		// The Troop is offered to be assigned, and Saruman is not.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop1));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));

		scn.FreepsAssignToMinions(aragorn, troop1);
		assertTrue(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(saruman));
	}

	@Test
	public void TheShadowPlayerIsNotOfferedAMinionWhoCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();

		// Both minions are left over.  The Troop is offered to the Shadow player to be assigned, and Saruman is not.
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop1));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(saruman));

		scn.ShadowAssignToMinions(aragorn, troop1);
		assertTrue(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(saruman));
	}

	@Test
	public void ACardsAssignmentDoesNotOfferAMinionWhoCannotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var cavalry = scn.GetFreepsCard("cavalry");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(cavalry);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Arwen is the one [elven] companion.  The two Troops are offered to be assigned to her, and Saruman is
		// not.
		scn.FreepsUseCardAction(cavalry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(troop1);
		assertTrue(scn.IsCharAssigned(arwen));
		assertTrue(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(saruman));
	}

	@Test
	public void TheFreePeoplesPlayerCannotAssignACharacterAgainstAMinionHeIsBarredFrom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);

		var guard = scn.GetShadowCard("guard");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Aragorn is spotted: he is not to be assigned to the Guard.
		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsPass();
		scn.ShadowPass();

		// Aragorn may not be assigned to the Guard; he may be assigned to the Troop, and Arwen to the Guard.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(guard));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, guard));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, troop1));
		assertTrue(scn.FreepsCanAssignAgainst(arwen, guard));
	}

	@Test
	public void ACardsAssignmentDoesNotOfferAMinionTheCharacterIsBarredFrom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var cavalry = scn.GetFreepsCard("cavalry");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(cavalry);

		var guard = scn.GetShadowCard("guard");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Arwen is spotted: she is not to be assigned to the Guard.
		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(guard));

		// Arwen is the one [elven] companion.  The two Troops are offered to be assigned to her, and the Guard is
		// not.
		scn.FreepsUseCardAction(cavalry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCard(troop1);
		assertTrue(scn.IsCharAssigned(arwen));
		assertTrue(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(guard));
	}
}
