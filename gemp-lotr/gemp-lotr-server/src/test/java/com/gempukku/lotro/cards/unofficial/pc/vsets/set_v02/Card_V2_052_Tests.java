package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_052_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("ruin", "102_52");
					put("eowyn", "13_124");
					put("arrowslits", "5_80");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NowforRuinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Ruin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a minion is killed, spot 2 valiant companions to add a [rohan] token here.
		* 	Each time there are 3 [rohan] tokens here, remove all tokens here to play a [rohan] condition or event from your discard pile. The Shadow player may then play a minion from their discard pile. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruin");

		assertEquals("Now for Ruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NowforRuinTriggersOnReinforceOfSecondToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCardToDiscard(arrowslits);
		scn.FreepsMoveCharToTable("eowyn"); // On a regroup move, reinforces a Rohan token

		scn.StartGame();
		scn.AddTokensToCard(ruin, 2);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		scn.FreepsChooseToMove();
		scn.FreepsAcceptOptionalTrigger(); //Eowyn's reinforcement
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertEquals(Zone.SUPPORT, arrowslits.getZone());
	}
}
