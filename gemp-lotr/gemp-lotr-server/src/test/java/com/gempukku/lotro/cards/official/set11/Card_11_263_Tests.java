package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_263_Tests
{

	// The Fellowship path, with West Gate of Moria in each deck in place of site 9 (which no test reaches); a test
	// lays it on the path as the site after the fellowship's.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("westgate", "11_263");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestGateofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: West Gate of Moria
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Underground</b>. When the fellowship moves from this site, remove (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("westgate");

		assertEquals("West Gate of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void MovingThereRemovesNothingAndMovingOnFromItRemovesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The Shadow player's copy, as a site 2 is on the first player's first turn.
		var westgate = scn.GetShadowSite("westgate");

		scn.StartGame();
		scn.MoveCardToAdventurePath(westgate);
		scn.SetTwilight(4);

		// The move to it: 4 + 2 for its Shadow number + 1 companion, and nothing removed.
		scn.FreepsPass();
		assertSame(westgate, scn.GetCurrentSite());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move from it to Rivendell Valley (Shadow number 0): 3 - 2 + 0 + 1 companion.
		scn.SkipToMovementDecision();
		scn.SetTwilight(3);
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
