package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cutthroat", "11_113");
					put("cutthroat2", "11_113");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text

					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] Dwarf, resistance 7; his draw only on a skirmish win
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] Dwarf, resistance 6; muster only at a battleground or underground site
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, resistance 5, cost 2; abilities used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] ally, resistance 0 as every ally; ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo 1_290, resistance 10
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CutthroatOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Cutthroat Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each companion who has resistance 5 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cutthroat");

		assertEquals("Cutthroat Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ACompanionOfResistance5PlayedMakesEachCutthroatOrcStrengthPlus1AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var cutthroat = scn.GetShadowCard("cutthroat");
		var cutthroat2 = scn.GetShadowCard("cutthroat2");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(sam);

		scn.StartGame();
		// Minions laid early, to be read before and after the play (Q-0228).
		scn.MoveMinionsToTable(cutthroat, cutthroat2, troop);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(7, scn.GetResistance(gimli));
		// Resistance 6, one more than the text allows.
		assertEquals(6, scn.GetResistance(farin));
		// An ally: resistance 0, but no companion.
		assertEquals(0, scn.GetResistance(bilbo));
		assertEquals(9, scn.GetStrength(cutthroat));
		assertEquals(9, scn.GetStrength(cutthroat2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(sam);
		assertEquals(5, scn.GetResistance(sam));
		// Sam is the one companion with resistance 5 or less: each copy +1 for itself, and no more.
		assertEquals(10, scn.GetStrength(cutthroat));
		assertEquals(10, scn.GetStrength(cutthroat2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsEachCompanionBroughtToResistance5OrLessTheRingBearerIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var cutthroat = scn.GetShadowCard("cutthroat");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli, farin, sam);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(cutthroat, troop);

		assertEquals(0, scn.GetBurdens());
		// Sam alone: 9 + 1.
		assertEquals(10, scn.GetStrength(cutthroat));
		assertEquals(13, scn.GetStrength(troop));

		// Each burden takes 1 from every companion's resistance.
		scn.AddBurdens(2);
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(bilbo));
		// Gimli, Farin and Sam: 9 + 3.
		assertEquals(12, scn.GetStrength(cutthroat));
		assertEquals(13, scn.GetStrength(troop));

		scn.AddBurdens(3);
		assertEquals(5, scn.GetResistance(frodo));
		// The Ring-bearer as well, and still not the ally: 9 + 4.
		assertEquals(13, scn.GetStrength(cutthroat));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
