package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_061_Tests
{

// ----------------------------------------
// ULAIRE ATTEA, KHAMUL OF THE EAST TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "103_61");       // Úlairë Attëa, Khamul of the East
					put("captain", "4_225");      // Easterling Captain - str 11
					put("southron", "4_222");     // Desert Warrior - Southron, NOT Easterling

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Attea, Khamul of the East
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Easterling.</b>  Fierce.
		* 	While you can spot 3 burdens, each Easterling is strength +2.
		* 	While you can spot 5 burdens, each Easterling is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Khamul of the East", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void AtteaGrantsStrengthToEasterlingsAtThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var captain = scn.GetShadowCard("captain");
		var southron = scn.GetShadowCard("southron");
		scn.MoveMinionsToTable(attea, captain, southron);

		scn.StartGame();

		// Base strengths, no burdens
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(11, scn.GetStrength(captain));
		assertEquals(6, scn.GetStrength(southron));

		scn.AddBurdens(3);

		// With 3 burdens, Easterlings get +2
		assertEquals(14, scn.GetStrength(attea));
		assertEquals(13, scn.GetStrength(captain));
		// Southron is not Easterling - no bonus
		assertEquals(6, scn.GetStrength(southron));
	}

	@Test
	public void AtteaGrantsDamageToEasterlingsAtFiveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var captain = scn.GetShadowCard("captain");
		var southron = scn.GetShadowCard("southron");
		scn.MoveMinionsToTable(attea, captain, southron);

		scn.StartGame();

		// No damage keyword at 0 burdens
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(captain, Keyword.DAMAGE));

		scn.AddBurdens(4);

		// 4 burdens - still no damage (need 5)
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(captain, Keyword.DAMAGE));

		scn.AddBurdens(1); // Now at 5

		// With 5 burdens, Easterlings get damage +1
		assertTrue(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(captain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		// Southron still not Easterling
		assertFalse(scn.HasKeyword(southron, Keyword.DAMAGE));
	}

	@Test
	public void AtteaBothBonusesActiveAtFiveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var captain = scn.GetShadowCard("captain");
		scn.MoveMinionsToTable(attea, captain);

		scn.StartGame();
		scn.AddBurdens(5);

		// Both bonuses active
		assertEquals(14, scn.GetStrength(attea)); // 12 + 2
		assertEquals(13, scn.GetStrength(captain)); // 11 + 2
		assertEquals(1, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
	}
}
