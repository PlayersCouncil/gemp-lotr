package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servant", "18_52");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, the [gondor] companion who is no Man
					put("guard", "5_83");         // Household Guard: a [rohan] Man, the Man who is not [gondor]; valiant and nothing else
					// Rohan Stable Master: another follower, Aid (1), with no transfer text of its own
					put("stablemaster", "15_134");
					put("troop", "1_177");        // Goblin Patrol Troop: no text; only there so that a maneuver phase is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianServantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gondorian Servant, Denethor's Handman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Follower
		 * Subtype:
		 * Resistance: +3
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may add (1) to transfer this to a companion.)</helper><br>Each time you attach this to a companion, except a [gondor] Man, add (4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servant");

		assertEquals("Gondorian Servant", card.getBlueprint().getTitle());
		assertEquals("Denethor's Handman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getResistance());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void AttachedByItsAidToAGondorCompanionWhoIsNoManItAddsFourBesideTheAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToSupportArea(servant);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, servant);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(servant));
		scn.FreepsAcceptOptionalTrigger();

		// Any companion may be given him, a [gondor] Man included.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, pippin, guard));
		scn.FreepsChooseCard(pippin);

		// 1 for the Aid + 4 for a companion who is no [gondor] Man
		assertAttachedTo(servant, pippin);
		assertEquals(pool + 1 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedToAManWhoIsNotGondorItAddsFourAndAnotherFollowerAttachedAddsNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var servant = scn.GetFreepsCard("servant");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToSupportArea(servant, stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, servant, stablemaster);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(servant));
		scn.FreepsChooseOptionalTrigger(servant);
		scn.FreepsChooseCard(guard);

		// 1 for the Aid + 4 for a [rohan] Man
		assertAttachedTo(servant, guard);
		assertEquals(pool + 1 + 4, scn.GetTwilight());

		// The other follower goes to Pippin, to whom the Servant would add (4): only its own Aid (1) is added.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stablemaster));
		scn.FreepsChooseOptionalTrigger(stablemaster);
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(stablemaster, pippin);
		assertEquals(pool + 5 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedToAGondorManItAddsOnlyTheAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(servant);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, servant);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(servant));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		// The Aid's (1), and nothing for a [gondor] Man.
		assertAttachedTo(servant, aragorn);
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedAgainInASecondManeuverPhaseItAddsFourAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToSupportArea(servant);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, servant);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(servant, pippin);
		assertEquals(pool + 1 + 4, scn.GetTwilight());

		// Nobody is assigned to the Troop. The fellowship moves again, and a second maneuver phase is played.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);

		pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(servant));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(guard);

		// 1 for the Aid + 4 again, for the [rohan] Man
		assertAttachedTo(servant, guard);
		assertEquals(pool + 1 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
