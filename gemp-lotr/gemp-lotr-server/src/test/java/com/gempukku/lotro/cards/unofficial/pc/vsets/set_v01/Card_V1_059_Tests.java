
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_059_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("sam", "1_311");
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
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void RivendellGatewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Rivendell Gateway
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 0
		* Type: sanctuary
		* Subtype: 
		* Site Number: 3
		* Game Text: Sanctuary. Forest.  Fellowship: Exert a companion to make a companion of another culture strength +1
		 * until the end of the turn
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl site3 = scn.GetFreepsSite(3);

		assertFalse(site3.getBlueprint().isUnique());
		//assertEquals(Side.FREE_PEOPLE, site3.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, site3.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(site3, Keyword.SANCTUARY));
		assertTrue(scn.hasKeyword(site3, Keyword.FOREST));
		assertEquals(0, site3.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(3, site3.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FellowshipActionExertsCompanionOfOneCultureToPumpAnotherUntilEndOfTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(legolas, sam);

		scn.StartGame();

		scn.SkipToSite(3);
		PhysicalCardImpl site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(site3));

		scn.FreepsUseCardAction(site3);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.GetFreepsCardChoiceCount());

		scn.FreepsChooseCard(legolas);
		assertTrue(scn.FreepsDecisionAvailable("Choose a companion"));
		assertEquals(2, scn.GetFreepsCardChoiceCount()); //Legolas can only boost sam and frodo
		scn.FreepsChooseCard(sam);

		assertEquals(4, scn.GetStrength(sam));

		scn.FreepsUseCardAction(site3);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to exert"));
		assertEquals(3, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCard(legolas);
		assertTrue(scn.FreepsDecisionAvailable("Choose a companion"));
		assertEquals(2, scn.GetFreepsCardChoiceCount()); //Legolas can only boost sam and frodo
		scn.FreepsChooseCard(sam);

		assertEquals(5, scn.GetStrength(sam));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetStrength(sam));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetStrength(sam));
	}

	@Test
	public void FellowshipActionLimitedTo6PerTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		scn.FreepsMoveCharToTable(boromir);

		scn.StartGame();

		scn.SkipToSite(3);
		PhysicalCardImpl site3 = scn.GetCurrentSite();
		assertEquals(4, scn.GetStrength(frodo));

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(5, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(6, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(7, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(8, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(9, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertTrue(scn.FreepsActionAvailable(site3));
		scn.FreepsUseCardAction(site3);
		scn.FreepsChooseCard(boromir);
		assertEquals(10, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(boromir);

		assertFalse(scn.FreepsActionAvailable(site3));

	}
}
