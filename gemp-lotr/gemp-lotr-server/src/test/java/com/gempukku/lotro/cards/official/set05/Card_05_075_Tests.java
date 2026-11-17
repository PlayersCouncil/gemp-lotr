package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "5_75");
					put("runner2", "5_75");
					// Southron Soldier: [raider] Southron Man, cost 2, "Southron. Ambush (2)" and nothing else
					put("soldier", "4_254");
					// Desert Nomad: [raider] Southron Man of cost 4, read beside the Runner in hand; damage +1 at 2 threats only
					put("nomad", "7_132");
					// Easterling Axeman: [raider] Man, no Southron; strength +3 and fierce at 2 burdens only
					put("axeman", "4_224");
					// Bladetusk Rearguard: [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce
					// mounted Southron minion until the end of the turn ..." (no Man)
					put("rearguard", "103_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Southron Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (2)</b>.<br>While you can spot a Southron, this minion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");

		assertEquals("Southron Runner", runner.getBlueprint().getTitle());
		assertNull(runner.getBlueprint().getSubtitle());
		assertFalse(runner.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, runner.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, runner.getBlueprint().getCulture());
		assertEquals(CardType.MINION, runner.getBlueprint().getCardType());
		assertEquals(Race.MAN, runner.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(runner, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(runner, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(runner, Keyword.AMBUSH));
		assertEquals(3, runner.getBlueprint().getTwilightCost());
		assertEquals(4, runner.getBlueprint().getStrength());
		assertEquals(1, runner.getBlueprint().getVitality());
		assertEquals(4, runner.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsTwoLessOnceASouthronIsInPlayAndNotWhileOnlyAnEasterlingIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var soldier = scn.GetShadowCard("soldier");
		var nomad = scn.GetShadowCard("nomad");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(runner, runner2, soldier, nomad);

		scn.StartGame();
		assertInZone(Zone.HAND, soldier);
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Only the Easterling is in play; the Southrons in hand are not spotted.
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertInZone(Zone.HAND, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(runner));
		assertEquals(3, scn.GetTwilightCost(runner2));
		assertEquals(4, scn.GetTwilightCost(nomad));

		scn.ShadowPlayCard(soldier);

		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetTwilightCost(runner));
		// A second copy has its own -2 and no more: the two discounts do not reach each other.
		assertEquals(1, scn.GetTwilightCost(runner2));
		// Another minion in the same hand, a Southron too, keeps its printed cost of 4: the discount is the Runner's own.
		assertEquals(4, scn.GetTwilightCost(nomad));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWhileASouthronIsInPlayItTakesTwoLessFromThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		// At site 2 the Runner (site 4) is roaming: 3 - 2 + 2 roaming. Without the discount it would want 5,
		// and with -3 it would be offered from 3.
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(runner));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(runner));
		scn.ShadowPlayCard(runner);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var rearguard = scn.GetShadowCard("rearguard");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		// The Axeman, no Southron, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertEquals(3, scn.GetTwilightCost(runner));
		scn.ShadowUseCardAction(rearguard);

		// A Southron minion now, and still no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
