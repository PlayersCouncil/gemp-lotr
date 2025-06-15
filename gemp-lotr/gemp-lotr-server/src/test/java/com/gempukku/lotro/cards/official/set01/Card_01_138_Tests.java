package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_138");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansSnowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Snows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Spell</b>. <b>Weather</b>. To play, exert an [isengard] minion. Plays on a site.<br>No player may play skirmish events or use skirmish special abilities during skirmishes at this site. Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Saruman's Snows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SarumansSnowsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
