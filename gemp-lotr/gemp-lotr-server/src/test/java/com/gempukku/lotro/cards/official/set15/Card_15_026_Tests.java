package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_26");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UruvielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruviel, Woodland Maid
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype: 
		 * Resistance: 1
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may add (1) to transfer this to a companion.)</helper><br>Each time you transfer this to a companion, you may exert an Elf to make the fellowship's current site a <b>forest</b> until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruviel", card.getBlueprint().getTitle());
		assertEquals("Woodland Maid", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void UruvielTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
