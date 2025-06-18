package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("merry", "1_302");
					put("legolas", "1_50");
					put("boromir", "3_122");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "101_59");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RivendellGatewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Rivendell Gateway
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3
		 * Game Text: Sanctuary. Fellowship: Exert an unbound companion to make one of another culture strength +1 until the end of the turn (limit +6).
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell Gateway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(card, Keyword.FOREST));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FellowshipActionExertsCompanionOfOneCultureToPumpAnotherUntilEndOfTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(legolas, merry, boromir);

		scn.StartGame();

		scn.SkipToSite(3);
		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(site3));

		scn.FreepsUseCardAction(site3);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.FreepsGetCardChoiceCount()); //Frodo is not allowed

		scn.FreepsChooseCard(legolas);
		assertTrue(scn.FreepsDecisionAvailable("Choose a companion"));
		assertEquals(2, scn.FreepsGetCardChoiceCount()); //Legolas can only boost merry or boromir, not frodo
		scn.FreepsChooseCard(merry);

		assertEquals(4, scn.GetStrength(merry));

		scn.FreepsUseCardAction(site3);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(legolas);
		assertTrue(scn.FreepsDecisionAvailable("Choose a companion"));
		assertEquals(2, scn.FreepsGetCardChoiceCount()); //Legolas can only boost merry or boromir, not frodo
		scn.FreepsChooseCard(merry);

		assertEquals(5, scn.GetStrength(merry));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetStrength(merry));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetStrength(merry));
	}

	@Test
	public void FellowshipActionLimitedTo6PerTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();

		scn.SkipToSite(3);
		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetStrength(merry));

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(4, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(5, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(6, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(7, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(8, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(9, scn.GetStrength(merry));
		scn.FreepsUseCardAction(boromir);

		assertFalse(scn.FreepsActionAvailable(site3));

	}
}
