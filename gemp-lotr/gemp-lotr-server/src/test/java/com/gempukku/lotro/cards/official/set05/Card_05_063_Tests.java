package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "5_63");
					put("berserker2", "5_63");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2."
					// Thrarin, Dwarven Smith: Dwarf ally, vitality 2.  "Maneuver: Exert Thrarin to allow him to participate
					// in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Uruk-hai Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>This minion is strength +1 for each wound on a character in its skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("berserker");

		assertEquals("Uruk-hai Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void InItsSkirmishItIsStrengthPlus1ForEachWoundOnACharacterThereAndNoneElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(berserker, berserker2, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(berserker, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(berserker2, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// In no skirmish, with wounded characters all round: its printed 7.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(berserker2));
		assertEquals(7, scn.GetStrength(berserker));
		assertEquals(7, scn.GetStrength(berserker2));

		// The Savage, left over, goes to Aragorn beside the first Berserker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, berserker }, new PhysicalCardImpl[] { gimli, berserker2 });
		scn.ShadowAssignToMinions(aragorn, savage);

		// Assigned, with no skirmish begun: still 7.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(berserker));
		assertEquals(7, scn.GetStrength(berserker2));

		scn.FreepsResolveSkirmish(aragorn);

		// 7 + 2 on Aragorn + 1 on the Savage + 1 on itself.  The second Berserker, whose skirmish waits, is 7: the
		// wounds on Gimli and on itself are not in a skirmish of its yet, and those of this skirmish are not its own.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(berserker));
		assertEquals(7, scn.GetStrength(berserker2));
	}

	@Test
	public void AWoundPlacedDuringItsSkirmishRaisesItsStrengthBy1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(berserker);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.FreepsResolveSkirmish(gimli);

		// 7 + 1 on Gimli.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(berserker));

		// Gimli exerts in the skirmish: a second wound on him, and 7 + 2.
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(berserker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnAnAllyAndAMinionInItsSkirmishCountToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var berserker = scn.GetShadowCard("berserker");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(berserker, savage);
		scn.AddWoundsToChar(savage, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin exerts to take part in skirmishes: the one wound on him.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, berserker);
		scn.ShadowAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		// 7 + 1 on the ally + 2 on the other minion.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(berserker));
	}
}
