package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_333_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("moors", "51_333");
					put("goblin1", "1_174");
					put("goblin2", "1_174");
					put("goblin3", "1_174");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("moors", "51_333");
					put("site3", "1_337");
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
	public void MidgewaterMoorsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Midgewater Moors
		* Unique: False
		* Side: 
		* Culture: 
		* Twilight Cost: 1
		* Type: site
		* Subtype: Standard
		* Site Number: 2
		* Game Text: <b>Plains.</b> Each time a minion is played, the Free Peoples player must exert a companion or discard a card from hand.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var moors = scn.GetFreepsCard("moors");

		assertFalse(moors.getBlueprint().isUnique());
		assertEquals(CardType.SITE, moors.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(moors, Keyword.PLAINS));
		assertEquals(1, moors.getBlueprint().getTwilightCost());
		assertEquals(2, moors.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, moors.getBlueprint().getSiteBlock());
	}

	@Test
	public void MidgewaterMoorsPlayingMinionsWithNoCardsInHandExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		scn.ShadowMoveCardToHand("goblin1", "goblin2", "goblin3");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard("goblin1");
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard("goblin2");
		assertEquals(2, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard("goblin3");
		assertEquals(3, scn.GetWoundsOn(frodo));
	}

	@Test
	public void MidgewaterMoorsPlayingMinionsWithNoVitalityDiscardsACardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		scn.ShadowMoveCardToHand("goblin1", "goblin2", "goblin3");
		scn.FreepsMoveCardToHand("goblin1", "goblin2", "goblin3");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(3, scn.GetFreepsHandCount());
		scn.ShadowPlayCard("goblin1");
		scn.FreepsChooseCard("goblin1");
		assertEquals(2, scn.GetFreepsHandCount());
		scn.ShadowPlayCard("goblin2");
		scn.FreepsChooseCard("goblin2");
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPlayCard("goblin3");
		assertEquals(0, scn.GetFreepsHandCount());
	}

	@Test
	public void MidgewaterMoorsFreepsChoosesExertionOrDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		scn.ShadowMoveCardToHand("goblin1", "goblin2", "goblin3");
		scn.FreepsMoveCardToHand("goblin1", "goblin2", "goblin3");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard("goblin1");
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoiceAvailable("Exert a companion");
		scn.FreepsChoiceAvailable("Discard a card from hand");
	}


}
