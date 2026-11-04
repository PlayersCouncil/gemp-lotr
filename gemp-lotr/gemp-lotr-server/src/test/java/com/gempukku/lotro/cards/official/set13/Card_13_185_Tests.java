package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_185_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Nothing but the Ring-bearer: the burdens are the only thing the site reads.
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with the Shaft in place of site 9, which no test reaches: the Shaft is
					// put on the path as the next site wherever a test wants it, and takes that site's number.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("shaft", "13_185");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AbandonedMineShaftStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Abandoned Mine Shaft
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Underground. If this site is not in region 1 when the fellowship moves from here during the
		 * 	regroup phase, add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("shaft");

		assertEquals("Abandoned Mine Shaft", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void MovingFromTheShaftAtSiteFourInTheRegroupPhaseAddsABurdenToTheMoversRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(3);
		// The Shadow player's copy: the site's owner is not the player whose fellowship moves.
		var shaft = scn.GetShadowSite("shaft");
		scn.MoveCardToAdventurePath(shaft);
		scn.FreepsPass();
		assertSame(shaft, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());

		scn.FreepsChooseToMove();
		// Site 4 is in region 2.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromTheShaftAtSiteSevenInTheRegroupPhaseAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(6);
		var shaft = scn.GetShadowSite("shaft");
		scn.MoveCardToAdventurePath(shaft);
		scn.FreepsPass();
		assertSame(shaft, scn.GetCurrentSite());
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsChooseToMove();
		// Site 7 is in region 3, and no more in region 1 than site 4.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromTheShaftAtSiteThreeInTheRegroupPhaseAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(2);
		var shaft = scn.GetShadowSite("shaft");
		scn.MoveCardToAdventurePath(shaft);
		scn.FreepsPass();
		assertSame(shaft, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsChooseToMove();
		// Site 3 is the last site of region 1.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheShaftInTheRegroupPhaseOrFromItInTheFellowshipPhaseAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		var shaft = scn.GetShadowSite("shaft");
		scn.MoveCardToAdventurePath(shaft);

		scn.SkipToMovementDecision();
		assertEquals(0, scn.FreepsGetBurdens());
		// The regroup move TO the Shaft, at site 4.
		scn.FreepsChooseToMove();
		assertSame(shaft, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertSame(shaft, scn.GetCurrentSite());
		assertEquals(0, scn.FreepsGetBurdens());

		// The fellowship-phase move FROM the Shaft, in region 2.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
