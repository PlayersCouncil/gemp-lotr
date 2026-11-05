package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_013_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counsel", "101_13");

					// Gandalf, Friend of the Shirefolk: companion, cost 4.  His text only changes his own strength.
					put("gandalf", "1_72");
					// Barliman Butterbur: a [gandalf] ally, cost 0.  A [gandalf] card that is not Gandalf, and an ally
					// that is not [elven].  His fellowship ability is never used here.
					put("barliman", "1_70");
					// Lorien Elf: an [elven] companion, so [elven] and not an ally.
					put("lorienelf", "1_53");

					// [elven] allies.  None of their texts does anything from the draw deck or the hand.
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: cost 4, home site 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: cost 2, home site 6
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: cost 2, home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEredNimraisScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counsel", "101_13");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("barliman", "1_70");    // Barliman Butterbur: an ally, cost 0
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] ally, cost 4
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] ally, cost 2
				}},
				new HashMap<>()
				{{
					// The rig's Towers path, with Ered Nimrais as site 4.  Sites 1 to 3 have nothing but abilities
					// that are never used here.
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					// Ered Nimrais: "The twilight cost of each companion and ally played at Ered Nimrais is +3."
					put("site4", "4_343");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CounselOfTheWiseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Counsel of the Wise
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf to reveal an [elven] ally from your draw deck.  You may add (X) to take that
		 * ally into hand, where X is that ally's twilight cost.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("counsel");

		assertEquals("Counsel of the Wise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		// A [gandalf] card in play that is not Gandalf.
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(counsel, gandalf);

		scn.StartGame();

		// There are [elven] allies in the draw deck to reveal; only Gandalf is missing.
		assertInZone(Zone.DECK, elrond, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counsel));

		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(counsel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealsAnElvenAllyFromYourDrawDeckAndTakesItIntoHandForItsTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);
		// An [elven] ally that is not in the draw deck.  The opponent's copy of Silinde is in the opponent's draw
		// deck, so Silinde stands for both "not your draw deck" cases.
		scn.MoveCardsToDiscard(silinde);

		scn.StartGame();

		assertInZone(Zone.DECK, elrond, orophin, lorienelf, barliman, scn.GetShadowCard("silinde"));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(counsel));

		scn.FreepsPlayCard(counsel);
		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();

		// Both [elven] allies in the draw deck, and nothing else: not the [elven] companion, not the [gandalf] ally,
		// not the [elven] ally in the discard pile and in the opponent's draw deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, barliman, silinde));
		scn.FreepsChooseCardBPFromSelection(orophin);

		// The ally that was chosen is revealed: both players are shown Orophin.  A card that is only being shown is
		// listed as not selectable.
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(orophin, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// The event itself cost nothing, and nothing is added or taken until the player agrees to it.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to pay"));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, orophin);

		scn.FreepsChooseYes();

		// Orophin costs 2.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.HAND, orophin);
		assertInZone(Zone.DECK, elrond);
		assertInZone(Zone.DISCARD, counsel);
		assertEquals(1, scn.GetFreepsHandCount());
		// A draw deck that has been looked through is reshuffled when the looking is done: once, and only the deck
		// that was looked through.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingAnAllyWhoCostsFourAddsFourToThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);

		scn.StartGame();
		// A pool that is not empty, to tell adding X from leaving X in the pool.
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.DECK, elrond, orophin);

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(elrond);
		// Elrond is revealed to both players.
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertEquals(3, scn.GetTwilight());
		scn.FreepsChooseYes();

		// 3 in the pool + 4 for Elrond
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, elrond);
		assertInZone(Zone.DECK, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningToAddTwilightLeavesTheRevealedAllyInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, elrond, orophin);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(elrond);
		// Elrond is revealed to both players.
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("Would you like to pay"));

		scn.FreepsChooseNo();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, elrond, orophin);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, counsel);
		assertEquals(1, scn.GetFreepsDiscardCount());
		// The ally stays in the draw deck, but the deck was looked through to find it, so it is reshuffled all the
		// same: once, and only the deck that was looked through.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealedAllyIsShownToTheOpponentBeforeAnythingIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);

		scn.StartGame();

		assertInZone(Zone.DECK, elrond, orophin);
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsPlayCard(counsel);
		// It is the ally that is revealed, not the draw deck: while the deck is looked through the opponent is
		// shown nothing.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(elrond);

		// A revealed card is shown to every player.  That happens whether or not the ally is then taken, so it is
		// read here before the player has been asked about the twilight.  A card that is only being shown is listed
		// as not selectable.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, elrond);
		assertTrue(scn.ShadowDecisionAvailable("Revealed card"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(orophin, false));
		// "Every player" includes the one who chose it.
		assertTrue(scn.FreepsDecisionAvailable("Revealed card"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(orophin, false));

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Only when both have seen it is the player asked about the twilight, with nothing yet paid or taken.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, elrond);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to pay"));
	}

	@Test
	public void CanBePlayedWithNoElvenAllyInTheDrawDeckAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);
		scn.MoveCardsToDiscard(elrond, orophin, silinde);

		scn.StartGame();

		// What is left in the draw deck is an [elven] companion and a [gandalf] ally.
		assertInZone(Zone.DECK, lorienelf, barliman);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();

		// Spotting Gandalf is all the event asks for; revealing an ally is what it then does, if it can.
		assertTrue(scn.FreepsPlayAvailable(counsel));
		scn.FreepsPlayCard(counsel);
		// The draw deck is still looked through.
		scn.FreepsDismissRevealedCards();

		// No ally was found, so nothing is revealed to anybody and nobody is asked about twilight.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, counsel);
		assertInZone(Zone.DECK, lorienelf, barliman);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		// The deck was looked through for an ally that was not there, and a deck that has been looked through is
		// reshuffled whether or not the card looked for was found: once, and only that deck.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyFoundInTheDrawDeckNeedNotBeRevealedAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel);
		// Elrond is the one [elven] ally left in the draw deck.
		scn.MoveCardsToDiscard(orophin, silinde);

		scn.StartGame();

		assertInZone(Zone.DECK, elrond, lorienelf, barliman);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();

		// Elrond is the only card that could be chosen, and he is still not revealed unasked: "There is no
		// penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, silinde, lorienelf, barliman));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		// No ally was found, so nothing is revealed to anybody and nobody is asked about twilight.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsDecisionAvailable("Would you like to pay"));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, elrond);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		// The event is spent all the same.
		assertInZone(Zone.DISCARD, counsel);
		// The deck was looked through, so it is reshuffled though nothing came out of it: once, and only that deck.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtEredNimraisXIsStillTheAllysPrintedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEredNimraisScenario();

		var counsel = scn.GetFreepsCard("counsel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(counsel, barliman);

		scn.StartGame();
		scn.SkipToSite(4);

		// The turns that were skipped drew the rest of the deck into hand.
		scn.MoveCardsToTopOfDeck(elrond, orophin);
		scn.FreepsRefreshDecision();

		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.HAND, counsel, barliman);
		assertEquals(0, scn.GetTwilight());

		// An ally played here costs 3 more: Barliman costs 0 + 3 for the site.
		scn.FreepsPlayCard(barliman);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(counsel);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(elrond);
		// Elrond is revealed to both players.
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseYes();

		// Elrond is taken into hand, not played, so the site does not touch his cost: 3 in the pool + 4, not + 7.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
