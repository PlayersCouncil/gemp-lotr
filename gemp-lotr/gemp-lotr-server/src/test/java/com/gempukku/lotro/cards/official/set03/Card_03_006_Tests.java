package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_6");
					put("uruk", "1_151");       // Uruk Savage: a Shadow card of twilight cost 2
					// Three [dwarven] cards for the discard pile:
					put("axe", "1_9");          // Dwarven Axe
					put("armor", "1_8");        // Dwarven Armor
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StormofArgumentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Storm of Argument
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Reveal a card at random from an opponent's hand. Shuffle up to X [dwarven] cards from your discard pile into your draw deck, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Storm of Argument", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// A lone test, from engine pass 26 (F-0391): the event's first sentence is now written as its three siblings
	// write it (Friends of Old 3_16, His First Serious Check 3_33, Might of Númenor 3_43), and X is still the
	// twilight cost of the card revealed.  The card's own treatment is still to come.  With No Business of Ours
	// in play nothing is revealed: Card_02_044_Tests.
	@Test
	public void TheCardRevealedAtRandomIsShownToTheFreePeoplesPlayerAndItsTwilightCostIsX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetFreepsCard("card");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(storm);
		scn.MoveCardsToDiscard(axe, armor, guard);

		// One card in the Shadow hand, so that the card revealed at random is not left to chance.  Its twilight
		// cost is 2, and the event's own is 1.
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(storm));
		scn.FreepsPlayCard(storm);
		assertEquals(1, scn.GetTwilight());

		// The player of the event is shown the card; the owner of the hand is asked nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();

		// Up to 2 of the three [dwarven] cards, the cost of the card revealed.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		scn.FreepsChooseCards(axe, armor);

		assertInZone(Zone.DECK, axe);
		assertInZone(Zone.DECK, armor);
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.DISCARD, storm);
		assertInZone(Zone.HAND, uruk);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void StormofArgumentTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
