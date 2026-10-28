package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loud", "12_9");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4; "To play, spot a Dwarf" is its whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F; Maneuver: exert him to let him skirmish until the regroup phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					put("dwarvenaxe", "1_9");   // Dwarven Axe: a [dwarven] possession, held in hand
					put("axestrike", "1_3");    // Axe Strike: a [dwarven] skirmish event, held in hand
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession, held in hand
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] companion, held in hand
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, not fierce, no text
					put("troop2", "1_177");
					put("fighter", "1_146");    // Uruk Fighter: [isengard] Uruk-hai, not fierce; its maneuver ability needs 5 companions
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul printed fierce, strength 8 +1 for each companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoudAndStrongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Loud and Strong
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard a [dwarven] card from hand and spot a minion skirmishing a Dwarf to make that minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("loud");

		assertEquals("Loud and Strong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsADwarvenCardFromHandToMakeTheChosenMinionSkirmishingTheDwarfFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loud = scn.GetFreepsCard("loud");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var axestrike = scn.GetFreepsCard("axestrike");
		var sword = scn.GetFreepsCard("sword");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveCardsToSupportArea(loud);
		scn.MoveCardsToHand(dwarvenaxe, axestrike, sword, farin);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var fighter = scn.GetShadowCard("fighter");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, loud);
		assertInHand(dwarvenaxe, axestrike, sword, farin);
		scn.MoveMinionsToTable(troop, fighter, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{gimli, troop2});
		// The Shadow player puts the Fighter on the Guard as well: two minions of other cultures and races in one skirmish.
		scn.ShadowAssignToMinions(guard, fighter);
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop2, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(loud));
		scn.FreepsUseCardAction(loud);

		// The cost: a [dwarven] card of any kind from hand (an item, an event, a character), not a [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dwarvenaxe, axestrike, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axestrike);
		assertInZone(Zone.DISCARD, axestrike);
		assertInHand(dwarvenaxe, sword, farin);

		// The minion: either of the two skirmishing the Guard, not the second Troop, assigned to Gimli and still waiting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, fighter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(fighter);

		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop2, Keyword.FIERCE));
		assertInZone(Zone.SUPPORT, loud);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		// Still fierce in the next skirmish and into the fierce round.
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));
		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		// Gone with the start of the regroup phase.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyInASkirmishIsADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loud = scn.GetFreepsCard("loud");
		var thrarin = scn.GetFreepsCard("thrarin");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(loud);
		scn.MoveCardsToHand(dwarvenaxe);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(dwarvenaxe);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him skirmish away from his home site.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, troop);
		scn.FreepsResolveSkirmish(thrarin);

		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(loud));
		scn.FreepsUseCardAction(loud);

		// The one [dwarven] card in hand and the one minion are taken unasked.
		assertInZone(Zone.DISCARD, dwarvenaxe);
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionAlreadyFierceMayBeChosenAndTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loud = scn.GetFreepsCard("loud");
		var guard = scn.GetFreepsCard("guard");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(loud);
		scn.MoveCardsToHand(dwarvenaxe);

		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		assertInHand(dwarvenaxe);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, rider);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		// A keyword offered to a card that has it already is still an offer (Q-0245).
		assertTrue(scn.FreepsActionAvailable(loud));
		scn.FreepsUseCardAction(loud);

		assertInZone(Zone.DISCARD, dwarvenaxe);
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheSkirmishOfACompanionWhoIsNoDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loud = scn.GetFreepsCard("loud");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToSupportArea(loud);
		scn.MoveCardsToHand(dwarvenaxe);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertInHand(dwarvenaxe);
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{guard, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish: the Guard's minion is assigned but not yet skirmishing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(loud));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(loud));
	}

	@Test
	public void IsNotOfferedWithNoDwarvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loud = scn.GetFreepsCard("loud");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(loud);
		scn.MoveCardsToHand(sword);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(sword);
		assertEquals(1, scn.GetFreepsHandCount());
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(loud));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
	}
}
