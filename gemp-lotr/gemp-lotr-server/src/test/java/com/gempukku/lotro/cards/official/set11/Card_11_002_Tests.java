package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_2");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheOneRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The One Ring, The Ruling Ring
		 * Unique: True
		 * Side: 
		 * Culture: 
		 * Twilight Cost: 
		 * Type: Onering
		 * Subtype: 
		 * Strength: 1
		 * Game Text: <b>Response:</b> If the Ring-bearer is about to take a wound in a skirmish, he or she wears The One Ring until the regroup phase.<br>While the Ring-bearer is wearing The One Ring, each time he or she is about to take a wound in a skirmish, add a burden instead.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetRing();
		var card = scn.GetFreepsCard("card");

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Ruling Ring", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheOneRingTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
