package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_054_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("freebooter", "8_54");
					put("wargalley", "8_59");
					put("leaders", "7_112");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CorsairFreebooterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Freebooter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, you may remove 2 culture tokens to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("freebooter");

		assertEquals("Corsair Freebooter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.CORSAIR));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CorsairFreebooterRemovesArbitraryTokensToReinforceRaiderTokenTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var wargalley = scn.GetShadowCard("wargalley");
		scn.ShadowMoveCardToHand(freebooter);
		scn.ShadowMoveCardToSupportArea(wargalley);

		var leaders = scn.GetFreepsCard("leaders");
		scn.FreepsMoveCardToSupportArea(leaders);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddTokensToCard(leaders, 3);
		scn.AddTokensToCard(wargalley, 1);

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(freebooter));
		assertEquals(3, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(wargalley));

		scn.ShadowPlayCard(freebooter);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		//Both Noble Leaders and Corsair War Galley have tokens on them to remove
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(leaders);
		scn.ShadowChooseCard(leaders);

		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(3, scn.GetCultureTokensOn(wargalley));
	}
}
