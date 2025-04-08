package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_002_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("raider", "102_2");

					put("card1", "102_3");
					put("card2", "102_4");
					put("card3", "102_5");
					put("card4", "102_6");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DunlendingRaiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Dunlending Raider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While you have initiative, this minion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raider");

		assertEquals("Dunlending Raider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DunlendingRaiderCosts3IfFreepsHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var raider = scn.GetShadowCard("raider");
		scn.ShadowMoveCardToHand(raider);

		scn.FreepsMoveCardToHand("card1", "card2", "card3", "card4");

		scn.StartGame();
		scn.SetTwilight(7);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(raider));
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(raider);
		//10 in the pool, -3 for full price -2 for roaming
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void DunlendingRaiderCosts1IfShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var raider = scn.GetShadowCard("raider");
		scn.ShadowMoveCardToHand(raider);

		scn.StartGame();
		scn.SetTwilight(7);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(raider));
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(raider);
		//10 in the pool, -1 for full price -2 for roaming
		assertEquals(7, scn.GetTwilight());
	}
}
