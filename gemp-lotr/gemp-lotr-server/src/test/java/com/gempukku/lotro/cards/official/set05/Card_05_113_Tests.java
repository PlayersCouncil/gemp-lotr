package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nouse", "5_113");
					put("nouse2", "5_113");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet; a standing strength bonus only

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers only a weapon played on him
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,  // Frodo, Son of Drogo: a [shire] Hobbit with the Frodo signet
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAragornSignetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nouse", "5_113");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet; a standing strength bonus only

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers only a weapon played on him
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Master of the Precious: a Frodo, a [shire] Hobbit, with the Aragorn signet; his fellowship
				// ability is used only on purpose
				"5_111",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoUseThatWayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: No Use That Way
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Add 3 burdens to wound a minion skirmishing a companion who has the Frodo signet twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nouse");

		assertEquals("No Use That Way", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsThreeBurdensToWoundAMinionSkirmishingACompanionWhoHasTheFrodoSignetTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nouse = scn.GetFreepsCard("nouse");
		var nouse2 = scn.GetFreepsCard("nouse2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(nouse, nouse2);

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop, soldier);
		// The Chieftain, vitality 2, is exhausted: he is offered to a wound, as he would not be to an exertion.
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, chieftain},
				new PhysicalCardImpl[]{frodo, soldier});
		scn.ShadowAssignToMinions(gandalf, troop);

		// Gandalf's skirmish; the Soldier waits for Frodo's.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertTrue(scn.IsCharAssigned(soldier));
		assertFalse(scn.IsCharSkirmishing(soldier));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(nouse));

		scn.FreepsPlayCard(nouse);
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Vitality 3: two wounds, and he is still in play.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.DISCARD, nouse);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Frodo's skirmish: he has the Frodo signet too.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, soldier));
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(nouse2);
		assertEquals(6, scn.GetBurdens());
		// Vitality 2: the second wound kills him.
		assertInZone(Zone.DISCARD, soldier, nouse2);
		// Frodo's was the last skirmish, and it is over with its only minion gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheSkirmishOfAFrodoWithoutTheFrodoSignetItIsPlayedForNothingAndTheBurdensAreStillAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAragornSignetFrodoScenario();

		var nouse = scn.GetFreepsCard("nouse");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(nouse);

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, troop},
				new PhysicalCardImpl[]{gandalf, chieftain});

		// Frodo, with the Aragorn signet, skirmishes first; Gandalf, who has the Frodo signet, waits.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop));
		assertTrue(scn.IsCharAssigned(chieftain));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(nouse));

		scn.FreepsPlayCard(nouse);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.DISCARD, nouse);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nouse = scn.GetFreepsCard("nouse");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(nouse);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.HAND, nouse);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nouse));
	}
}
