package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("taskmaster1", "2_90");
					put("taskmaster2", "2_90");   // not unique: "this minion" is read beside another copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcTaskmasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Orc Taskmaster
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Exert this minion to add (1).
		*/

		var scn = GetScenario();

		var taskmaster = scn.GetFreepsCard("taskmaster1");

		assertEquals("Orc Taskmaster", taskmaster.getBlueprint().getTitle());
		assertNull(taskmaster.getBlueprint().getSubtitle());
		assertFalse(taskmaster.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, taskmaster.getBlueprint().getSide());
		assertEquals(Culture.SAURON, taskmaster.getBlueprint().getCulture());
		assertEquals(CardType.MINION, taskmaster.getBlueprint().getCardType());
		assertEquals(Race.ORC, taskmaster.getBlueprint().getRace());
		assertEquals(3, taskmaster.getBlueprint().getTwilightCost());
		assertEquals(8, taskmaster.getBlueprint().getStrength());
		assertEquals(3, taskmaster.getBlueprint().getVitality());
		assertEquals(6, taskmaster.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToAddOneUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var taskmaster2 = scn.GetShadowCard("taskmaster2");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster1, taskmaster2);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(taskmaster1));
		assertEquals(0, scn.GetWoundsOn(taskmaster2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(taskmaster1));
		assertTrue(scn.ShadowActionAvailable(taskmaster2));

		scn.ShadowUseCardAction(taskmaster1);
		// The Taskmaster used exerts, and the copy beside it does not.
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(taskmaster1));
		assertEquals(0, scn.GetWoundsOn(taskmaster2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(taskmaster1);
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(taskmaster1));
		assertEquals(0, scn.GetWoundsOn(taskmaster2));

		// Vitality 3 with 2 wounds: exhausted, so it can no longer exert; the copy still can.
		assertFalse(scn.ShadowActionAvailable(taskmaster1));
		assertTrue(scn.ShadowActionAvailable(taskmaster2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
