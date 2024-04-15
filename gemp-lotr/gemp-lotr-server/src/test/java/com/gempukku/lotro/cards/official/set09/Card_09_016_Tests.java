package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_016_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "9_16");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GlorfindelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Glorfindel, Revealed in Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 3

		 * Game Text: When Glorfindel is in your starting fellowship, his twilight cost is -2.<br><b>Skirmish:</b> Reveal the top card of your draw deck. You may exert Glorfindel to make a Nazgûl he is skirmishing strength -X, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Glorfindel", card.getBlueprint().getTitle());
		assertEquals("Revealed in Wrath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GlorfindelTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
