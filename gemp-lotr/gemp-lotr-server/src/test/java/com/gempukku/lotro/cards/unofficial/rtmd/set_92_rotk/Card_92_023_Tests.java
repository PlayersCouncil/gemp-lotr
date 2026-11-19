package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_023_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion, strength 6, vitality 3
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion, strength 4, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3, vitality 4
					put("rider", "5_83");       // Household Guard: a [rohan] Man, strength 6, vitality 3; Valiant is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man who is an ally, strength 6
					put("bounder", "1_286");    // Bounder: a [shire] ally with 2 vitality, who could exert
					put("runner", "1_178");     // Goblin Runner: a minion, strength 5, vitality 1
					put("savage", "1_151");     // Uruk Savage: a minion, strength 5, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_23", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion, strength 6, vitality 3
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion, strength 4, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3, vitality 4
					put("rider", "5_83");       // Household Guard: a [rohan] Man, strength 6, vitality 3; Valiant is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man who is an ally, strength 6
					put("bounder", "1_286");    // Bounder: a [shire] ally with 2 vitality, who could exert
					put("runner", "1_178");     // Goblin Runner: a minion, strength 5, vitality 1
					put("savage", "1_151");     // Uruk Savage: a minion, strength 5, vitality 3; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_23"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_23
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Skirmish: Exert your companion to make another companion of the same culture strength +1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_23", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void ExertDwarfPumpsAnotherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		int guardStrength = scn.GetStrength(guard);
		assertEquals(4, guardStrength);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Exert Gimli (Dwarven)
		scn.FreepsChooseCard(gimli);

		// Guard is auto-selected as the only other Dwarven companion
		assertEquals(guardStrength + 1, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Gimli himself and Aragorn, who is [gondor], are as they were; nobody else paid
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertNonMatchingCultureDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		// Aragorn is Gondor; no other Gondor companions, so the effect should fizzle
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(aragorn);

		// Exert Aragorn (Gondor) — no other Gondor companion, pump fizzles
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OwnerGated() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier; Freeps should not be able to use it
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		var mod = scn.GetShadowCard("mod");
		// Gimli and the Dwarf Guard could both exert
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnyCompanionMayExertAndAnotherCompanionOfHisCultureIsStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var merchant = scn.GetFreepsCard("merchant");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(aragorn, knight, pippin, rider);
		scn.MoveAlliesToTable(merchant, bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Any companion may exert, whatever his culture, in the skirmish or out of it, and the Ring-bearer, who
		// has no other [shire] companion beside him; not an ally who could exert, and not a minion
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, pippin, rider, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, merchant, savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));

		// Aragorn is [gondor]: the other [gondor] companions, the Knight in the skirmish and Pippin (a Hobbit)
		// out of it. Not Aragorn himself, not the [rohan] Man, not the [gondor] ally, nobody of another culture
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, rider, merchant, frodo, bounder, savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Knight (6) beats the Uruk Savage (5); in the next skirmish Pippin is 3 again
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedOnceEveryCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Frodo (vitality 4) and the Dwarf Guard (vitality 2) are exhausted; Gimli (vitality 3) can exert once more
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, troop);

		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		// Gimli is the only companion who can pay and the Dwarf Guard the only other [dwarven] companion:
		// neither is asked for
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(5, scn.GetStrength(guard));

		// Now no companion can exert, though the Bounder (an ally) and the Goblin Patrol Troop could
		scn.ShadowPass();
		assertEquals(1, scn.GetVitality(gimli));
		assertEquals(1, scn.GetVitality(guard));
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(2, scn.GetVitality(bounder));
		assertEquals(3, scn.GetVitality(troop));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
