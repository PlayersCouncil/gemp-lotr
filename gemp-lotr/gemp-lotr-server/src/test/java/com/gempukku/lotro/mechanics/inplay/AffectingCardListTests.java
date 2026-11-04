package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Which card the client's card info names for each modifier in its "Affecting card" list (Ketura's item, engine pass
 * 59).  The numbers printed on a borne card (CR 4.2 "attribute bonus": "a positive modifier for a character's
 * strength, vitality, and/or resistance, written with a plus sign", and a minion's site number) are added to its
 * bearer's by a rule of the engine, StatModifiersRule, whose modifiers have no source card.  The list showed each of
 * them as "System", for every bearer that bore anything, with a sum and with a 0 where nothing added to it: Gandalf
 * bearing Glamdring and Narya read "System: Strength +2", "System: Vitality modifier" and "System: Resistance +0",
 * beside "Glamdring: Damage +1", which is Glamdring's own text.  Now each borne card that adds to a number has a line
 * of its own, named for it, and a borne card that adds nothing has none.  The numbers themselves are unchanged
 * (BorneItemsTests).
 *
 * The lines are read with the rig's GetAffectingCard, which reads what the card info reads.  The table is the
 * Fellowship block's, with Frodo, Son of Drogo as the Ring-bearer.
 */
public class AffectingCardListTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gandalf, Friend of the Shirefolk: strength +1 for each of Hobbit, Dwarf, Elf and Man spotted in
					// the fellowship.  Frodo is the one Hobbit here, so his own line is "Strength +1".
					put("gandalf", "1_72");
					// Glamdring: possession, hand weapon, strength +2.  "Bearer must be Gandalf.  He is damage +1."
					// Its fellowship and regroup ability is never used.
					put("glamdring", "1_75");
					// Narya: artifact, ring, vitality +1.  "Bearer must be Gandalf."  Its start-of-turn trigger is
					// past by the time it is laid.
					put("narya", "3_34");
					// Shadowfax, Unequaled Steed: possession, mount, resistance +2.  "Bearer must be Gandalf."  Its
					// response to a wound is never met.
					put("shadowfax", "11_40");

					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man; "Knight." is his whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, site 4, no text at all
					// Goblin Spear: hand weapon, strength +2.  "Bearer must be a [moria] minion."  Its other text
					// answers a kill.
					put("spear", "1_182");
					// Second Level: a [gondor] Fortification condition for the support area, printed site number +2.
					// "Maneuver: Exert a [gondor] Man or spot a ranger to transfer this condition from your support
					// area to a minion."
					put("level", "7_118");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EachBorneCardsPrintedBonusIsNamedForThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var narya = scn.GetFreepsCard("narya");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, glamdring, narya, shadowfax);

		// 6 + 1 (Frodo, a Hobbit) + 2; 4 + 1; resistance 6 + 2.
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetVitality(gandalf));
		assertEquals(8, scn.GetResistance(gandalf));

		var lines = scn.GetAffectingCard(gandalf);
		assertTrue(lines.toString(), lines.contains("Glamdring: Strength +2"));
		assertTrue(lines.toString(), lines.contains("Narya: Vitality +1"));
		assertTrue(lines.toString(), lines.contains("Shadowfax: Resistance +2"));
		// Glamdring's own text, and Gandalf's, were named for their cards before.
		assertTrue(lines.toString(), lines.contains("Glamdring: Damage +1"));
		assertTrue(lines.toString(), lines.contains("Gandalf: Strength +1"));
		// No borne card's number is left to the rules' name.
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.startsWith("System: Strength")));
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.startsWith("System: Vitality")));
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.startsWith("System: Resistance")));
		assertEquals(lines.toString(), 5, lines.size());
	}

	@Test
	public void ABorneCardThatAddsNothingToANumberShowsNoLineForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, glamdring);

		// Glamdring prints no vitality and no resistance: no "Vitality +0", no "Resistance +0".
		var lines = scn.GetAffectingCard(gandalf);
		assertTrue(lines.toString(), lines.contains("Glamdring: Strength +2"));
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.contains("Vitality")));
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.contains("Resistance")));
		assertEquals(lines.toString(), 3, lines.size());
	}

	@Test
	public void ASiteNumberPrintedOnABorneCardIsNamedForThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(level);

		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);

		// The Spear prints a strength and no site number: the Troop shows no "Site number +0".
		var before = scn.GetAffectingCard(troop);
		assertTrue(before.toString(), before.contains("Goblin Spear: Strength +2"));
		assertTrue(before.toString(), before.stream().noneMatch(line -> line.contains("Site number")));

		scn.SkipToPhase(Phase.MANEUVER);
		// Second Level's own ability: the Knight is the one [gondor] Man to exert, and the Troop the one minion.
		scn.FreepsUseCardAction(level);
		assertAttachedTo(level, troop);
		assertEquals(6, scn.GetMinionSiteNumber(troop));

		var after = scn.GetAffectingCard(troop);
		assertTrue(after.toString(), after.contains("Second Level: Site number +2"));
		assertTrue(after.toString(), after.contains("Goblin Spear: Strength +2"));
		assertTrue(after.toString(), after.stream().noneMatch(line -> line.startsWith("System: Site number")));
		assertTrue(after.toString(), after.stream().noneMatch(line -> line.startsWith("System: Strength")));
	}

	@Test
	public void AHinderedBorneCardShowsNoLine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var narya = scn.GetFreepsCard("narya");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, glamdring, narya);
		scn.HinderCard(narya);

		// A hindered card gives no bonus (HinderedBorneCardTests), and is named for none.
		assertEquals(4, scn.GetVitality(gandalf));
		var lines = scn.GetAffectingCard(gandalf);
		assertTrue(lines.toString(), lines.contains("Glamdring: Strength +2"));
		assertTrue(lines.toString(), lines.stream().noneMatch(line -> line.contains("Vitality")));
	}
}
