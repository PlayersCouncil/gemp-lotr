package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_51");
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
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Legolas, Prince of Mirkwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Archer</b>.<br>While skirmishing a Nazgûl, Legolas is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Prince of Mirkwood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void IsStrengthPlus3OnlyWhileSkirmishingANazgulAndNotWhileWaitingForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);

		// Legolas is an archer: the Shadow player places the fellowship's one archery wound on Khamul.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, lemenya},
				new PhysicalCardImpl[]{aragorn, khamul});
		assertEquals(6, scn.GetStrength(legolas));

		// Aragorn's skirmish first: Legolas is assigned to a Nazgul but not skirmishing, and Aragorn, skirmishing one,
		// is not made stronger by his text.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, khamul));
		assertTrue(scn.IsCharAssignedAgainst(legolas, lemenya));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		// 8 against 12: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, lemenya));
		assertEquals(9, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against 9: he loses and takes a wound; once his skirmish is over he is 6 again (Khamul is fierce, so a
		// fierce assignment phase follows).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.IsCharSkirmishing(legolas));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SkirmishingANazgulWhoIsNotARingwraithHeIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);

		// Legolas's archery wound goes on the one minion unasked.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(khamul));
		scn.FreepsAssignToMinions(legolas, khamul);
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, khamul));
		assertEquals(9, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingARingwraithMinionWhoIsNoNazgulHeIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);

		// Legolas's archery wound goes on the one minion unasked; the Hound, vitality 2, lives through it.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(hound));
		scn.FreepsAssignToMinions(legolas, hound);

		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, hound));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
