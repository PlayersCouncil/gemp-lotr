package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_01_017_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "51_17");
					put("guard", "51_7");
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
		* Title: Grimir, Dwarven Elder
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Dwarven
		* Twilight Cost: 1
		* Type: ally
		* Subtype: Dwarf
		* Strength: 3
		* Vitality: 3
		* Site Number: 3
		* Game Text: <b>Fellowship:</b> Exert Grimir and discard the top grimir of your draw deck to place a [dwarven] event from your discard pile on top of your draw deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");

		assertTrue(grimir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, grimir.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, grimir.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, grimir.getBlueprint().getCardType());
		assertEquals(Race.DWARF, grimir.getBlueprint().getRace());
		assertEquals(1, grimir.getBlueprint().getTwilightCost());
		assertEquals(3, grimir.getBlueprint().getStrength());
		assertEquals(3, grimir.getBlueprint().getVitality());
		assertTrue(grimir.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
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

		var topdeck = scn.GetFromTopOfFreepsDeck(1);
		var topdeck2 = scn.GetFromTopOfFreepsDeck(2);
		assertEquals(Zone.DECK, topdeck.getZone());
		assertEquals(Zone.DECK, topdeck2.getZone());
		assertEquals(Zone.DISCARD, event.getZone());
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsUseCardAction(grimir);
		assertEquals(Zone.DISCARD, topdeck.getZone());
		assertEquals(Zone.DECK, topdeck2.getZone());
		assertEquals(Zone.DECK, event.getZone());
		assertEquals(scn.GetFreepsTopOfDeck(), event);
		assertEquals(1, scn.GetWoundsOn(grimir));
	}
}
