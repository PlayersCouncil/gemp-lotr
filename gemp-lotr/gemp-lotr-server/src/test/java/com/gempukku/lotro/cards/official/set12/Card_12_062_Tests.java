package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("zealot", "12_62");
					put("zealot2", "12_62");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, resistance 6; his maneuver ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf, resistance 6; his skirmish ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, resistance 5; his abilities are used only on purpose
					put("runner", "1_178");     // Goblin Runner: vitality 1, so always exhausted; its text answers its own play, and it is cheated in
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Uruk Camp: Battleground and Plains; its text answers an Uruk-hai played, and none is.
					// Site 1, Eastemnet Gullies 4_325, and site 3, Barrows of Edoras 4_337 (a sanctuary), are
					// no battlegrounds; site 4, White Rocks 4_346, is one and nothing else.
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingZealotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dunlending Zealot
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While this minion is exhausted, each unbound companion is resistance -1 (or -2 if the fellowship is at a battleground site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("zealot");

		assertEquals("Dunlending Zealot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileExhaustedEachUnboundCompanionIsResistanceMinus1OrMinus2AtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var zealot = scn.GetShadowCard("zealot");
		var zealot2 = scn.GetShadowCard("zealot2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(zealot, zealot2, runner);

		// The Zealots unwounded have 2 vitality left and are not exhausted; the Goblin Runner, beside them, is.
		assertEquals(0, scn.GetWoundsOn(zealot));
		assertEquals(0, scn.GetWoundsOn(zealot2));
		assertEquals(1, scn.GetVitality(runner));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// One wound exhausts the first. Site 1 is no battleground: -1 to each unbound companion only, and
		// nothing from the second Zealot, who is not exhausted.
		scn.AddWoundsToChar(zealot, 1);
		assertEquals(1, scn.GetVitality(zealot));
		assertEquals(2, scn.GetVitality(zealot2));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// The fellowship moves to Uruk Camp, a battleground: -2.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
	}

	@Test
	public void MinusTwoAtABattlegroundThatIsNothingElseAndMinusOneAtASanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var zealot = scn.GetShadowCard("zealot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(zealot);
		scn.AddWoundsToChar(zealot, 1);
		assertEquals(1, scn.GetWoundsOn(zealot));

		// At Barrows of Edoras, a sanctuary and no battleground: -1.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));

		// White Rocks, a battleground: -2.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(gimli));
	}
}
