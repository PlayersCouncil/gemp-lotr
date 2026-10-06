package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_087_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advances", "53_87");

					// The minions on the table.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2, vitality 2
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, cost 2, vitality 2
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, vitality 3

					// The Shadow player's draw deck, by cost.
					put("sieger", "18_129");    // White Hand Sieger: [uruk-hai], the one minion that costs 0
					put("sieger2", "18_129");
					put("runner", "1_178");     // Goblin Runner: [moria], cost 1
					put("slayer", "3_97");      // Orc Slayer: [sauron], cost 1
					put("soldier2", "1_271");   // Orc Soldier: [sauron], cost 2
					put("savage2", "1_151");    // Uruk Savage: [isengard], cost 2
					put("assassin", "1_262");   // Orc Assassin: [sauron] tracker, cost 2
					put("assassin2", "1_262");
					put("hunters", "1_267");    // Orc Hunters: [sauron], cost 3
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard], cost 4
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession, cost 1

					// The Free Peoples player's hand, by cost.
					put("armor", "1_8");        // Dwarven Armor: 0
					put("bow", "1_41");         // Elven Bow: 1
					put("guard", "1_7");        // Dwarf Guard: 1
					put("gimli", "1_13");       // Gimli, Son of Gloin: 2
					put("legolas", "1_50");     // Legolas, Greenleaf: 2
					put("boromir", "1_97");     // Boromir, Son of Denethor: 3
					put("boromir2", "1_97");
					put("lord", "1_96");        // Boromir, Lord of Gondor: 3

					put("fearing", "2_100");    // Fearing the Worst: the twilight cost of each tracker is +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDarkLordAdvancesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Dark Lord Advances
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert a [sauron] minion to reveal a card at random from the Free Peoples player's hand.
		 * You may take a minion into hand from your draw deck costing up to X, where X is the twilight cost
		 * of the card revealed.  The Free Peoples player may discard X cards from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("advances");

		assertEquals("The Dark Lord Advances", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionToRevealACardFromTheFreePeoplesHandAndTakeAMinionOfThatCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");
		var soldier2 = scn.GetShadowCard("soldier2");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var shadowBow = scn.GetShadowCard("bow");
		var gimli = scn.GetFreepsCard("gimli");

		// The Shadow player holds a card of his own that costs 1; the card revealed is to be the other player's.
		scn.MoveCardsToHand(advances, shadowBow);
		// Gimli costs 2 and is the only card in the Free Peoples player's hand.
		scn.MoveCardsToHand(gimli);
		// A minion that is cheap enough, but in the discard pile.  Nothing that costs more than 2 is left in
		// the draw deck.
		scn.MoveCardsToDiscard(runner, scn.GetShadowCard("hunters"), scn.GetShadowCard("raiders"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowPlayAvailable(advances));

		scn.ShadowPlayCard(advances);
		assertEquals(0, scn.GetTwilight());

		// Either [sauron] minion may exert, an Orc or an Uruk-hai; the [isengard] Uruk-hai may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Shadow player is shown the one card of the other player's hand, and not his own.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		scn.DismissRevealedCards();
		assertInHand(gimli);

		// He looks through his draw deck.
		scn.ShadowDismissRevealedCards();

		// X is 2.  Minions of any culture that cost 2 or less; not a possession, not the minion in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(slayer, soldier2, savage2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar, runner));
		scn.ShadowChooseCardBPFromSelection(soldier2);

		// With one card in hand against a cost of 2, the Free Peoples player is not asked whether to prevent it.
		assertInHand(soldier2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, slayer, savage2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInDiscard(advances);
		// The deck that was looked through is shuffled, and the other is not.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(advances);
		scn.MoveCardsToHand(scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// The Shadow player's last actions before the regroup phase, when no skirmish is fought.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(advances));

		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(advances));
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(advances);
		scn.MoveCardsToHand(scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, savage);
		// Vitality 2 with 1 wound: the Orc Soldier cannot exert.  The Uruk Savage could, and is not [sauron].
		scn.AddWoundsToChar(soldier, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.ShadowPlayAvailable(advances));
	}

	@Test
	public void ARevealedCardCostingThreeIsPreventedByDiscardingThreeCardsOfFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var hunters = scn.GetShadowCard("hunters");
		var boromir = scn.GetFreepsCard("boromir");
		var boromir2 = scn.GetFreepsCard("boromir2");
		var lord = scn.GetFreepsCard("lord");
		var freepsHunters = scn.GetFreepsCard("hunters");

		scn.MoveCardsToHand(advances);
		// Four cards that each cost 3, one of them a Shadow card: whichever is revealed, X is 3.
		scn.MoveCardsToHand(boromir, boromir2, lord, freepsHunters);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.GetWoundsOn(sentry));
		// One card of the four is revealed.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// Orc Hunters cost 3.
		scn.ShadowChooseCardBPFromSelection(hunters);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Exactly 3 of the 4, of the Free Peoples player's choosing.
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(boromir, lord, freepsHunters);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInHand(boromir2);
		assertInDiscard(boromir, lord, freepsHunters);

		// The minion stays in the draw deck.  The exertion and the event are spent all the same.
		assertInZone(Zone.DECK, hunters);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(advances);
		// The deck was looked through before the minion was refused.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARevealedCardCostingOneIsPreventedByDiscardingOneCardOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var slayer = scn.GetShadowCard("slayer");
		var bow = scn.GetFreepsCard("bow");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCardsToHand(advances);
		// Two cards that each cost 1.
		scn.MoveCardsToHand(bow, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// Orc Slayer costs 1.
		scn.ShadowChooseCardBPFromSelection(slayer);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInHand(bow);
		assertInDiscard(guard);
		assertInZone(Zone.DECK, slayer);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithExactlyEnoughCardsTheFreePeoplesPlayerMayDeclineToPreventAndTheMinionIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var slayer = scn.GetShadowCard("slayer");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveCardsToHand(advances);
		// Two cards that each cost 2: just enough to prevent.
		scn.MoveCardsToHand(gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// Orc Slayer costs 1.
		scn.ShadowChooseCardBPFromSelection(slayer);

		assertInZone(Zone.DECK, slayer);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInHand(slayer);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(gimli, legolas);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARevealedCardCostingNothingIsPreventedForNoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var sieger = scn.GetShadowCard("sieger");
		var sieger2 = scn.GetShadowCard("sieger2");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToHand(advances);
		// Dwarven Armor costs 0.
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// White Hand Sieger costs 0, and there are two in the deck.
		scn.ShadowChooseCardBPFromSelection(sieger);

		// X is 0, and X may be 0: the Free Peoples player may prevent this by discarding no cards.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		// The game still asks which cards, and there are none to choose.
		assertEquals(0, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(armor);
		assertInZone(Zone.DECK, sieger, sieger2);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void XIsThePrintedCostOfTheRevealedCardWhateverACardInPlayMakesItCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var assassin = scn.GetShadowCard("assassin");
		var fearing = scn.GetFreepsCard("fearing");
		var freepsAssassin = scn.GetFreepsCard("assassin");
		var freepsAssassin2 = scn.GetFreepsCard("assassin2");

		scn.MoveCardsToHand(advances);
		// Two copies of Orc Assassin, a tracker that costs 2, are the Free Peoples player's whole hand.
		scn.MoveCardsToHand(freepsAssassin, freepsAssassin2);
		scn.MoveCardsToSupportArea(fearing);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 printed + 2 for Fearing the Worst, were either copy to be played.
		assertEquals(4, scn.GetTwilightCost(freepsAssassin));
		assertEquals(4, scn.GetTwilightCost(assassin));

		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		// The Shadow player's own Orc Assassin is printed at 2 and so costs no more than X.
		scn.ShadowChooseCardBPFromSelection(assassin);

		// X is 2: two cards are enough to prevent, where a cost of 4 would have asked for more than the hand holds.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInDiscard(freepsAssassin, freepsAssassin2);
		assertInZone(Zone.DECK, assassin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtARevealedCostOfOneOnlyMinionsCostingOneOrLessAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var sieger = scn.GetShadowCard("sieger");
		var runner = scn.GetShadowCard("runner");
		var slayer = scn.GetShadowCard("slayer");
		var soldier2 = scn.GetShadowCard("soldier2");
		var savage2 = scn.GetShadowCard("savage2");
		var hunters = scn.GetShadowCard("hunters");
		var raiders = scn.GetShadowCard("raiders");
		var bow = scn.GetFreepsCard("bow");

		scn.MoveCardsToHand(advances);
		// Elven Bow costs 1.
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// X is 1: the minions that cost 0 and 1, of three cultures, and none that costs 2, 3 or 4.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sieger, runner, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier2, savage2, hunters, raiders));
		scn.ShadowChooseCardBPFromSelection(runner);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInHand(runner);
		// One minion and no more.
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, sieger, slayer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtARevealedCostOfThreeAMinionCostingThreeIsOfferedAndOneCostingFourIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var slayer = scn.GetShadowCard("slayer");
		var soldier2 = scn.GetShadowCard("soldier2");
		var hunters = scn.GetShadowCard("hunters");
		var raiders = scn.GetShadowCard("raiders");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(advances);
		// Boromir costs 3, and one card in hand cannot pay 3 to prevent.
		scn.MoveCardsToHand(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// X is 3: Orc Hunters at exactly 3 and the cheaper minions, and not the Uruk-hai Raiding Party at 4.
		assertTrue(scn.ShadowHasCardChoiceAvailable(slayer, soldier2, hunters));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		scn.ShadowChooseCardBPFromSelection(hunters);

		assertInHand(hunters);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToTakeAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveCardsToHand(advances);
		// Two cards that each cost 2: the Free Peoples player could pay to prevent, were there anything to prevent.
		scn.MoveCardsToHand(gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(advances);
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// Nothing is taken and nothing is asked of the Free Peoples player.
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The deck was looked through all the same.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRevealedCardIsShownToTheShadowPlayerAndNotToTheOwnerOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(advances);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(advances);

		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowDismissRevealedCards();
		// What he is shown next is his own draw deck.
		assertTrue(scn.ShadowGetTotalRevealedCardCount() > 1);
	}
}
