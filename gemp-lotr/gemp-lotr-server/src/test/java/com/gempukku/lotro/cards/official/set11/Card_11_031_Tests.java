package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_31");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTakeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("account", "11_31");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion

					// Free Peoples cards for the discard pile and the hand.
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession

					// Shadow cards for the discard pile and the hand.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] minion
					put("savage", "1_151");     // Uruk Savage: [isengard] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalAccountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Final Account
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: To play, exert a [gandalf] Wizard.<br>Discard 2 cards from hand to take a Free Peoples card and a Shadow card from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Final Account", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheCardsDiscardedToPayForItAreNotTheCardsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTakeScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetFreepsCard("troop");
		var fear = scn.GetFreepsCard("fear");
		var raiders = scn.GetFreepsCard("raiders");
		var savage = scn.GetFreepsCard("savage");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(account, armor, raiders, savage);
		scn.MoveCardsToDiscard(guard, axe, troop, fear);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(account, armor, raiders, savage);
		assertInDiscard(guard, axe, troop, fear);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(account);
		// Gandalf is the only Wizard and exerts without a choice; then 2 of the 3 other cards in hand.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, raiders, savage));
		scn.FreepsChooseCards(armor, raiders);
		assertInDiscard(armor, raiders);
		assertInHand(savage);

		// The cards just discarded for the cost lie in the discard pile and are not offered (Q-0320: A, the They
		// Are Coming ruling of Q-0192), nor is the event being played (CR 4.2 "event", which names this card).
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, account, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(guard);

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, fear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(raiders, savage, axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(troop);

		assertInHand(guard, troop, savage);
		assertInDiscard(axe, armor, fear, raiders, account);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FinalAccountTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
