package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("rampager", "5_1");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DunlendingRampagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Dunlending Rampager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play this minion, the Free Peoples player may discard 2 cards from hand to discard him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rampager");

		assertEquals("Dunlending Rampager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DunlendingRampagerDoesNotOfferChoiceWhenNotEnoughCardsInFreepsHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rampager = scn.GetShadowCard("rampager");
		scn.ShadowMoveCardToHand(rampager);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(rampager));
		scn.ShadowPlayCard(rampager);

		assertEquals(Zone.SHADOW_CHARACTERS, rampager.getZone());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 2 cards from hand to discard Dunlending Rampager?"));
	}
}
