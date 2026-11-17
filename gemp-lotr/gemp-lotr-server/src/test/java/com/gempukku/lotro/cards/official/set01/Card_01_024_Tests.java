package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stairs", "1_24");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; Maneuver: exert to skirmish until regroup
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, strength 15, damage +1, fierce
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StairsofKhazaddumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Stairs of Khazad-dûm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.<br>While a Dwarf skirmishes a [moria] minion, that Dwarf is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stairs");

		assertEquals("Stairs of Khazad-dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfCompanionIsStrengthPlus1OnlyWhileHeSkirmishesAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCompanionsToTable(gimli, aragorn);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));

		// Aragorn, no Dwarf, skirmishes a [moria] Orc: nothing.  Gimli is assigned to the other and still waiting.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(runner));

		// Gimli, 7, beats the Runner; his skirmish over, he is 6 again.
		scn.PassSkirmishActions();
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyIsStrengthPlus1AgainstAMoriaTrollAndADwarfIsNotAgainstAMinionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);

		var troll = scn.GetShadowCard("troll");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		// Thrarin's own ability lets him skirmish away from his home site.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{thrarin, troll});
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));

		// Gimli skirmishes an [isengard] minion: nothing.  Thrarin waits for the Troll.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(5, scn.GetStrength(thrarin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
