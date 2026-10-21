package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("endurance", "2_4");
					put("endurance2", "2_4");

					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2; "to play, spot a Dwarf" (cheated in)
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; a skirmish ability never used
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability never used

					// Goblin Scrabbler: [moria] Orc, strength 4, vitality 1; its play trigger is not set off by a cheat
					put("scrabbler", "2_64");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not set off by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnduranceofDwarvesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Endurance of Dwarves
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Vitality: +1
		 * Game Text: Bearer must be a Dwarf.
		 * Skirmish: Discard this condition to make bearer strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("endurance");

		assertEquals("Endurance of Dwarves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnADwarfCompanionOrADwarfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var endurance = scn.GetFreepsCard("endurance");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(endurance);

		scn.StartGame();

		assertInHand(endurance);
		assertTrue(scn.FreepsPlayAvailable(endurance));
		scn.FreepsPlayCard(endurance);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(grimir);
		assertAttachedTo(endurance, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeItsBearerStrengthPlus1ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var endurance = scn.GetFreepsCard("endurance");
		var endurance2 = scn.GetFreepsCard("endurance2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, endurance);
		// The second copy, on another Dwarf who waits for his own skirmish.
		scn.AttachCardsTo(gimli, endurance2);

		scn.StartGame();
		assertAttachedTo(endurance, guard);
		assertAttachedTo(endurance2, gimli);

		var scrabbler = scn.GetShadowCard("scrabbler");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(scrabbler, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not a skirmish phase: neither copy is offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(endurance));
		assertFalse(scn.FreepsActionAvailable(endurance2));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, scrabbler},
				new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(4, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertAttachedTo(endurance, guard);
		assertAttachedTo(endurance2, gimli);
		assertTrue(scn.FreepsActionAvailable(endurance));

		scn.FreepsUseCardAction(endurance);
		assertInZone(Zone.DISCARD, endurance);
		assertEquals(5, scn.GetStrength(guard));
		// The other copy stays where it is, and its bearer is not made stronger.
		assertAttachedTo(endurance2, gimli);
		assertEquals(6, scn.GetStrength(gimli));

		// Still in force as the skirmish is resolved: 5 beats the Scrabbler's 4, where 4 would tie and lose.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.DISCARD, scrabbler);

		// Gone in the next skirmish, a phase of its own.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesTheBearerStrongerAndNotTheDwarfWhoIsTheOneSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var endurance = scn.GetFreepsCard("endurance");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(gimli, endurance);

		scn.StartGame();
		assertAttachedTo(endurance, gimli);

		var scrabbler = scn.GetShadowCard("scrabbler");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(scrabbler, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, scrabbler},
				new PhysicalCardImpl[]{gimli, runner});

		// The Guard's skirmish comes first: the bearer waits for his own, and another Dwarf is the one in this one.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertAttachedTo(endurance, gimli);
		assertTrue(scn.FreepsActionAvailable(endurance));

		scn.FreepsUseCardAction(endurance);
		assertInZone(Zone.DISCARD, endurance);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
