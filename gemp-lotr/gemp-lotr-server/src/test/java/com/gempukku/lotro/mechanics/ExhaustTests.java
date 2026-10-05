package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Exhausting a character.
 *
 * Comprehensive Rules 4.2, "exhausted": a character with only 1 vitality remaining is exhausted; no player may exert
 * an exhausted character; "to exhaust a character means to exert that character as many times as you can".
 *
 * Player's Council ruling (Ketura, 2026-10-04): an exhausted character may not be exhausted.  Decipher's cards
 * never ask for exhausting as a cost, so the ruling shows on PC cards that do, and in which characters may be
 * chosen for an exhausting effect.
 */
public class ExhaustTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "7_116");         // Ranger of Osgiliath. Regroup: Discard 2 cards from hand to exhaust a minion.
					put("fodder1", "1_3");
					put("fodder2", "1_3");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: vitality 4
					put("words", "103_8");          // Words of Power: Exhaust your Wizard to reconcile your hand.

					put("taskmaster", "2_90");      // Orc Taskmaster: vitality 3
					put("soldier", "1_271");        // Orc Soldier: vitality 2
					put("troll", "1_165");          // Cave Troll of Moria: vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExhaustingExertsACharacterUntilOneVitalityIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(scn.GetFreepsCard("fodder1"), scn.GetFreepsCard("fodder2"));
		scn.MoveMinionsToTable(troll);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetVitality(troll));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.FreepsActionAvailable(ranger));

		scn.FreepsUseCardAction(ranger);

		// Vitality 4: three exertions, and the fourth cannot be made.
		assertEquals(3, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetVitality(troll));
		assertTrue(scn.IsExhausted(troll));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(0, scn.GetFreepsHandCount());
	}

	@Test
	public void ExhaustingAWoundedCharacterExertsItOnlyAsOftenAsItStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(scn.GetFreepsCard("fodder1"), scn.GetFreepsCard("fodder2"));
		scn.MoveMinionsToTable(troll);
		scn.AddWoundsToChar(troll, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetWoundsOn(troll));

		scn.FreepsUseCardAction(ranger);

		// One exertion was left to make.
		assertEquals(3, scn.GetWoundsOn(troll));
		assertTrue(scn.IsExhausted(troll));
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
	}

	@Test
	public void AnExhaustedCharacterCannotBeChosenToBeExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var troll = scn.GetShadowCard("troll");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(scn.GetFreepsCard("fodder1"), scn.GetFreepsCard("fodder2"));
		scn.MoveMinionsToTable(troll, taskmaster, soldier);
		// Vitality 3 with 2 wounds: already exhausted.
		scn.AddWoundsToChar(taskmaster, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.IsExhausted(taskmaster));
		assertFalse(scn.IsExhausted(troll));
		assertFalse(scn.IsExhausted(soldier));

		scn.FreepsUseCardAction(ranger);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troll, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(taskmaster));

		scn.FreepsChooseCard(soldier);
		// Vitality 2: one exertion.
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.IsExhausted(soldier));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(2, scn.GetWoundsOn(taskmaster));
	}

	@Test
	public void ExhaustingAsACostCanBePaidByACharacterWhoCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words);
		scn.MoveMinionsToTable(troll);
		// Vitality 4 with 2 wounds: one exertion short of exhausted.
		scn.AddWoundsToChar(gandalf, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsExhausted(gandalf));
		assertTrue(scn.FreepsPlayAvailable(words));

		scn.FreepsPlayCard(words);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsExhausted(gandalf));
	}

	@Test
	public void ExhaustingAsACostCannotBePaidByAnExhaustedCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var words = scn.GetFreepsCard("words");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(words);
		scn.MoveMinionsToTable(troll);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(words));
	}
}
