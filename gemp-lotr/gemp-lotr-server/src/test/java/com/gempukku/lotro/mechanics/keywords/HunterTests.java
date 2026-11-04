package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The keyword hunter.
 *
 * Comprehensive Rules 4.2, "hunter": "This keyword has the form of 'Hunter X' where 'X' is the strength bonus the
 * character receives when skirmishing a character that does not have the hunter keyword."
 *
 * "Skirmish phase(s)": "A character is 'skirmishing' or in a skirmish 'involving' that character only while the
 * skirmish phase that character is assigned to is happening."  So the bonus is there for the length of the
 * hunter's own skirmish and at no other time, an assigned hunter whose skirmish has not begun included.
 *
 * Hunters are on both sides of the table, and the rule names no side: a hunter minion has the bonus against a
 * companion who is no hunter as a hunter companion has it against such a minion, and two hunters fighting each
 * other have none.
 */
public class HunterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Eager Hunter: strength 6, vitality 3; "Damage +1. Hunter 3." is his whole text.
					put("gimli", "15_5");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; no hunter; a maneuver ability, not used here

					// White Hand Butcher: strength 5, vitality 2; "Hunter 6." is its whole text.
					put("butcher", "17_122");
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHunterIsStrongerByItsNumberWhileSkirmishingACharacterWhoIsNoHunterAndOnlyThen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var butcher = scn.GetShadowCard("butcher");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, brigand);
		scn.SkipToAssignments();

		// Outside a skirmish a hunter has what is printed on it.
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(brigand, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));

		// Each hunter is assigned to a character who is no hunter.  Being assigned is not skirmishing.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brigand}, new PhysicalCardImpl[]{aragorn, butcher});
		assertTrue(scn.IsCharAssignedAgainst(gimli, brigand));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, butcher));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));

		// Gimli's skirmish: 6 + 3 for hunter 3.  The Brigand, no hunter, has nothing, and the Butcher, whose own
		// skirmish has not begun, has nothing yet.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, brigand));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(8, scn.GetStrength(aragorn));

		// 9 beats 8, which the printed 6 would not: the Brigand takes the wound (and one for damage +1) and
		// Gimli takes none.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(gimli));
		// His skirmish is over and so is the bonus.
		assertEquals(6, scn.GetStrength(gimli));

		// The Butcher's skirmish: 5 + 6 for hunter 6.  Aragorn, no hunter, has his 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, butcher));
		assertEquals(11, scn.GetStrength(butcher));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		// 11 beats 8, which the printed 5 would not.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(5, scn.GetStrength(butcher));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoHuntersSkirmishingEachOtherHaveNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher);
		scn.SkipToAssignments();

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));

		scn.FreepsAssignToMinions(gimli, butcher);
		scn.FreepsResolveSkirmish(gimli);

		// Hunter 3 against hunter 6: neither is skirmishing a character without the keyword.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, butcher));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));

		// 6 beats 5.  With the bonuses it would be 9 against 11 and Gimli would take the wound.  The Butcher,
		// vitality 2, takes two (damage +1) and is killed.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, butcher);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHunterSkirmishingAHunterAndACharacterWhoIsNoneHasItsBonusAndTheHunterFacingItHasNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var butcher = scn.GetShadowCard("butcher");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, runner);
		scn.SkipToAssignments();

		// The Free Peoples player takes the Runner and the Shadow player adds the Butcher, who was left over.
		scn.FreepsAssignToMinions(gimli, runner);
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(gimli, butcher);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, butcher));

		// Gimli is skirmishing a character who is no hunter, the Runner: 6 + 3.  The Butcher is skirmishing
		// Gimli alone, a hunter: its printed 5.  The Runner has no keyword to gain by.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(5, scn.GetStrength(runner));

		// 9 against 5 + 5: Gimli loses by one and takes one wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
