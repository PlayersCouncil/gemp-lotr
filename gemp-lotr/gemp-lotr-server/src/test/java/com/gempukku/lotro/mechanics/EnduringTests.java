package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The keyword enduring.
 *
 * Comprehensive Rules 4.2, "enduring": "For each wound on a character with the enduring keyword, that character
 * is strength +2."
 *
 * A wound is a wound however it came there.  "Exert": "Exerting a character is different from wounding a
 * character, even though both require placement of a wound token", so the tokens of an exertion count as those
 * of an attack do.  And the bonus is read from the wounds that are on the character now: a wound healed takes its
 * +2 with it.  Enduring characters are on both sides.
 */
public class EnduringTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dead Man of Dunharrow: enduring; strength 6, vitality 3.  "To play, add a threat.  When
					// you play this companion, exert him twice."
					put("deadman", "10_27");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3; Knight is his whole text, and he is not enduring
					put("legolas", "1_50");     // Legolas, Greenleaf: archer; his archery ability is not used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here

					// Ulaire Toldea, Thrall of The One: enduring; strength 12, vitality 3.  "Shadow cards
					// cannot exert [him] during a skirmish phase.  Skirmish: Heal Ulaire Toldea to make him
					// damage +1."
					put("toldea", "10_71");
					put("marksman1", "1_176");  // Goblin Marksman: archer; strength 7, vitality 1; no other text
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnEnduringCompanionIsStrengthPlus2ForAWoundAndACompanionWhoIsNotEnduringIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(deadman, knight);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);

		assertTrue(scn.HasKeyword(deadman, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(knight, Keyword.ENDURING));

		// Two archers: the Free Peoples player has two archery wounds to place.
		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(6, scn.GetStrength(deadman));
		assertEquals(6, scn.GetStrength(knight));

		// One on the Knight, who has no keyword to gain by.
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(deadman));

		// One on the Dead Man: 6 + 2.
		scn.FreepsChooseCard(deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(8, scn.GetStrength(deadman));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheWoundsOfAnExertionCountAndEachOneIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCardsToHand(deadman);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(deadman));

		// "To play, add a threat.  When you play this companion, exert him twice."
		scn.FreepsPlayCard(deadman);
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());

		// Two wounds, neither from an attack: 6 + 2 + 2.
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(10, scn.GetStrength(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEnduringMinionGainsStrengthWithAWoundAndLosesItWhenTheWoundIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);

		assertTrue(scn.HasKeyword(toldea, Keyword.ENDURING));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(12, scn.GetStrength(toldea));

		// Legolas is the fellowship's one archer, and Toldea the only minion to take his wound: 12 + 2.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertEquals(14, scn.GetStrength(toldea));

		scn.FreepsAssignAndResolve(aragorn, toldea);
		assertEquals(14, scn.GetStrength(toldea));
		assertFalse(scn.HasKeyword(toldea, Keyword.DAMAGE));

		// "Skirmish: Heal Ulaire Toldea to make him damage +1."  The wound goes, and its +2 with it.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(toldea));
		scn.ShadowUseCardAction(toldea);
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(12, scn.GetStrength(toldea));
		assertTrue(scn.HasKeyword(toldea, Keyword.DAMAGE));

		// Aragorn, 8, loses to 12 and takes a wound, and one more for damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
