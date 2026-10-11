package com.gempukku.lotro.cards.official.set11;

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

public class Card_11_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: strength +2 and no text, so that Frodo loses to the Orc Soldier and is not overwhelmed

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
				"11_1"
		);
	}

	@Test
	public void RingOfRingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The One Ring, The Ring of Rings
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Resistance: +2
		 * Game Text: <b>Response:</b> If the Ring-bearer is about to take a wound, he or she wears The One Ring until the regroup phase.
		 * While the Ring-bearer is wearing The One Ring, each time he or she is about to take a wound, add a burden instead.
		 */

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Ring of Rings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getResistance());
	}

	@Test
	public void RingOfRingsConvertsWoundsDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Frodo, Son of Drogo is resistance 10, and the Ring adds 2.  It prints no strength and no vitality.
		assertEquals(12, scn.GetResistance(frodo));
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(4, scn.GetVitality(frodo));

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

		// He puts the Ring on, and the wound that was answered is itself a burden.
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(11, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Worn: the next wound is one more burden, and the response is not offered a second time.
		scn.ShadowPlayCard(snuffler3);
		scn.ShadowChooseCard(frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The maneuver phase.  The Free Peoples player names no culture, so Gate Picket wounds Frodo and Sam:
		// a burden for Frodo's wound, and a wound for Sam.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase: the Marksman's wound, placed on Frodo.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PutOnInTheArcheryPhaseTheRingTurnsSkirmishWoundsIntoBurdensComesOffAtTheRegroupPhaseAndAnswersAWoundThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var gollum = scn.GetShadowCard("gollum");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, soldier, gollum, worker);
		scn.SkipToArcheryWounds();

		// 3 + 2 for the sword
		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, marksman}, new PhysicalCardImpl[]{frodo, soldier});
		// Gollum and the Worker are kept out of the fighting, for the regroup phase.
		scn.ShadowDeclineAssignments();

		// One skirmish is fought and over: Aragorn's 8 kills the Marksman.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, marksman);

		// In the skirmish after it the Ring is still on.  The Soldier's own wound is a burden ...
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and so is the wound for losing, 5 against 7.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// Off, it answers a wound again, in the regroup phase as in any other: Gollum's.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// And the Worker's wound, later in that phase, is a burden with nothing asked.
		scn.FreepsPass();
		scn.ShadowUseCardAction(worker);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RingOfRingsRespondsToAnyWoundToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(1, scn.GetBurdens());
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
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
