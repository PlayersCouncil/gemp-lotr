package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: strength +2 and no text, so that Frodo loses to the Orc Soldier by one

					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Shadow: Exert Gollum twice to exert the
					// Ring-bearer."  "Regroup: Exert Gollum twice to wound the Ring-bearer."  His other abilities
					// remove a burden to add twilight.
					put("gollum", "9_28");
					// Little Snuffler: [gollum] Shadow event, cost 2.  In region 1, "spot Gollum to wound a companion."
					put("snuffler1", "13_52");
					put("snuffler2", "13_52");
					put("snuffler3", "13_52");
					// Gate Picket: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert this minion twice to make
					// the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer and nothing else
					put("marksman2", "1_176");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					// Isengard Worker: [isengard] Orc, strength 5, vitality 3.  "Regroup: Exert this minion to make
					// the Free Peoples player wound a companion."
					put("worker", "3_62");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing
		);
	}

	@Test
	public void IsildursBaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The One Ring, Isildur's Bane
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: <b>Response:</b> If bearer is about to take a wound, he wears The One Ring until the regroup phase.
		 * While wearing The One Ring, each time the Ring-bearer is about to take a wound, add 2 burdens instead.
		 */

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Isildur's Bane", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsildursBaneConvertsWoundsTo2BurdensDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollum");
		var picket = scn.GetShadowCard("picket");
		var marksman = scn.GetShadowCard("marksman");
		var snuffler1 = scn.GetShadowCard("snuffler1");
		var snuffler2 = scn.GetShadowCard("snuffler2");
		var snuffler3 = scn.GetShadowCard("snuffler3");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(snuffler1, snuffler2, snuffler3);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, picket, marksman);
		// Three Little Snufflers at 2 each.
		scn.SetTwilight(6);

		// Frodo, Son of Drogo is strength 3 and vitality 4: the Ring adds 1 to each.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetVitality(frodo));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());

		// An exertion is no wound: Gollum exerts the Ring-bearer and the Ring has nothing to say.
		scn.ShadowUseCardAction(gollum);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Sam does not bear the Ring: Little Snuffler's wound on him is a wound, and nothing is offered.
		scn.ShadowPlayCard(snuffler1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same wound on Frodo: the Ring's response is offered.
		scn.ShadowPlayCard(snuffler2);
		scn.ShadowChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertFalse(scn.RBWearingOneRing());

		// He puts the Ring on, and the wound that was answered is itself 2 burdens.
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Worn: the next wound is 2 more burdens, and the response is not offered a second time.
		scn.ShadowPlayCard(snuffler3);
		scn.ShadowChooseCard(frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The maneuver phase.  The Free Peoples player names no culture, so Gate Picket wounds Frodo and Sam:
		// 2 burdens for Frodo's wound, and a wound for Sam.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertEquals(6, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase: the Marksman's wound, placed on Frodo.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(8, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PutOnInTheArcheryPhaseTheRingTurnsSkirmishWoundsIntoBurdensAndComesOffAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, soldier);
		scn.SkipToArcheryWounds();

		// 3 + 1 for the Ring + 2 for the sword
		assertEquals(6, scn.GetStrength(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, marksman}, new PhysicalCardImpl[]{frodo, soldier});

		// One skirmish is fought and over: Aragorn's 8 kills the Marksman.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, marksman);

		// In the skirmish after it the Ring is still on.  The Soldier's own wound is 2 burdens ...
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and so is the wound for losing, 6 against 7.
		scn.PassSkirmishActions();
		assertEquals(6, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void IsildursBaneRespondsToAnyWoundToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var picket = scn.GetShadowCard("picket");

		scn.StartGame();
		scn.MoveMinionsToTable(picket);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());

		// The Free Peoples player names no culture, so Gate Picket wounds Frodo.
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nobody fights: still worn when the assignments are over, and off at the regroup phase.
		scn.SkipToAssignments();
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void DecliningTheResponseLeavesTheWoundAWoundAndTheNextWoundAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		scn.StartGame();
		scn.MoveMinionsToTable("marksman", "marksman2");
		// Two archers, and Frodo the only companion: both wounds are his.
		scn.SkipToArcheryWounds();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());

		// The second wound asks again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PutOnInTheRegroupPhaseTheRingComesOffAtTheEndOfThatRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gollum = scn.GetShadowCard("gollum");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, worker);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());

		// Gollum wounds the Ring-bearer in the regroup phase, and the Ring is put on there.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// It is not taken off at once: the Worker's wound, later in the same regroup phase, is 2 burdens.
		scn.FreepsPass();
		scn.ShadowUseCardAction(worker);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.RBWearingOneRing());

		// Still on when the Free Peoples player is asked about another move, and off once that regroup phase is over.
		scn.SkipToMovementDecision();
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}
}
