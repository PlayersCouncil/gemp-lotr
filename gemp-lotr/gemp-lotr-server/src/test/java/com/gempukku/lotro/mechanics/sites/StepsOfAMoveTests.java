package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The order in which the steps of a move are carried out.
 *
 * Comprehensive Rules 4.2, "moving your fellowship": "When you move, perform actions as follows: A Shadow player
 * places the next site card from their adventure deck, if needed.  Perform "When you move from..." actions.
 * Perform "When the fellowship moves..." actions.  Move your player marker to the next site.  Perform "When you
 * move to..." actions.  Add twilight tokens equal to the new site's Shadow number", then for the region and for
 * each companion.  And: "After you have completed any moving actions and have added twilight tokens to the
 * twilight pool, the fellowship has moved to the new site".
 *
 * So a text that answers "the fellowship moves" is carried out with the fellowship still at the site it is
 * leaving: the marker is there, that site's text is the one in force, and the move has not yet been counted.
 * Ketura on Q-0184 (Race Text 92_18, "... (or 2 if at site 7 or higher)", on the move from site 6), 2026-10-09:
 * "I think A is the correct reading as per that movement breakdown you cited. However, if the engine is doing
 * that in the wrong order, that needs adjusting, rather than switching the trigger to 'moves from'."  Until then
 * the game moved the marker before it raised "the fellowship moves".
 *
 * What waits for the twilight at the far end of a move is in AtASiteWhenTheMoveIsFinishedTests.
 */
public class StepsOfAMoveTests
{
	// Council Courtyard at 3: "When the fellowship moves from Council Courtyard, remove (2)."  Dwarrowdelf Chamber
	// at 4 (Shadow number 2): "When the fellowship moves to Dwarrowdelf Chamber, Gimli or 2 other companions must
	// exert."  The other sites are the usual Fellowship ones.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Under the Watching Eye: "Each time the fellowship moves, the Free Peoples player must exert a
					// companion."  With two companions or more it stops the move to ask which.
					put("eye", "1_281");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Fellowship ability used only on purpose
				}},
				new HashMap<>() {{
					put("site1", "1_319");  // Bag End, 0
					put("site2", "1_331");  // Ettenmoors, 2
					put("site3", "1_337");  // Council Courtyard, 0; a sanctuary
					put("site4", "1_344");  // Dwarrowdelf Chamber, 2
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A King path with Steward's Tomb at 5: "Wounds cannot be prevented or healed.  Burdens cannot be removed."
	// Osgiliath Fallen at 4 has a Skirmish ability only; Minas Tirith Seventh Circle at 6 answers a move FROM it.
	protected VirtualTableScenario GetTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: an Archery ability only
					// Legolas' Sword: "Each time the fellowship moves, you may spot a wounded minion to heal Legolas."
					put("sword", "5_12");
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");  // Osgiliath Fallen
					put("site5", "10_119"); // Steward's Tomb
					put("site6", "7_350");  // Minas Tirith Seventh Circle
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhenTheFellowshipMovesIsAnsweredBeforeTheMarkerIsMoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var eye = scn.GetShadowCard("eye");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(eye);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetMoveCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		// Under the Watching Eye is asking which companion exerts.  The marker has not moved, the move is not
		// yet counted, and nothing has been added for it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(scn.GetSite(1), scn.GetCurrentSite());
		assertEquals(0, scn.GetMoveCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChooseCard(aragorn);

		// Then the marker, and last the twilight: 2 for Ettenmoors and 2 for the companions.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovesFromThenTheFellowshipMovesThenTheMarkerThenMovesToThenTheTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var eye = scn.GetShadowCard("eye");

		scn.StartGame();
		// Frodo alone on the way, and nothing that answers a move.
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(eye);
		scn.SetTwilight(5);

		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.FreepsPass();

		// "When you move from": Council Courtyard has removed its 2 already.
		// "When the fellowship moves": Under the Watching Eye asks for one companion, at site 3 still.
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsChooseCard(aragorn);

		// The marker, and then "When you move to": Dwarrowdelf Chamber asks for two companions, with no Gimli.
		// No twilight has been added for the move.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.GetTwilight());

		scn.FreepsChooseCards(frodo, sam);

		// The twilight last: 2 for Dwarrowdelf Chamber and 3 for the companions.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3 + 2 + 3, scn.GetTwilight());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTextOfTheSiteMovedToIsNotYetInForceWhenTheFellowshipMovesIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, sword);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The move in the regroup phase, from Osgiliath Fallen to Steward's Tomb.  The Sword's heal is offered
		// with the fellowship still at site 4.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsAcceptOptionalTrigger();

		// "Wounds cannot be ... healed" is the text of the site ahead: Legolas is healed.
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTextOfTheSiteLeftIsStillInForceWhenTheFellowshipMovesIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, sword);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The same move one site on, from Steward's Tomb.  "Wounds cannot be ... healed" still holds while "the
		// fellowship moves" is answered: the Sword's heal could heal nobody, it costs nothing (a spot is no cost),
		// and so its offer is not made (Q-0034; withheld by the engine since Q-0224, which asks the heal itself).
		// Legolas keeps his wound, and the fellowship moves on.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
