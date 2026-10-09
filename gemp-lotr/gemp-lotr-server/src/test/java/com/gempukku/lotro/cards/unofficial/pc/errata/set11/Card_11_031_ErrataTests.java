package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_031_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("account", "61_31");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally, vitality 4
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is an Ent, vitality 4

					// Shadow cards for the discard pile and the hand.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] minion
					put("savage", "1_151");     // Uruk Savage: [isengard] minion

					// Free Peoples cards for the discard pile and the hand.
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession
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
		 * Game Text: To play, exert a [gandalf] Wizard.
		 * Discard a card from hand to take a Shadow card from your discard pile into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("account");

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
	public void ExertsAGandalfWizardAndDiscardsACardFromHandToTakeAShadowCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var fear = scn.GetFreepsCard("fear");
		var raiders = scn.GetFreepsCard("raiders");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var shadowSavage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(radagast);
		// In hand beside the event: a Free Peoples card and a Shadow card.
		scn.MoveCardsToHand(account, armor, raiders);
		// In the discard pile: a Shadow minion, a Shadow condition and a Free Peoples card.
		scn.MoveCardsToDiscard(troop, fear, guard);
		// A Shadow card in the other player's discard pile, of which this player's copy is still in his draw deck.
		scn.MoveCardsToDiscard(shadowSavage);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(account));

		scn.FreepsPlayCard(account);

		// The exertion: either Wizard, companion or ally, and not the Ent of Gandalf's culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The discard: either of the other cards in hand, and not the event that is being played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(account));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(armor);
		assertInDiscard(armor);
		assertInHand(raiders);

		// The card taken: a Shadow card of either type from this player's own discard pile.  Not the Free Peoples
		// cards there (one of them just discarded), not the Shadow card still in hand, and not the other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, fear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, armor, raiders, shadowSavage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(fear);

		assertInHand(fear, raiders);
		assertInDiscard(troop, guard, armor, account, shadowSavage);
		// 3 in hand - the event - the card discarded + the card taken.
		assertEquals(2, scn.GetFreepsHandCount());
		// 3 in the pile - the card taken + the card discarded + the event.
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowCardDiscardedToPayForItMayBeTheCardTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(account, armor, raiders);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(account);
		// Gandalf is the only Wizard: he exerts without a choice, and the Ent does not.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, raiders));
		scn.FreepsChooseCard(raiders);
		assertInDiscard(raiders);

		// The cost is paid before the card to take is chosen, so the minion just discarded is a Shadow card in
		// the discard pile like the one that was there before.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		scn.FreepsChooseCardBPFromSelection(raiders);

		assertInHand(raiders, armor);
		assertInDiscard(troop, account);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyGandalfWizardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(account, armor);
		scn.MoveCardsToDiscard(troop);
		// Vitality 4 with 3 wounds: Gandalf cannot exert.  The Ent of his culture could.
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsPlayAvailable(account));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoOtherCardInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetFreepsCard("troop");
		var fear = scn.GetFreepsCard("fear");

		scn.MoveCompanionsToTable(gandalf);
		// The event is the only card in hand, and a card being played cannot be discarded to pay for itself.
		scn.MoveCardsToHand(account);
		scn.MoveCardsToDiscard(troop, fear);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsPlayAvailable(account));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowCardInTheDiscardPileTheCostsArePaidAndNothingIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var shadowSavage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(account, armor);
		// Only a Free Peoples card in this player's discard pile; the one Shadow card is in the other player's.
		scn.MoveCardsToDiscard(guard, shadowSavage);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(account));

		scn.FreepsPlayCard(account);

		// One Wizard and one other card in hand: both costs are paid without a choice.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(guard, armor, account, shadowSavage);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
