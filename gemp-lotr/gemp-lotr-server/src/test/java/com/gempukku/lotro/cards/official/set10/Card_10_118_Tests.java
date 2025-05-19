package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "1_306");
					put("bowmen", "3_41");
					put("knows", "9_50");

					put("savage", "1_151");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "10_118"); //here
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorPrairieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Pelennor Prairie
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: 4K
		 * Game Text: <b>Plains</b>. <b>Shadow:</b> Exert a minion and remove a burden to make
		 * the Free Peoples player discard one of his or her conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Pelennor Prairie", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PelennorPrairieIsNotBlockedByPippinFriendToFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var prairie = scn.GetShadowSite(4);

		var pippin = scn.GetFreepsCard("pippin");
		var bowmen = scn.GetFreepsCard("bowmen");
		var knows = scn.GetFreepsCard("knows");
		scn.MoveCompanionToTable(pippin);
		scn.MoveCardsToSupportArea(bowmen, knows);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		scn.SkipToSite(3);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(prairie, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(prairie));

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetBurdens());
		scn.ShadowUseCardAction(prairie);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.FreepsGetCardChoiceCount());

	}
}
