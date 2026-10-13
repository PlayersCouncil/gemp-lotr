package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The keyword ambush (X).
 *
 * Comprehensive Rules 4.2, "ambush": "When the Free Peoples player assigns one of his characters to skirmish a
 * minion with the keyword 'ambush (X),' the Shadow player who owns that minion may add (x). ...  The Free Peoples
 * player may decide not to assign any characters to your minion with ambush, allowing you to assign it later in the
 * assignment phase."
 *
 * The same entry has each instance of the keyword on one minion resolved separately ("the Shadow player may add
 * (1), (5), neither, or both (as separate actions)").  The game knowingly does not: separate instances are offered
 * as one trigger for their sum (Ketura, 2026-10-04, F-0010: "nobody is ever going to want two of them but not
 * three").
 */
public class AmbushTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability, not used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: his text is about being overwhelmed
					put("soldier", "4_254");    // Southron Soldier: [raider] Man; "Southron. Ambush (2)." is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: no text at all, so no ambush
					// Raider Bow: "Bearer must be a [raider] Man. Bearer is an archer and ambush (5)."
					put("bow", "7_155");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhenTheFreePeoplesPlayerAssignsACompanionToAnAmbushMinionItsOwnerMayAddThatMuchTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);

		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(troop, Keyword.AMBUSH));

		scn.SkipToAssignments();
		int pool = scn.GetTwilight();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// A companion to each minion, the one with ambush and the one without.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});

		// The offer is the minion's owner's, for the ambush minion alone; nothing is added before he takes it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troop));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(pool, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();

		// Ambush (2).
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, soldier));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DecliningAmbushAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		int pool = scn.GetTwilight();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, soldier));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnAmbushMinionTheShadowPlayerAssignsHimselfAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		int pool = scn.GetTwilight();

		// The Free Peoples player defends against the other minion only: no offer.
		scn.FreepsAssignToMinions(aragorn, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(soldier));

		// The Shadow player assigns the ambush minion himself: no offer either.
		scn.ShadowAssignToMinions(boromir, soldier);

		assertTrue(scn.IsCharAssignedAgainst(boromir, soldier));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwoInstancesOfAmbushOnOneMinionAreOfferedAsOneForTheirSum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var bow = scn.GetShadowCard("bow");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.SkipToAssignments();
		// The Bow makes its bearer an archer too, so it goes on once the archery phase is over.
		scn.AttachCardsTo(soldier, bow);
		int pool = scn.GetTwilight();

		// Ambush (2) printed and ambush (5) from the Bow.
		assertEquals(7, scn.GetKeywordCount(soldier, Keyword.AMBUSH));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertEquals(pool, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();

		// One offer, for 2 + 5; no second one follows it.
		assertEquals(pool + 7, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
