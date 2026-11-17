package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grand", "11_32");
					put("gandalf", "8_15");     // Gandalf, Leader of Men: [gandalf] Wizard companion, strength 7, resistance 6; text only for a starting fellowship
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ALLY (Hobbit set, Q-0312); a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, strength 6, resistance 6; +2 only beside 3 Ents
					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] Dwarf, strength 6, resistance 7; a win trigger never reached
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] Dwarf, strength 5, resistance 6; muster at a battleground or underground site only
					put("saruman", "4_173");    // Saruman, Black Traitor: an [isengard] Wizard minion, strength 8; his play trigger is never reached
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GforGrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: G for Grand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot a [gandalf] Wizard, each companion who has resistance 6 or more is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grand");

		assertEquals("G for Grand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AGandalfWizardPlayedMakesEachCompanionOfResistance6OrMoreStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grand = scn.GetFreepsCard("grand");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToSupportArea(grand);
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();
		// One burden takes 1 off every companion's resistance.
		scn.AddBurdens(1);

		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(farin));
		// Frodo 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(farin));

		scn.FreepsPlayCard(gandalf);

		// Gandalf is spotted. Frodo (9) and Gimli (6, the threshold exactly) are +1; Farin (5) is not, and nor is
		// Gandalf himself, whose resistance of 6 is 5 under the burden.
		assertEquals(5, scn.GetResistance(gandalf));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfWizardInPlayNoCompanionIsStrongerUntilAGandalfWizardAllyIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grand = scn.GetFreepsCard("grand");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		var radagast = scn.GetFreepsCard("radagast");
		var frodo = scn.GetRingBearer();
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCardsToSupportArea(grand);
		scn.MoveCompanionsToTable(avenger, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman);

		// A [gandalf] companion who is no Wizard, and a Wizard of another culture: neither is spotted.
		assertEquals(6, scn.GetResistance(avenger));
		assertEquals(7, scn.GetResistance(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(saruman));

		scn.MoveAlliesToTable(radagast);

		// A [gandalf] Wizard who is an ally switches it on; he, an ally of resistance 0, is not stronger.
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(radagast));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
