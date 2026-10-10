package com.gempukku.lotro.mechanics;

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
 * Followers and the keyword aid.
 *
 * Comprehensive Rules 4.2, "follower": "A follower is played to your support area.  While your follower is in your
 * support area, it has no effect on the game.  Each follower has the keyword 'Aid' which you can use to transfer
 * that follower to one of your characters.  When your follower is borne by a character, that follower will provide
 * an ongoing effect ...  During the regroup phase, each follower is returned to its owner's support area (at no
 * cost) only after the Free Peoples player has reconciled, but before the Shadow player discards all minions in
 * play."
 *
 * "Aid": "At the start of the maneuver phase, you may pay the aid cost to transfer that follower to your character.
 * ...  The follower is borne by the character for the rest of the turn, no matter how often the fellowship moves.
 * You don't have to pay the aid cost again for a follower borne by a character in a subsequent maneuver phase."
 *
 * The follower here is Pallando, Far-travelling One 13_37: strength +1, "Aid - (2)".  Transferred to a [gandalf]
 * companion his own text asks nothing ("Each time you transfer this to a companion, except a [gandalf] companion,
 * exert bearer twice"), so Gandalf is who bears him.
 */
public class AidTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pallando, Far-travelling One: [gandalf] follower, cost 2, strength +1, Aid - (2).  His other
					// trigger needs his bearer to win a skirmish, and nobody fights here.
					put("pallando", "13_37");
					// Gandalf, Friend of the Shirefolk: strength 6, +1 for each of Hobbit, Dwarf, Elf and Man in
					// the fellowship: 8 beside Frodo and Aragorn
					put("gandalf", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability, not used here
					put("troop", "1_177");      // Goblin Patrol Troop: no text at all; there so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseAFollowerIsTransferredToACompanionForItsAidCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(pallando);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(gandalf));

		// Played, the follower lies in the support area and does nothing there.
		scn.FreepsPlayCard(pallando);

		assertInZone(Zone.SUPPORT, pallando);
		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		// At the start of the maneuver phase its owner may pay the aid cost.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		// Aid - (2).  Borne, the follower gives its bearer what it has to give, and nobody else.
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(pallando, gandalf);
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheAidCostNotPaidTheFollowerStaysInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(pool, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pallando);
		assertNotAttachedTo(pallando, gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ABorneFollowerStaysOnItsBearerThroughASecondMoveAndReturnsToTheSupportAreaAsTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(pallando, gandalf);
		assertEquals(9, scn.GetStrength(gandalf));

		// Nobody is assigned to the Troop.  The first regroup phase of the turn: the fellowship moves on, and the
		// follower stays where it is.
		scn.SkipToMovementDecision();
		assertAttachedTo(pallando, gandalf);
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());

		// A second maneuver phase in the same turn.  The aid may be paid again, and need not be: unpaid, the
		// follower is still borne.
		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(pool, scn.GetTwilight());
		assertAttachedTo(pallando, gandalf);
		assertEquals(9, scn.GetStrength(gandalf));

		// The last regroup phase of the turn.  Still borne while regroup actions may be taken.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertAttachedTo(pallando, gandalf);

		// The fellowship has moved twice, so the turn ends here: both players reconcile, and the follower goes
		// back to its owner's support area at no cost.
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P2());
		scn.PassReconcile(scn.P1());

		assertInZone(Zone.SUPPORT, pallando);
		assertNotAttachedTo(pallando, gandalf);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
