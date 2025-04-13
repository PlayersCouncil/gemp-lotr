package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_051_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("wrath", "102_51");
					put("javelin", "7_248");

					//NEEDS MORE TESTS
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NowforWrathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [rohan] item or [rohan] ally is discarded or killed, add a [rohan] token here.
		* 	Each time a minion is exerted by a Free Peoples card, you may remove a [rohan] token here and exert a valiant companion to wound that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Now for Wrath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NowforWrathReactsToReconciledPossessionDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		scn.FreepsMoveCardToSupportArea(wrath);
		scn.FreepsMoveCardToHand(javelin);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.FreepsChooseCard(javelin);
		assertEquals(Zone.DISCARD, javelin.getZone());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}
}
