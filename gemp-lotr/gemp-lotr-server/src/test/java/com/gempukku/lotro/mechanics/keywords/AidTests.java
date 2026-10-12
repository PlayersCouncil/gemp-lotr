package com.gempukku.lotro.mechanics.keywords;

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
 *
 * Aid is an action with a cost, and is offered whether or not the follower can then go anywhere (F-0363; Ketura,
 * 2026-10-10: "such followers should be marked as noeffect when their aid is offered, so that the UI can indicate
 * it won't do anything, but if they *want* the burden or whatever (and many such decks do), they can still attempt
 * to pay it and get nothing out of it, just like any other legal cost-with-no-effect action").  While no character
 * can be given the follower the offer carries the no-effect flag; taken, its cost is paid, nobody is asked for a
 * character and nothing moves.  Shown at Streets of Bree 18_140 ("While the fellowship is at this site, followers
 * cannot be transferred to a character"), and with the follower already borne by its owner's only character
 * (Q-0244).
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

	// The same cards on the same path, with Streets of Bree in each adventure deck in place of a site no test
	// reaches.  It has no number: MoveCardToAdventurePath lays it as the site ahead.
	protected VirtualTableScenario GetStreetsOfBreeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pallando", "13_37");
					put("gandalf", "1_72");     // strength 7 beside Frodo alone
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					// Streets of Bree: Shadows-block site, dwelling.  "While the fellowship is at this site,
					// followers cannot be transferred to a character."
					put("streets", "18_140");
				}},
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
		assertEquals("false", scn.FreepsActionNoEffect(pallando));
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
		// Frodo and Aragorn could each be given him.
		assertEquals("false", scn.FreepsActionNoEffect(pallando));
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

	@Test
	public void WhereFollowersCannotBeTransferredTheAidIsOfferedWithTheNoEffectFlagAndItsCostBuysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsOfBreeScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveCardToAdventurePath(streets);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(streets, scn.GetCurrentSite());

		// The Aid is offered all the same, and says beforehand that it will do nothing.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertEquals("effect", scn.FreepsActionNoEffect(pallando));

		scn.FreepsAcceptOptionalTrigger();

		// Its cost is paid, nobody is asked for a character, and the follower stays where it was.
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, pallando);
		assertNotAttachedTo(pallando, gandalf);
		assertEquals(7, scn.GetStrength(gandalf));
	}

	@Test
	public void OnceTheFellowshipHasLeftThatSiteTheAidIsNotFlaggedAndTransfersTheFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsOfBreeScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveCardToAdventurePath(streets);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(streets, scn.GetCurrentSite());
		assertEquals("effect", scn.FreepsActionNoEffect(pallando));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, pallando);

		// Nobody is assigned to the Troop, and the fellowship moves on in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNotSame(streets, scn.GetCurrentSite());

		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertEquals("false", scn.FreepsActionNoEffect(pallando));

		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		scn.FreepsChooseCard(gandalf);

		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(pallando, gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AFollowerBorneByItsOwnersOnlyCharacterHasNobodyToGoToAndItsAidIsFlaggedAndMovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		// The first maneuver phase.  Frodo is the only character there is, so nobody is asked who is to bear the
		// follower.  He is no [gandalf] companion: Pallando's own text exerts him twice.
		assertEquals("false", scn.FreepsActionNoEffect(pallando));
		scn.FreepsAcceptOptionalTrigger();

		assertAttachedTo(pallando, frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second maneuver phase in the same turn.  The Aid is offered again, and there is nobody else to give
		// the follower to.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertEquals("effect", scn.FreepsActionNoEffect(pallando));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertAttachedTo(pallando, frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
	}
}
