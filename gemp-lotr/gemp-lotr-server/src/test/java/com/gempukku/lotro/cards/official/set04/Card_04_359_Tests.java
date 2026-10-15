package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_359_Tests
{

	// The Towers path with Wizard's Vale as its site 8, in place of Ring of Isengard 4_358.  Site 7 is Helm's Tower
	// 102_71 (Shadow number 6; damage +1 only while 6 companions can be spotted).  No other card: the fellowship is
	// Frodo alone, so each move adds the Shadow number and 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>() {{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_359");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardsValeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wizard's Vale
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 8
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: At the start of the first Shadow phase, if the twilight pool has fewer than 9 twilight tokens, add (4) (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Wizard's Vale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheFirstShadowPhaseAPoolOfEightGetsFourMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetMoveCount());
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		// The turn's first move: 1 + 6 for Wizard's Vale + 1 for Frodo = 8, fewer than 9, and 4 more.
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(12, scn.GetTwilight());
	}

	@Test
	public void APoolOfNineAtTheStartOfTheFirstShadowPhaseGetsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());

		// 2 + 6 + 1 = 9: not fewer than 9.
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(9, scn.GetTwilight());
	}

	@Test
	public void TheSecondShadowPhaseOfATurnGetsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(6);

		// The first move of the turn is to Helm's Tower: the first Shadow phase is there.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseToMove();

		// The second move, to Wizard's Vale: 0 + 6 + 1 = 7, fewer than 9, but this is the turn's second Shadow phase.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetTwilight());
	}
}
