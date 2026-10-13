package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_10");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two copies of the condition are the Free Peoples player's whole hand.
	protected VirtualTableScenario GetStackOfferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loyalty1", "7_10");
					put("loyalty2", "7_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoyaltyUnshakenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Loyalty Unshaken
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, you may stack 2 cards from hand here and draw a card for each [dwarven] card you stack.<br><b>Skirmish:</b> Make a Dwarf strength +2. Also, make that Dwarf <b>damage +2</b> for each [dwarven] card stacked on this condition. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Loyalty Unshaken", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  With no card left
	// in hand nothing can be stacked, and no card is drawn "for each [dwarven] card you stack".
	@Test
	public void TheOfferToStackIsMadeWhileACardIsLeftInHandAndNotOnceTheHandIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackOfferScenario();

		var loyalty1 = scn.GetFreepsCard("loyalty1");
		var loyalty2 = scn.GetFreepsCard("loyalty2");
		scn.MoveCardsToHand(loyalty1, loyalty2);

		scn.StartGame();

		// The second copy is still in hand: there is a card to stack.
		scn.FreepsPlayCard(loyalty1);
		assertInZone(Zone.SUPPORT, loyalty1);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(loyalty1));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(loyalty2);

		// It is the last card in hand: once it is played the hand is empty.
		scn.FreepsPlayCard(loyalty2);
		assertInZone(Zone.SUPPORT, loyalty2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(loyalty2));
		assertEquals(0, scn.GetStackedCards(loyalty2).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void LoyaltyUnshakenTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
