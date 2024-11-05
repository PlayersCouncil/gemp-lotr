package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_043_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("eyes", "1_43");
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
		 * Name: Far-seeing Eyes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play an Elf, choose an opponent to discard a card from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyes");

		assertEquals("Far-seeing Eyes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FarseeingEyesMakesShadowDiscardCardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.FreepsMoveCardToHand("eyes", "arwen", "elrond");

		scn.ShadowMoveCardToHand("card1", "card2");

		scn.StartGame();

		scn.FreepsPlayCard("eyes");
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard("arwen");
		scn.ShadowChooseCard("card1");
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard("elrond");
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
	}
}
