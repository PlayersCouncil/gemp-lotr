package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_352_Tests
{

	// The King path, Minas Tirith Third Circle its site 6 in place of the Seventh Circle.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; to play, spot a [gondor] companion (cheated in)
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit, vitality 4; to play, add a burden (cheated in)
					put("smeagol", "103_116");
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability used only on purpose
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_352");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasTirithThirdCircleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Minas Tirith Third Circle
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. If the fellowship moves from this site during the regroup phase, heal each Hobbit companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Minas Tirith Third Circle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void ARegroupMoveFromTheSiteHealsEachHobbitCompanionOnceAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveCardsToSupportArea(bounder);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(bounder, 1);

		// The move TO the Third Circle, in the fellowship phase, heals nobody.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Minas Tirith Third Circle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// A [shire] Hobbit who is the Ring-bearer and a [gondor] Hobbit, one wound each; not the [shire] companion
		// who is no Hobbit, nor the Hobbit who is an ally.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheSiteInTheRegroupPhaseOrFromItInTheFellowshipPhaseHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddWoundsToChar(pippin, 2);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The regroup move from site 5 TO the Third Circle.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetWoundsOn(pippin));
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(pippin));

		// The next turn begins at the Third Circle; its sanctuary heals are declined.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(pippin));

		// The move FROM the Third Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
