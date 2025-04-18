package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_012_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("gimli", "51_12");

					put("card1", "1_191");
					put("card2", "1_178");
					put("card3", "1_179");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: *Gimli, Dwarf of Erebor
		* Side: Free Peoples
		* Culture: Dwarven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Dwarf
		* Strength: 6
		* Vitality: 3
		* Signet: aragorn
		* Game Text: <b>Damage +1</b> 
		* 	 <b>Fellowship: </b> If the twilight pool has fewer than 2 twilight tokens, add (2) and place a card from hand beneath your draw deck to draw a card.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");

		assertTrue(gimli.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gimli.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, gimli.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, gimli.getBlueprint().getCardType());
		assertEquals(Race.DWARF, gimli.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(2, gimli.getBlueprint().getTwilightCost());
		assertEquals(6, gimli.getBlueprint().getStrength());
		assertEquals(3, gimli.getBlueprint().getVitality());
		assertEquals(6, gimli.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, gimli.getBlueprint().getSignet());
	}

	@Test
	public void GimliAbilityDoesntWorkWith2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl card1 = scn.GetFreepsCard("card1");
		PhysicalCardImpl card2 = scn.GetFreepsCard("card2");
		scn.FreepsMoveCardToHand(gimli);
		scn.FreepsMoveCardToHand(card1);

		scn.StartGame();

		scn.FreepsPlayCard(gimli);
		//the 2 twilight from his cost should disqualify his own action
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void GimliAbilityAdds2TwilightAndPlacesOnBottom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl card1 = scn.GetFreepsCard("card1");
		PhysicalCardImpl card2 = scn.GetFreepsCard("card2");
		PhysicalCardImpl card3 = scn.GetFreepsCard("card3");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsMoveCardToHand(card1);
		
		scn.StartGame();

		scn.FreepsMoveCardsToBottomOfDeck(card3);

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
		//card from the deck was drawn
		assertEquals(Zone.HAND, card2.getZone());
	}
}
