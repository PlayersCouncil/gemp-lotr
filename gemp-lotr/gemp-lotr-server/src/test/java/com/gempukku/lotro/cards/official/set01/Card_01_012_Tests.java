package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_12");

					put("card1", "1_191");
					put("card2", "1_178");
					put("card3", "1_179");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli, Dwarf of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Damage +1</b>
		 * 	 <b>Fellowship: </b> If the twilight pool has fewer than 2 twilight tokens, add (2) and place a card from hand beneath your draw deck.
		 */

		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Dwarf of Erebor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void GimliAbilityDoesntWorkWith2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToHand(card1);

		scn.StartGame();

		scn.FreepsPlayCard(gimli);
		//the 2 twilight from his cost should disqualify his own action
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void GimliAbilityAdds2TwilightAndPlacesOnBottom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		scn.MoveCompanionToTable(gimli);
		scn.MoveCardsToHand(card1);

		scn.StartGame();
		scn.MoveCardsToBottomOfDeck(card3);

		assertTrue(scn.FreepsActionAvailable(gimli));
		assertEquals(Zone.HAND, card1.getZone());
		assertEquals(Zone.DECK, card2.getZone());
		assertEquals(scn.GetFreepsTopOfDeck().getBlueprintId(), card2.getBlueprintId());
		assertEquals(Zone.DECK, card3.getZone());
		assertEquals(scn.GetFreepsBottomOfDeck().getBlueprintId(), card3.getBlueprintId());

		scn.FreepsUseCardAction(gimli);
		//card from hand placed on the bottom of the deck
		assertEquals(Zone.DECK, card1.getZone());
		assertEquals(scn.GetFreepsBottomOfDeck().getBlueprintId(), card1.getBlueprintId());
		//card from the deck was not drawn (as it is in the errata)
		assertEquals(Zone.DECK, card2.getZone());
	}
}
