package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_52");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianServantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gondorian Servant, Denethor's Handman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Follower
		 * Subtype: 
		 * Resistance: 3
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may add (1) to transfer this to a companion.)</helper><br>Each time you attach this to a companion, except a [gondor] Man, add (4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gondorian Servant", card.getBlueprint().getTitle());
		assertEquals("Denethor's Handman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getResistance());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GondorianServantTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
