package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Game text given to another card "until ..." is a snapshot, as copied keywords are.
 *
 * Ketura, 2026-10-07, on War Howdah 103_62 ("Until the end of the turn, that Southron gains the game text of that
 * stacked Man", F-0160): "War Howdah should keep the text and keywords, even if the Man is later lost."
 * Comprehensive Rules 4.2, "game text": a boldfaced keyword in the text box, number included, is game text.
 *
 * The card's own tests show the Southron keeping both when the Man is discarded from the stack.  What they cannot
 * show is the far side of the time named: the Southron is a minion until the end of the turn and is discarded
 * before it ends.  That is shown here with a card of the tests' own, Test Mirror 0_8005, which gives one minion's
 * game text to another until the assignment phase.
 */
public class GameTextSnapshotTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Mirror (the tests' own card): "Maneuver: Choose your minion and another of your minions.
					// Until the assignment phase, the second gains the game text of the first."
					put("mirror", "0_8005");
					// Southron Traveler: vitality 1. "Southron. Ambush (2). Response: If a Southron is about to
					// take a wound, remove (3) to prevent that wound."
					put("traveler", "5_76");
					put("warrior1", "4_222");   // Desert Warrior: Southron, strength 6, vitality 2; may remove (3) to wound at the start of his own skirmish
					put("warrior2", "4_222");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("bow", "1_90");         // Aragorn's Bow: he is an archer; an archery ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: Archer; an archery ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GameTextAndKeywordsTakenAsASnapshotStayWhenTheirCardIsGoneAndEndWhenTheTimeIsUp() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, bow);

		var mirror = scn.GetShadowCard("mirror");
		var traveler = scn.GetShadowCard("traveler");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		scn.MoveCardsToSupportArea(mirror);

		scn.StartGame();
		scn.MoveMinionsToTable(traveler, warrior1, warrior2);
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(warrior1, Keyword.AMBUSH));

		scn.ShadowUseCardAction(mirror);
		scn.ShadowChooseCard(traveler);
		scn.ShadowChooseCard(warrior1);

		// The keyword comes with its number, and the other Warrior gains nothing.
		assertEquals(2, scn.GetKeywordCount(warrior1, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(warrior2, Keyword.AMBUSH));

		// Two arrows from the fellowship.  The first kills the Traveler: the response is declined.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(traveler);
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, traveler);
		// The Warrior keeps what was taken from the Traveler: the keyword, and the response, which prevents the
		// second arrow's wound.
		assertEquals(2, scn.GetKeywordCount(warrior1, Keyword.AMBUSH));

		int twilight = scn.GetTwilight();
		scn.ShadowChooseCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior1));
		scn.ShadowChooseOptionalTrigger(warrior1);

		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(warrior1));

		// The assignment phase: the time is up, and the Warrior has neither.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetKeywordCount(warrior1, Keyword.AMBUSH));

		scn.FreepsAssignToMinions(aragorn, warrior1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// The Warrior's own trigger at the start of his skirmish
		scn.ShadowDeclineOptionalTrigger();

		// Aragorn (8) beats the Warrior (6), a Southron about to take a wound with (3) and more in the pool: no
		// response is offered, and the wound is taken.
		twilight = scn.GetTwilight();
		assertTrue(twilight >= 3);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(warrior1));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
