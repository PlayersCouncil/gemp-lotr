package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_136_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "12_136");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BerserkerTorchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Berserker Torch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: <b>Toil 1</b>. <helper>(For each [uruk-hai] character you exert when playing this, its twilight cost is -1.)</helper><br>Bearer must be an [uruk-hai] Uruk-hai.<br>Bearer is strength +2 for each wound on each character in its skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Berserker Torch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BerserkerTorchTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
