package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_199_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "7_199");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, strength 9; her text is about companions
					// bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, strength 8; its text needs 3 burdens
					put("creeper", "8_75");
					// Bill Ferny, Swarthy Sneering Fellow: a [wraith] Man, strength 4.  "Nazgûl are not roaming. ..."
					put("ferny", "2_75");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Nazgûl are strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Morgul Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachNazgulOfEitherCultureIsStrengthPlusOneAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var ferny = scn.GetShadowCard("ferny");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, creeper, ferny);

		// Without the Soldier: each at its printed strength.
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.MoveMinionsToTable(soldier);

		// Both Nazgûl, the [wraith] one and the [raider] one, are 1 stronger.  The [wraith] Orc, the [wraith] Man,
		// the Soldier itself and the companion are not.
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
