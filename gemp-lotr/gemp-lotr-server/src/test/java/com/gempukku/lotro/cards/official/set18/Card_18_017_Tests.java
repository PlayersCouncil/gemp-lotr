package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_17");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoodhallElfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Woodhall Elf, Exile
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: 
		 * Resistance: 1
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper><br>When you transfer this to an [elven] companion, you may draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Woodhall Elf", card.getBlueprint().getTitle());
		assertEquals("Exile", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WoodhallElfTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
