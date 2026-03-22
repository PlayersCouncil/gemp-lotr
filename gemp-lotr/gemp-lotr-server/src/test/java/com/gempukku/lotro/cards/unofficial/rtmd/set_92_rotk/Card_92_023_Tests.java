package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_023_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli, Dwarf of Erebor (1_13): Dwarven companion, twilight 2, strength 6, vitality 3
		put("gimli", "1_13");
		// Dwarf Guard (1_7): Dwarven companion, twilight 1, strength 3, vitality 1
		put("guard", "1_7");
		// Aragorn, Ranger of the North (1_89): Gondor companion, twilight 4, strength 8, vitality 4
		put("aragorn", "1_89");
		// Goblin Runner (1_178): Moria goblin minion
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_23", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_23"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_23", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
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

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Exert Gimli (Dwarven)
		scn.FreepsChooseCard(gimli);

		// Guard is auto-selected as the only other Dwarven companion
		assertEquals(guardStrength + 1, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void ExertNonMatchingCultureDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		// Aragorn is Gondor; no other Gondor companions, so the effect should fizzle
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
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
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(aragorn);

		// Exert Aragorn (Gondor) — no other Gondor companion, pump fizzles
		assertEquals(1, scn.GetWoundsOn(aragorn));
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
		assertFalse(scn.FreepsActionAvailable(mod));
	}
}
