package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "1_17");
					put("guard", "1_7");
					put("event", "1_3");

					put("runner1", "1_178");
					put("runner2", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Grimir, Dwarven Elder
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Dwarf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fellowship:</b>  Exert Grimir to shuffle a [dwarven] event from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimir");

		assertEquals("Grimir", card.getBlueprint().getTitle());
		assertEquals("Dwarven Elder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void GrimirAbilityExertsAndDiscardsTopDeckToRetrieveDwarvenEvent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var event = scn.GetFreepsCard("event");
		scn.MoveCompanionsToTable(grimir);
		scn.MoveCardsToDiscard(guard, event);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(grimir));

		assertEquals(Zone.DISCARD, event.getZone());
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsUseCardAction(grimir);

		assertEquals(Zone.DECK, event.getZone());
		assertEquals(1, scn.GetWoundsOn(grimir));
	}
}
