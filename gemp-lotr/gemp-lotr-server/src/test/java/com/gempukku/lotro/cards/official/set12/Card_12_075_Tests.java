package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_75");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PoisonousWordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Poisonous Words
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver or regroup
		 * Game Text: <b>Toil 2</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -2.)</helper><br>Add (1) for each [men] card you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Poisonous Words", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void PoisonousWordsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
