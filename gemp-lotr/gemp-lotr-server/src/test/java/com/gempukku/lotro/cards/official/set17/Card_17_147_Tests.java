package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_147_Tests
{
	// Imladris stands in each adventure deck in place of site 9, which no test reaches, and a test puts the
	// Shadow player's copy on the Fellowship-block path as the next site the fellowship moves to: there it is a
	// sanctuary as site 3 or 6, and not as any other number.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("imladris", "17_147");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Race Text 94_6, "Site 8 is a sanctuary.": a sanctuary that is no site 3 or 6.
	protected VirtualTableScenario GetSite8SanctuaryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("imladris", "17_147");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_6",
				null
		);
	}

	@Test
	public void ImladrisStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Imladris
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>River</b>. When the fellowship moves to this site, if it is a sanctuary, remove 2 burdens.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("imladris");

		assertEquals("Imladris", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AsSiteThreeASanctuaryImladrisRemovesTwoBurdensAndMovingOnRemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imladris = scn.GetShadowSite("imladris");

		scn.StartGame();
		// The other player's Ring-bearer carries burdens of his own, put on in his turn.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(2);
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// Imladris is the next site, site 3.
		scn.MoveCardToAdventurePath(imladris);
		scn.AddBurdens(3);
		assertEquals(3, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());

		scn.FreepsPass();

		assertEquals(imladris, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Exactly two of the three, and only from the moving fellowship's Ring-bearer.
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.AddBurdens(2);
		assertEquals(3, scn.GetBurdens());

		// The regroup move from Imladris to site 4 removes none.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(imladris, scn.GetSite(3));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteFourWhichIsNoSanctuaryImladrisRemovesNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imladris = scn.GetShadowSite("imladris");

		scn.StartGame();
		scn.SkipToSite(3);
		// Imladris is the next site, site 4.
		scn.MoveCardToAdventurePath(imladris);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals(imladris, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(imladris, Keyword.SANCTUARY));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteEightMadeASanctuaryByAnotherCardImladrisRemovesTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSite8SanctuaryScenario();

		var imladris = scn.GetShadowSite("imladris");

		scn.StartGame();
		scn.SkipToSite(7);
		// Imladris is the next site, site 8, which Race Text 94_6 makes a sanctuary.
		scn.MoveCardToAdventurePath(imladris);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());

		scn.FreepsPass();

		assertEquals(imladris, scn.GetCurrentSite());
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(imladris, Keyword.SANCTUARY));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
