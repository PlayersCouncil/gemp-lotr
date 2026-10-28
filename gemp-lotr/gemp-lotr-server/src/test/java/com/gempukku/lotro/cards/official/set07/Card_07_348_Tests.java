package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_348_Tests
{

	// The King path, Minas Tirith Fourth Circle its site 6 in place of the Seventh Circle.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; valiant and nothing else
					// Merry, Master Holbytla: [rohan] Hobbit companion, vitality 4; his trigger answers another [rohan] Man's
					// exertion (none here), his skirmish ability is used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability used only on purpose
					// Rohirrim Herdsman: [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_348");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasTirithFourthCircleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Minas Tirith Fourth Circle
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. If the fellowship moves from this site during the regroup phase, heal each [rohan] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Minas Tirith Fourth Circle", card.getBlueprint().getTitle());
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
	public void ARegroupMoveFromTheSiteHealsEachRohanCompanionOnceAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveCardsToSupportArea(herdsman);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The move TO the Fourth Circle, in the fellowship phase, heals nobody.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Minas Tirith Fourth Circle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(merry));

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Two [rohan] companions, one wound each; not the [gondor] Man, the [rohan] ally or Frodo; a [rohan] Man and a [rohan] Hobbit alike.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheSiteInTheRegroupPhaseOrFromItInTheFellowshipPhaseHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The regroup move from site 5 TO the Fourth Circle.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(guard));

		// The next turn begins at the Fourth Circle; its sanctuary heals are declined.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(guard));

		// The move FROM the Fourth Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
