package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8; a maneuver ability used only on purpose

					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul, strength 9, vitality 2; reaches only bearers of [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; his texts need 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc who is no Nazgul, strength 5; its text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Arwen, Daughter of Elrond
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ranger</b>.<br>While skirmishing a Nazgûl, Arwen is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Daughter of Elrond", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void SheIsStrengthPlus3OnlyWhileSkirmishingANazgulAndNotWhileWaitingForHerSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);

		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, lemenya},
				new PhysicalCardImpl[]{aragorn, khamul});
		assertEquals(6, scn.GetStrength(arwen));

		// Aragorn's skirmish first: Arwen is assigned to a Nazgul but not skirmishing, and Aragorn, skirmishing one,
		// is not made stronger by her text.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, khamul));
		assertTrue(scn.IsCharAssignedAgainst(arwen, lemenya));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		// 8 against 12: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, lemenya));
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against 9: she loses and takes a wound; once her skirmish is over she is 6 again (Khamul is fierce, so a
		// fierce assignment phase follows).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.IsCharSkirmishing(arwen));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SkirmishingANazgulWhoIsNotARingwraithSheIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, khamul);
		assertEquals(6, scn.GetStrength(arwen));

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, khamul));
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingARingwraithMinionWhoIsNoNazgulSheIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, hound);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, hound));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
