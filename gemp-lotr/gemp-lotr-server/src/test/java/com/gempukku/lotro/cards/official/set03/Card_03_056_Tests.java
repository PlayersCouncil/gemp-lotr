package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forger1", "3_56");
					put("forger2", "3_56");   // not unique: "this minion" is read beside another copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardForgerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Forger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Exert this minion to add (1).
		*/

		var scn = GetScenario();

		var forger = scn.GetFreepsCard("forger1");

		assertEquals("Isengard Forger", forger.getBlueprint().getTitle());
		assertNull(forger.getBlueprint().getSubtitle());
		assertFalse(forger.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, forger.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, forger.getBlueprint().getCulture());
		assertEquals(CardType.MINION, forger.getBlueprint().getCardType());
		assertEquals(Race.ORC, forger.getBlueprint().getRace());
		assertEquals(2, forger.getBlueprint().getTwilightCost());
		assertEquals(6, forger.getBlueprint().getStrength());
		assertEquals(2, forger.getBlueprint().getVitality());
		assertEquals(4, forger.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToAddOneUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forger1 = scn.GetShadowCard("forger1");
		var forger2 = scn.GetShadowCard("forger2");

		scn.StartGame();
		scn.MoveMinionsToTable(forger1, forger2);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(forger1));
		assertEquals(0, scn.GetWoundsOn(forger2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(forger1));
		assertTrue(scn.ShadowActionAvailable(forger2));

		scn.ShadowUseCardAction(forger1);
		// The Forger used exerts, and the copy beside it does not.
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(forger1));
		assertEquals(0, scn.GetWoundsOn(forger2));
		// Vitality 2 with 1 wound: exhausted, so it can no longer exert; the copy still can.
		assertFalse(scn.ShadowActionAvailable(forger1));
		assertTrue(scn.ShadowActionAvailable(forger2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(forger2);
		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(forger1));
		assertEquals(1, scn.GetWoundsOn(forger2));
		assertFalse(scn.ShadowActionAvailable(forger1));
		assertFalse(scn.ShadowActionAvailable(forger2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
