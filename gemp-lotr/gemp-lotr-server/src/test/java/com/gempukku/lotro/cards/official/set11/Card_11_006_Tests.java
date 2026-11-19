package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallen", "11_6");
					put("grimir", "11_10");     // Grimir, Dwarven Emissary: Dwarf companion, strength 7, resistance 7; a response to a wound, which nothing here deals
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, resistance 6; strength +2 while skirmishing an Orc
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, resistance 6; "To play, spot a Dwarf" is its whole text
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its play trigger is not reached by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 as the Ring-bearer: a Dwarf, resistance 4 and +1 for his damage +1.  His start-of-
	// skirmish burden or threat comes only in a skirmish involving him, and he is in none.
	protected VirtualTableScenario GetGimliRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallen", "11_6");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FallenLordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fallen Lord
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert the Ring-bearer and a Dwarf to make that Dwarf strength +X, where X equals that Dwarf's resistance.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fallen");

		assertEquals("Fallen Lord", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheRingBearerAndAChosenDwarfToMakeThatDwarfStrengthPlusHisResistanceUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fallen = scn.GetFreepsCard("fallen");
		var grimir = scn.GetFreepsCard("grimir");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(grimir, farin, aragorn, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(fallen);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(fallen);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, troop}, new PhysicalCardImpl[]{farin, runner});
		scn.FreepsResolveSkirmish(grimir);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(7, scn.GetStrength(grimir));
		assertEquals(7, scn.GetResistance(grimir));

		assertTrue(scn.FreepsPlayAvailable(fallen));
		scn.FreepsPlayCard(fallen);
		// Every Dwarf may be chosen: the one skirmishing, one assigned whose skirmish is still to come, and an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		// The Ring-bearer and Grimir exerted; Sam, Ring-bound but not the Ring-bearer, did not.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(sam));
		// 7 + 7 for his resistance.
		assertEquals(14, scn.GetStrength(grimir));
		assertEquals(5, scn.GetStrength(farin));
		assertInDiscard(fallen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Grimir's 14 beats the Troop's 13.  In the next skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.FreepsResolveSkirmish(farin);
		assertEquals(7, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsTheResistanceTheDwarfHasLeftAfterBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fallen = scn.GetFreepsCard("fallen");
		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(grimir, guard);
		scn.MoveCardsToHand(fallen);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		// Dwarf Guard's resistance 6, less 3 burdens; his strength 4 is not his resistance.
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(grimir));

		scn.FreepsPlayCard(fallen);
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir));
		scn.FreepsChooseCard(guard);

		// 4 + 3; Grimir, not chosen, is unchanged.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(grimir));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheRingBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fallen = scn.GetFreepsCard("fallen");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(grimir);
		scn.MoveCardsToHand(fallen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(fallen);
		// Frodo's vitality is 4: three wounds exhaust him.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimir, troop);
		scn.FreepsResolveSkirmish(grimir);

		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInHand(fallen);
		assertFalse(scn.FreepsPlayAvailable(fallen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fallen = scn.GetFreepsCard("fallen");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(grimir, aragorn);
		scn.MoveCardsToHand(fallen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(fallen);
		// Grimir's vitality is 3: two wounds exhaust him.  Aragorn, no Dwarf, could still exert.
		scn.AddWoundsToChar(grimir, 2);
		assertEquals(2, scn.GetWoundsOn(grimir));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(fallen);
		assertFalse(scn.FreepsPlayAvailable(fallen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsADwarfMayPayBothExertionsAndIsStrengthPlusHisResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var fallen = scn.GetFreepsCard("fallen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fallen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(5, scn.GetResistance(gimli));
		int strength = scn.GetStrength(gimli);

		// Gimli is the Ring-bearer and the only Dwarf: he is exerted once as each (vitality 3, so he can).
		assertTrue(scn.FreepsPlayAvailable(fallen));
		scn.FreepsPlayCard(fallen);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(strength + 5, scn.GetStrength(gimli));
		assertInDiscard(fallen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
