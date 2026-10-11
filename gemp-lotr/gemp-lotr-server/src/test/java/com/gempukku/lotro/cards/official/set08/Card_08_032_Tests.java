package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("catapult", "8_32");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, twilight cost 2; "Knight." is its whole text
					put("kog", "5_36");         // Knight of Gondor: [gondor] Man, twilight cost 2, a knight; a fellowship ability that needs a [gondor] condition
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] knight who is a Hobbit; a skirmish ability only
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man who is no knight; a regroup ability only
					put("guard1", "1_7");       // Dwarf Guards: cards to discard from hand, twilight cost 1
					put("guard2", "1_7");
					put("guard3", "1_7");

					// Shadow cards by twilight cost, of three types and three cultures at cost 2.
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, cost 2; "Damage +1." is its whole text
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area, cost 2; a Shadow and a Regroup ability
					put("arrow", "4_216");      // Arrow From the South: [raider] condition in the support area, cost 2; it answers a skirmish lost to a [raider] Man
					put("runner", "1_178");     // Goblin Runner: [moria] minion, cost 1; its text is about being played
					put("fighter", "1_146");    // Uruk Fighter: [isengard] minion, cost 3; a maneuver ability that needs 5 companions
					put("marksman", "1_176");   // Goblin Marksman: cost 3; only ever a card in a draw deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table small enough that every card of the Shadow player's draw deck can be put somewhere else.
	protected VirtualTableScenario GetEmptyShadowDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("catapult", "8_32");
					put("guard1", "1_7");       // Dwarf Guards: cards to discard from hand
					put("guard2", "1_7");
					put("runner", "1_178");     // Goblin Runner: a minion, twilight cost 1
					put("raiders", "4_242");    // Raiders From the East: a [raider] condition in the support area, twilight cost 0; its abilities need (3) or a token
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CatapultStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Catapult
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [gondor] knights.<br><b>Maneuver:</b> Discard 2 cards from hand to reveal the top card of an opponent's draw deck. Choose an opponent who must discard a Shadow card that has a twilight cost that is the same as the twilight cost of the revealed card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("catapult");

		assertEquals("Catapult", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondGondorKnightIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(catapult, kog);

		scn.StartGame();

		// One knight, and a [gondor] Man who is none.
		assertFalse(scn.FreepsPlayAvailable(catapult));

		scn.FreepsPlayCard(kog);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(catapult));

		scn.FreepsPlayCard(catapult);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, catapult);
		assertNull(catapult.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AKnightWhoIsNoManIsSpottedAsTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(pippin, ranger);
		scn.MoveCardsToHand(catapult, knight);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(catapult));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(catapult));

		scn.FreepsPlayCard(catapult);
		assertInZone(Zone.SUPPORT, catapult);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCardsAreDiscardedFromHandTheTopCardOfTheOpponentsDeckIsRevealedAndHeDiscardsAShadowCardOfItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var knight = scn.GetFreepsCard("knight");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var freepsFighter = scn.GetFreepsCard("fighter");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToHand(guard1, guard2, guard3);

		var savage = scn.GetShadowCard("savage");
		var ships = scn.GetShadowCard("ships");
		var arrow = scn.GetShadowCard("arrow");
		var runner = scn.GetShadowCard("runner");
		var fighter = scn.GetShadowCard("fighter");
		var shadowKnight = scn.GetShadowCard("knight");
		var shadowGuard = scn.GetShadowCard("guard1");
		scn.MoveCardsToSupportArea(ships, arrow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, fighter);
		// The Shadow player's deck: a Free Peoples card costing 2 on top, a card costing 1 under it.  The Free
		// Peoples player's own deck has a card costing 3 on top.
		scn.MoveCardsToTopOfDeck(shadowGuard, shadowKnight);
		scn.MoveCardsToTopOfDeck(freepsFighter);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);

		// Two cards of his choice from the three in hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, guard3));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard1, guard2);
		assertInDiscard(guard1, guard2);
		assertInHand(guard3);

		// Both players are shown the top card of the Shadow player's deck, and no other card.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowKnight, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowKnight, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Gondorian Knight costs 2.  The Shadow player chooses among his Shadow cards costing 2: a minion, a
		// possession and a condition.  Not the minions costing 1 and 3, and not the Free Peoples player's own
		// Gondorian Knight, who costs 2.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, ships, arrow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, fighter, knight, catapult));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(arrow);

		assertInDiscard(arrow);
		assertInZone(Zone.SUPPORT, ships);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner, fighter);
		// The card revealed is where it was.
		assertSame(shadowKnight, scn.GetShadowTopOfDeck());
		assertSame(freepsFighter, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheCostToMatchIsTheRevealedCardsAndWithNoShadowCardOfThatCostNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var knight = scn.GetFreepsCard("knight");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var kog = scn.GetFreepsCard("kog");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToHand(guard1, guard2, guard3, kog);

		var savage = scn.GetShadowCard("savage");
		var ships = scn.GetShadowCard("ships");
		var arrow = scn.GetShadowCard("arrow");
		var runner = scn.GetShadowCard("runner");
		var fighter = scn.GetShadowCard("fighter");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToSupportArea(ships, arrow);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, fighter);
		scn.MoveCardsToTopOfDeck(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(catapult);
		scn.FreepsChooseCards(guard1, guard2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksman, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Goblin Marksman, a Shadow card, costs 3: the one Shadow card in play costing 3 goes with no question.
		assertInDiscard(fighter);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertInZone(Zone.SUPPORT, ships, arrow);
		assertSame(marksman, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Again, with exactly 2 cards in hand: the Marksman is revealed once more, and nothing in play costs 3.
		scn.ShadowPass();
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);
		assertInDiscard(guard3, kog);
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertInZone(Zone.SUPPORT, ships, arrow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToHand(guard1);

		var savage = scn.GetShadowCard("savage");
		var shadowKnight = scn.GetShadowCard("knight");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Gondorian Knight costs what the Uruk Savage costs: there would be a card to discard.
		scn.MoveCardsToTopOfDeck(shadowKnight);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(catapult));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheOpponentsDrawDeckEmptyTheCardsAreDiscardedFromHandAndNoShadowCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEmptyShadowDeckScenario();

		var catapult = scn.GetFreepsCard("catapult");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToHand(guard1, guard2);

		var runner = scn.GetShadowCard("runner");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(raiders);
		scn.MoveCardsToDiscard(scn.GetShadowCard("catapult"), scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"));

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);

		// The cost is paid.  No card is revealed, so there is no cost to match: neither the condition costing 0
		// nor the minion is discarded, and nobody is shown anything.
		assertInDiscard(guard1, guard2);
		assertInZone(Zone.SUPPORT, raiders);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
