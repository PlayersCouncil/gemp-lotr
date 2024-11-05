package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_043_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("eyes", "51_43");
					put("arwen", "1_30");
					put("elrond", "1_40");
					put("card1", "1_41");
					put("card2", "1_42");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FarseeingEyesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Far-seeing Eyes
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: Each time you play an Elf, draw a card.  Any Shadow player may discard a card from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var eyes = scn.GetFreepsCard("eyes");

		assertTrue(eyes.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eyes.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, eyes.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, eyes.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(eyes, Keyword.SUPPORT_AREA));
		assertEquals(2, eyes.getBlueprint().getTwilightCost());
	}

	@Test
	public void FarseeingEyesDrawsACardEachTimeElfIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.FreepsMoveCardToHand("eyes", "arwen", "elrond");

		scn.StartGame();

		scn.FreepsPlayCard("eyes");
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.FreepsPlayCard("arwen");
		assertEquals(2, scn.GetFreepsHandCount()); //-1 from playing elf, +1 from draw
		assertEquals(1, scn.GetFreepsDeckCount());
		scn.FreepsPlayCard("elrond");
		assertEquals(2, scn.GetFreepsHandCount()); //-1 from playing elf, +1 from draw
		assertEquals(0, scn.GetFreepsDeckCount());
	}

	@Test
	public void FarseeingEyesShadowCanDiscardFromHandToPreventDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.FreepsMoveCardToHand("eyes", "arwen", "elrond");

		scn.ShadowMoveCardToHand("card1", "card2");

		scn.StartGame();

		scn.FreepsPlayCard("eyes");
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard("arwen");
		scn.ShadowChooseYes();
		scn.ShadowChooseCard("card1");
		assertEquals(1, scn.GetFreepsHandCount()); //draw blocked
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard("elrond");
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetFreepsHandCount()); //draw blocked
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
	}
}
