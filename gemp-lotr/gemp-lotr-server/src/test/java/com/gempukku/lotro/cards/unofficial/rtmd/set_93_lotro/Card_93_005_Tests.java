package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_5: Each companion with resistance 3 or less is strength -1.
 *
 * This is not owner-gated — it applies to all companions regardless of who owns the mod.
 *
 * Test cards:
 * - Gimli, Dwarf of Erebor (1_13): resistance 6, strength 6 — NOT affected
 * - Merry, Friend to Sam (1_302): resistance 3, strength 3 — affected (resistance <= 3)
 */
public class Card_93_005_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli (1_13): Dwarf companion, resistance 6, strength 6
		put("gimli", "1_13");
		// Merry : Hobbit companion, resistance 9, base strength 3
		put("merry", "11_168");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_5"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void LowResistanceCompanionLosesStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(gimli, merry);

		scn.StartGame();

		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		assertEquals(9, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(3);

		//Gimli is now strength -1
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		assertEquals(6, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));

		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(3);

		assertEquals(0, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		//Merry is now strength -1
		assertEquals(3, scn.GetResistance(merry));
		assertEquals(2, scn.GetStrength(merry));

		assertEquals(4, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(1);

		assertEquals(0, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		//Merry is now strength -1
		assertEquals(2, scn.GetResistance(merry));
		assertEquals(2, scn.GetStrength(merry));

		assertEquals(3, scn.GetResistance(frodo));
		assertEquals(3, scn.GetStrength(frodo));
	}

	@Test
	public void AffectsOpponentCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.AddBurdens(6);

		// Shadow owns the mod, but no owner-gating: merry should still be affected
		assertEquals(2, scn.GetStrength(merry));
	}
}
