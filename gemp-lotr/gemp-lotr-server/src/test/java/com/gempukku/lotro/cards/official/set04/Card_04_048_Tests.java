package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_048_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "4_48");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, unbound; +2 only while bearing a weapon
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, unbound; enduring, with no wound here
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound by rule, not the Ring-bearer
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally (only companions are unbound); a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, unbound, strength 8; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gimli, Lockbearer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Damage +1</b>.
		 * 		Gimli is strength +1 for each unbound Hobbit companion you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Lockbearer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void GimliIsStrongerForEachUnboundHobbitCompanionAndForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		// Merry ([shire]) and Pippin ([gondor]): 6 + 2. Frodo (the Ring-bearer) and Sam are Ring-bound, Bounder is a
		// Hobbit ally and Aragorn an unbound companion who is no Hobbit: none of them counts.
		assertEquals(8, scn.GetStrength(gimli));
		// No other companion is made stronger.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnUnboundHobbitCompanionPlayedMakesGimliStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(merry);

		scn.StartGame();
		assertInHand(merry);

		// Frodo alone, the Ring-bearer: no unbound Hobbit.
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
