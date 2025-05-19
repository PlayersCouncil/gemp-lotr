package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_242_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_310");
					put("gaffer", "1_291");
				}},
				new HashMap<>() {{
					put("greendragon", "11_242");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void GreenDragonInnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Green Dragon Inn
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Dwelling</b>. At the start of your fellowship phase, you may exert 2 Hobbits to play a Hobbit from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("greendragon");

		assertEquals("Green Dragon Inn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GreenDragonInnIsOnlyTriggeredByFreePeoplesPlayerAndNotShadow() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionToTable(sam);
		scn.MoveCardsToTopOfDeck(gaffer);

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
