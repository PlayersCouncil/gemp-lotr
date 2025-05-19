package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_65");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengefulPrimitiveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vengeful Primitive
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: You may play this minion any time you could play a maneuver event. When you play this minion in the maneuver phase, you may discard X [men] minions from play to add (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Vengeful Primitive", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void VengefulPrimitiveTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());
	}
}
