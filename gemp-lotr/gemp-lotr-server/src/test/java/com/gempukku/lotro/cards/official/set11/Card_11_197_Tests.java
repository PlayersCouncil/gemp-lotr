package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_197_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "11_197");
					put("uruk2", "11_197");
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, damage +1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, resistance 6; a maneuver
					                            // ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, resistance 5; abilities used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0), strength 4, vitality 2;
					                            // "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OverpoweringUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Overpowering Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>While this minion is skirmishing a character who has resistance 3 or less, this minion is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk");

		assertEquals("Overpowering Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3OnlyWhileSkirmishingACharacterOfResistance3OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk, uruk2, cuguard);

		// Two burdens: Aragorn 6 - 2 = 4, Sam 5 - 2 = 3.
		assertEquals(2, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(3, scn.GetResistance(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{sam, uruk2});
		scn.ShadowAssignToMinions(sam, cuguard);
		assertEquals(7, scn.GetStrength(uruk));
		assertEquals(7, scn.GetStrength(uruk2));
		assertEquals(9, scn.GetStrength(cuguard));

		scn.FreepsResolveSkirmish(aragorn);
		// Against resistance 4: as printed.  The other copy is assigned to Sam (resistance 3) but is not yet
		// skirmishing him: as printed too.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetStrength(uruk));
		assertEquals(7, scn.GetStrength(uruk2));
		assertEquals(9, scn.GetStrength(cuguard));

		// Aragorn (8) beats the copy (7), which takes one wound of its two.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(uruk));
		scn.FreepsResolveSkirmish(sam);

		// Skirmishing Sam, resistance 3: 7 + 3.  The Guard in the same skirmish, and the copy now in no
		// skirmish, are as printed.
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(10, scn.GetStrength(uruk2));
		assertEquals(9, scn.GetStrength(cuguard));
		assertEquals(7, scn.GetStrength(uruk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlus3SkirmishingAnAllyWhoHasNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(7, scn.GetStrength(uruk));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, uruk);
		scn.FreepsResolveSkirmish(thrarin);

		// An ally's resistance is 0 (CR 4.2 "resistance"): 7 + 3.
		assertEquals(0, scn.GetResistance(thrarin));
		assertEquals(10, scn.GetStrength(uruk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
