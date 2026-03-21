package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_005_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli, Dwarf of Erebor (1_13): 6 str, 3 vit, twilight 2
		put("gimli", "1_13");
		// Aragorn, Ranger of the North (1_89): 8 str, 4 vit, twilight 4
		put("aragorn", "1_89");
		// Legolas, Greenleaf (1_50): 6 str, 3 vit, twilight 2
		put("legolas", "1_50");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_5"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_5
		 * Type: MetaSite
		 * Game Text: Fellowship: Exert X companions to remove (X).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ExertOneCompanionToRemoveOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: Exert 1 companion to remove (1).

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SetTwilight(5);

		int twilightBefore = scn.GetTwilight();

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));
		// Choose Gimli to exert
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(twilightBefore - 1, scn.GetTwilight());
	}

	@Test
	public void ExertTwoCompanionsToRemoveTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: Exert 2 companions to remove (2).

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.SetTwilight(5);

		int twilightBefore = scn.GetTwilight();

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));
		// Choose both to exert
		scn.FreepsChooseCards(gimli, aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(twilightBefore - 2, scn.GetTwilight());
	}

	@Test
	public void CannotUseWithNoCompanionsThatCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: If no companions can exert, ability should not be available.
		// Frodo alone with 3 wounds (exhausted) = can't exert.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		// Exhaust Frodo (vitality 4, so 3 wounds = exhausted)
		scn.AddWoundsToChar(scn.GetRingBearer(), 3);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void CanUseWithOpponentsMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: If no companions can exert, ability should not be available.
		// Frodo alone with 3 wounds (exhausted) = can't exert.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(shadowMod));
	}
}
