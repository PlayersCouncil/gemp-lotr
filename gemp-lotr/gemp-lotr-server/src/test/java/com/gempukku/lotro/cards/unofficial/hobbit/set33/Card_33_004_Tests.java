package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_004_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The fellowship phase of
	// turn 1 is at Bag End (site 1), whose Fellowship action wants a [dwarven] follower in hand; the maneuver phase is at
	// The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knives", "33_4");
					put("knives2", "33_4");
					put("balin", "30_2");       // Balin, Brother of Dwalin: the one Balin; [dwarven] Dwarf companion, cost 3; a fellowship ability
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: another [dwarven] Dwarf companion, cost 3
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] hand weapon, a possession
					put("fury", "30_3");        // Battle Fury: a [dwarven] skirmish event
					put("takeup", "30_31");     // Take up arms. Fight!: a [gandalf] skirmish event
					put("noble", "30_14");      // Noble intentions: a [dwarven] skirmish event

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BluntTheKnivesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Blunt The Knives!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Choose one: draw 2 cards; play Balin from your draw deck; or place 2 cards from your discard pile beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knives");

		assertEquals("Blunt The Knives!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithEveryBranchPossibleAllThreeAreOfferedAndDrawTwoDrawsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(balin, dwalin, axe);
		scn.MoveCardsToDiscard(fury, takeup);
		scn.FreepsRefreshDecision();
		assertInHand(knives);
		assertInZone(Zone.DECK, balin, dwalin, axe);
		assertInDiscard(fury, takeup);
		assertEquals(1, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(knives));
		scn.FreepsPlayCard(knives);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(List.of("Draw 2 cards", "Play Balin from your draw deck", "Place 2 cards from discard beneath your deck"),
				scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Draw 2 cards");

		// The event has left the hand: two cards drawn, one left in the deck; the deck is not shuffled.
		assertInDiscard(knives);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(fury, takeup);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayBalinFromTheDrawDeckPlaysBalinAndNoOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(balin, dwalin, axe);
		scn.MoveCardsToDiscard(fury, takeup);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, balin, dwalin, axe);
		assertInDiscard(fury, takeup);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(knives);
		scn.FreepsChoose("Play Balin from your draw deck");
		scn.FreepsDismissRevealedCards();

		// Balin, and not Dwalin, another [dwarven] Dwarf companion. A play from a draw deck is 0 to 1 (Q-0019).
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwalin, axe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(balin);

		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.DECK, dwalin, axe);
		assertInDiscard(knives);
		// Shuffled as Balin leaves the deck that was looked through, and again once the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaceTwoCardsOfAnyKindFromTheDiscardPileBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");
		var noble = scn.GetFreepsCard("noble");

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(balin, takeup, noble);
		// A companion, a possession and an event.
		scn.MoveCardsToDiscard(dwalin, axe, fury);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, balin, takeup, noble);
		assertInDiscard(dwalin, axe, fury);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(knives);
		scn.FreepsChoose("Place 2 cards from discard beneath your deck");

		assertTrue(scn.FreepsHasCardChoiceAvailable(dwalin, axe, fury));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(dwalin, axe);
		// Which goes beneath first is asked; the other, unasked, is the bottom card.
		assertTrue(scn.FreepsDecisionAvailable("Choose card to put beneath draw deck"));
		scn.FreepsChooseCard(dwalin);

		assertInZone(Zone.DECK, dwalin, axe, balin, takeup, noble);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertInDiscard(fury, knives);
		// Beneath the cards already there.
		assertSame(axe, scn.GetFreepsBottomOfDeck());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBalinInPlayAndOneCardInTheDiscardPileTwoCardsAreDrawnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(dwalin, axe, takeup);
		scn.MoveCardsToDiscard(fury);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.DECK, dwalin, axe, takeup);
		assertInDiscard(fury);
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(knives);

		// Balin cannot be played while he is in play, and one card cannot be placed as two.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(fury, knives);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBalinTheOneCardInTheDeckAndOneCardInTheDiscardPileBalinIsPlayedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(balin);
		scn.MoveCardsToDiscard(fury);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, balin);
		assertInDiscard(fury);

		scn.FreepsPlayCard(knives);

		// One card cannot be drawn as two, nor placed as two.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(balin);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInDiscard(fury, knives);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBalinInPlayAndOneCardInTheDeckTwoCardsArePlacedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(takeup);
		scn.MoveCardsToDiscard(dwalin, axe, fury);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.DECK, takeup);
		assertInDiscard(dwalin, axe, fury);

		scn.FreepsPlayCard(knives);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChooseCards(axe, fury);
		scn.FreepsChooseCard(axe);
		assertInZone(Zone.DECK, takeup, axe, fury);
		assertInDiscard(dwalin, knives);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBranchPossibleInFullEachIsOfferedAndTheDrawTakesTheOneCardThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var fury = scn.GetFreepsCard("fury");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(takeup);
		scn.MoveCardsToDiscard(fury);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, takeup);
		assertInDiscard(fury);

		scn.FreepsPlayCard(knives);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChoose("Draw 2 cards");

		assertInHand(takeup);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInDiscard(fury, knives);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItIsNoManeuverEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knives = scn.GetFreepsCard("knives");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");

		scn.StartGame();
		scn.MoveCardsToHand(knives);
		scn.SetFreepsDrawDeck(balin, dwalin);
		scn.MoveMinionsToTable("runner");
		assertInHand(knives);
		assertInZone(Zone.DECK, balin, dwalin);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(knives));
		assertInHand(knives);
	}
}
