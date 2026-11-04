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
					put("account", "11_31");
					put("gandalf", "8_15");     // Gandalf, Leader of Men: [gandalf] Wizard companion, vitality 4; text only for a starting fellowship
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ALLY (Hobbit set, Q-0312), vitality 4
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is an Ent and no Wizard, vitality 4

					// For the hand: a Free Peoples item, a Shadow character and a Shadow card that is neither.
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] minion
					put("weary", "4_212");      // Weary: [isengard] condition

					// For the discard pile: a companion, a possession and an event of each side.
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession
					put("delving", "1_6");      // Delving: [dwarven] Fellowship event
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition
					put("bred", "1_121");       // Bred for Battle: [isengard] Skirmish event

					// For the other player's discard pile only (this player's copies stay in his draw deck).
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion
					put("savage", "1_151");     // Uruk Savage: [isengard] minion
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
		 * Game Text: To play, exert a [gandalf] Wizard.
		 * 	Discard 2 cards from hand to take a Free Peoples card and a Shadow card from your discard pile into hand.
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

	@Test
	public void ExertsAGandalfWizardAndDiscards2CardsToTakeAFreePeoplesCardAndAShadowCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var armor = scn.GetFreepsCard("armor");
		var raiders = scn.GetFreepsCard("raiders");
		var weary = scn.GetFreepsCard("weary");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var delving = scn.GetFreepsCard("delving");
		var troop = scn.GetFreepsCard("troop");
		var fear = scn.GetFreepsCard("fear");
		var bred = scn.GetFreepsCard("bred");
		var shadowGimli = scn.GetShadowCard("gimli");
		var shadowSavage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(account, armor, raiders, weary);
		scn.MoveCardsToDiscard(guard, axe, delving, troop, fear, bred);
		scn.MoveCardsToDiscard(shadowGimli, shadowSavage);

		scn.StartGame();

		assertInHand(account, armor, raiders, weary);
		assertInDiscard(guard, axe, delving, troop, fear, bred, shadowGimli, shadowSavage);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowDiscardCount());
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

		// The discard: any 2 of the other cards in hand, of either side and any type, and not the event being played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, raiders, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(account));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armor, raiders);
		assertInDiscard(armor, raiders);
		assertInHand(weary);

		// The Free Peoples card: a companion, a possession or an event of this player's pile; not the possession
		// just discarded (Q-0320), not the event being played (CR 4.2 "event"), not a Shadow card, and not the
		// other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, axe, delving));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, account, troop, shadowGimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(axe);

		// The Shadow card: a minion, a condition or an event; not the minion just discarded, not a Free Peoples
		// card, and not the other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, fear, bred));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(raiders, guard, shadowSavage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(bred);

		assertInHand(weary, axe, bred);
		assertInDiscard(guard, delving, troop, fear, armor, raiders, account, shadowGimli, shadowSavage);
		// 4 in hand - the event - 2 discarded + 2 taken.
		assertEquals(3, scn.GetFreepsHandCount());
		// 6 in the pile - 2 taken + 2 discarded + the event.
		assertEquals(7, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyGandalfWizardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var armor = scn.GetFreepsCard("armor");
		var raiders = scn.GetFreepsCard("raiders");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(account, armor, raiders);
		scn.MoveCardsToDiscard(guard, troop);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. The Ent of his culture could.
		scn.AddWoundsToChar(gandalf, 3);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInHand(account, armor, raiders);
		assertInDiscard(guard, troop);
		assertFalse(scn.FreepsPlayAvailable(account));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneOtherCardInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");

		scn.MoveCompanionsToTable(gandalf);
		// One card beside the event, and a card being played cannot be discarded to pay for itself.
		scn.MoveCardsToHand(account, armor);
		scn.MoveCardsToDiscard(guard, troop);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(account, armor);
		assertInDiscard(guard, troop);
		assertFalse(scn.FreepsPlayAvailable(account));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCardToTakeOnlyAShadowCardIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var weary = scn.GetFreepsCard("weary");
		var troop = scn.GetFreepsCard("troop");
		var shadowGimli = scn.GetShadowCard("gimli");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(account, armor, weary);
		// The one Free Peoples card in a discard pile is the other player's.
		scn.MoveCardsToDiscard(troop);
		scn.MoveCardsToDiscard(shadowGimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(account, armor, weary);
		assertInDiscard(troop, shadowGimli);
		assertEquals(1, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(account);

		// One Wizard and two other cards in hand: the costs are paid without a choice. The Armor just discarded is
		// not taken back, and the Troop is the one Shadow card to take.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInHand(troop);
		assertInDiscard(armor, weary, account, shadowGimli);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowCardToTakeOnlyAFreePeoplesCardIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var account = scn.GetFreepsCard("account");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armor = scn.GetFreepsCard("armor");
		var raiders = scn.GetFreepsCard("raiders");
		var guard = scn.GetFreepsCard("guard");
		var shadowSavage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(account, armor, raiders);
		// The one Shadow card in a discard pile is the other player's.
		scn.MoveCardsToDiscard(guard);
		scn.MoveCardsToDiscard(shadowSavage);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(account, armor, raiders);
		assertInDiscard(guard, shadowSavage);
		assertEquals(1, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(account);

		// The minion just discarded is not taken back, and the Dwarf Guard is the one Free Peoples card to take.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInHand(guard);
		assertInDiscard(armor, raiders, account, shadowSavage);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
