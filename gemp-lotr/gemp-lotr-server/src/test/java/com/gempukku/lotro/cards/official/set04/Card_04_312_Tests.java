package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_312_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("affairs", "4_312");
					put("affairs2", "4_312");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet; a standing strength bonus only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit with the Aragorn signet; abilities used only on purpose

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; answers only a weapon played on him
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,  // Frodo, Son of Drogo: has the Frodo signet
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("affairs", "4_312");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard with the Frodo signet; a standing strength bonus only
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion of another title than Frodo
					// Frodo, Frenzied Fighter: the one Frodo who can be in play beside another Ring-bearer; a skirmish
					// ability never used
					put("frodo", "13_149");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,  // Gimli, Bearer of Grudges: a Ring-bearer who is not Frodo
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MindYourOwnAffairsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Mind Your Own Affairs
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot Frodo and add a burden to wound a minion skirmishing a companion who has the
		 * 	Frodo signet.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("affairs");

		assertEquals("Mind Your Own Affairs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenToWoundAMinionSkirmishingACompanionWhoHasTheFrodoSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var affairs = scn.GetFreepsCard("affairs");
		var affairs2 = scn.GetFreepsCard("affairs2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(affairs, affairs2);

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop, soldier);
		// The Chieftain, vitality 2, is exhausted: a wound kills him.
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
		assertTrue(scn.FreepsPlayAvailable(affairs));

		scn.FreepsPlayCard(affairs);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);

		// A wound, not an exertion: the exhausted Chieftain is killed.
		assertInZone(Zone.DISCARD, chieftain, affairs);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Frodo's skirmish: he has the Frodo signet too.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, soldier));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(affairs2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.DISCARD, affairs2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfACompanionWithoutTheFrodoSignetItIsPlayedForNothingAndTheBurdenIsStillAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var affairs = scn.GetFreepsCard("affairs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveCardsToHand(affairs);

		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, troop},
				new PhysicalCardImpl[]{gandalf, chieftain});

		// Sam, a [shire] Hobbit with the Aragorn signet, skirmishes first; Gandalf, who has the Frodo signet, waits.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop));
		assertTrue(scn.IsCharAssigned(chieftain));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(affairs));

		scn.FreepsPlayCard(affairs);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.DISCARD, affairs);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutFrodoThoughAShireCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var affairs = scn.GetFreepsCard("affairs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveCardsToHand(affairs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.HAND, affairs);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertInZone(Zone.HAND, affairs);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(affairs));
	}

	@Test
	public void FrodoWhoIsNotTheRingBearerIsSpottedAndTheBurdenGoesOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var affairs = scn.GetFreepsCard("affairs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetFreepsCard("frodo");
		scn.MoveCompanionsToTable(gandalf, frodo);
		scn.MoveCardsToHand(affairs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(affairs));

		scn.FreepsPlayCard(affairs);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
