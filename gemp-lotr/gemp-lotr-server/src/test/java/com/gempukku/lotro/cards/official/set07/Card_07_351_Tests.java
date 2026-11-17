package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_351_Tests
{

	// The King path, Minas Tirith Sixth Circle its site 6 in place of the Seventh Circle.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability used only on purpose
					// Pippin, Trollslayer: [gondor] Hobbit companion, vitality 4; to play, spot a [gondor] companion (cheated in)
					put("pippin", "103_31");
					put("household", "5_83");   // Household Guard: [rohan] Man companion, vitality 3; Valiant is his whole text
					// Gondorian Merchant: the [gondor] ally, a Man, vitality 1; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Ranger's Cloak: [gondor] cloak on a [gondor] Man, vitality +1, so the Merchant can carry a wound
					put("cloak", "15_68");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_351");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasTirithSixthCircleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Minas Tirith Sixth Circle
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. If the fellowship moves from this site during the regroup phase, heal each [gondor] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Minas Tirith Sixth Circle", card.getBlueprint().getTitle());
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
	public void ARegroupMoveFromTheSiteHealsEachGondorCompanionOnceAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(boromir, pippin, household);
		scn.MoveCardsToSupportArea(merchant);
		scn.AttachCardsTo(merchant, cloak);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(household, 1);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(frodo, 1);

		// The move TO the Sixth Circle, in the fellowship phase, heals nobody.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals("Minas Tirith Sixth Circle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// A [gondor] Man and a [gondor] Hobbit, one wound each; not the [rohan] Man, the [gondor] ally or Frodo.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheSiteInTheRegroupPhaseOrFromItInTheFellowshipPhaseHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddWoundsToChar(boromir, 2);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		// The regroup move from site 5 TO the Sixth Circle.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(boromir));

		// The next turn begins at the Sixth Circle; its sanctuary heals are declined.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(boromir));

		// The move FROM the Sixth Circle, in the fellowship phase.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
