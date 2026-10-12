package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The keyword unhasty.
 *
 * Comprehensive Rules 4.2, "unhasty": "The Free Peoples player cannot assign a character with this keyword to a
 * skirmish unless it is at its home site (see ally) or when a [Gandalf] card allows it do so.  Shadow players may
 * assign any leftover unassigned minions to unhasty companions."
 *
 * The [gandalf] cards that allow it are the Ents' own: Birchseed, Tall Statesman 5_15 and Forest Guardian 5_17 each
 * have "Assignment: Exert an unbound Hobbit or discard 2 cards from hand to allow <this companion> to skirmish."
 * How long that lasts is each Ent's own to show.  Treebeard, Earthborn 4_103 is the unhasty ally; his home is
 * sites 2 and 8 of the Towers path.
 */
public class UnhastyTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Birchseed, Tall Statesman: unhasty Ent companion, strength 8, vitality 4.  "Assignment: Exert an
					// unbound Hobbit or discard 2 cards from hand to allow Birchseed to skirmish."  His fellowship
					// ability is not used here.
					put("birchseed", "5_15");
					// Forest Guardian: unhasty Ent companion with the same assignment ability, for itself
					put("guardian", "5_17");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: not unhasty; a maneuver ability, not used here
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit, vitality 4; no weapon here, so no text
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Treebeard, Earthborn: unhasty Ent ally, home sites 2T and 8T, strength 12, vitality 4.  His
					// response needs an unbound Hobbit about to be discarded and his fellowship ability one stacked
					// on him: there is neither here.
					put("treebeard", "4_103");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: not unhasty; a maneuver ability, not used here
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
				}},
				// Site 2 is Eastfold 4_331 (a Shadow ability that needs 3 [isengard] trackers); site 3 is Barrows of
				// Edoras 4_337 (a fellowship ability used only on purpose).
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFreePeoplesPlayerMayNotAssignAnUnhastyCompanionAndTheShadowPlayerMayAssignALeftoverMinionToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var birchseed = scn.GetFreepsCard("birchseed");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(birchseed, aragorn);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);

		assertTrue(scn.HasKeyword(birchseed, Keyword.UNHASTY));
		assertFalse(scn.HasKeyword(aragorn, Keyword.UNHASTY));

		scn.SkipToAssignments();

		// The other companions are the Free Peoples player's to assign; the unhasty one is not.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(birchseed));
		assertFalse(scn.FreepsCanAssign(birchseed));
		assertTrue(scn.ShadowCanAssign(birchseed));

		scn.FreepsAssignToMinions(aragorn, troop1);

		// The Shadow player has the leftover minion, and may give it to the unhasty companion.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(3, scn.ShadowGetFreepsAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(birchseed));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.IsCharAssigned(birchseed));

		scn.ShadowAssignToMinions(birchseed, troop2);
		assertTrue(scn.IsCharAssignedAgainst(birchseed, troop2));

		// And that skirmish is fought: Birchseed, 8, loses to the Troop's 13.
		assertEquals(0, scn.GetWoundsOn(birchseed));
		scn.FreepsResolveSkirmish(birchseed);
		assertTrue(scn.IsCharSkirmishingAgainst(birchseed, troop2));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(birchseed));
		assertEquals(0, scn.GetWoundsOn(troop2));
		// Aragorn's skirmish is still to come.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheFreePeoplesPlayerMayAssignAnUnhastyCompanionOnceACardAllowsHimToSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var birchseed = scn.GetFreepsCard("birchseed");
		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(birchseed, guardian, merry);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(birchseed));
		assertFalse(scn.FreepsCanAssign(guardian));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(birchseed));

		// Birchseed's own ability: the hand is empty, so the cost is the exertion, and Merry is the one unbound Hobbit.
		scn.FreepsUseCardAction(birchseed);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.HasKeyword(birchseed, Keyword.UNHASTY));
		assertTrue(scn.FreepsCanAssign(birchseed));
		assertFalse(scn.FreepsCanAssign(guardian));

		scn.PassCurrentPhaseActions();

		// Birchseed is now among those the Free Peoples player may assign; the other unhasty Ent is still not.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(3, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(birchseed));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(guardian));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merry));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		scn.FreepsAssignToMinions(birchseed, troop1);
		assertTrue(scn.IsCharAssignedAgainst(birchseed, troop1));
		assertFalse(scn.IsCharAssigned(guardian));

		// The minion left over is the Shadow player's, who is not held to any of this.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(guardian));
		scn.ShadowDeclineAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheFreePeoplesPlayerMayAssignAnUnhastyAllyAtHisHomeSiteAndNotAwayFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(treebeard);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);

		assertTrue(scn.HasKeyword(treebeard, Keyword.UNHASTY));

		// The fellowship moves to site 2, Treebeard's home.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));

		scn.FreepsAssignToMinions(treebeard, troop1);
		assertTrue(scn.IsCharAssignedAgainst(treebeard, troop1));

		// Treebeard, 12, loses to the Troop's 13; both live.
		scn.FreepsResolveSkirmish(treebeard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(treebeard));

		// The fellowship moves again, to site 3, with the same minion still in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// Away from home he is nobody's to assign, as any ally is.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop1));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(treebeard));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(treebeard));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
	}
}
