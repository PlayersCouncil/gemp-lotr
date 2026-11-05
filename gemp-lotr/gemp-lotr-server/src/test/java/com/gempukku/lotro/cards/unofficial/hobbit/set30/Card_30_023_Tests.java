package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_023_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard", "30_23");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; a fellowship ability, a discard bar that
					// needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					// Gandalf, Leader of Dwarves: a second Gandalf, for a board with one in play; a reconcile trigger
					// and two skirmish abilities
					put("gandalf2", "30_25");
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally; a fellowship ability only
					put("thranduil", "31_12");  // Thranduil, Elven King: [elven] Elf ally; a maneuver ability only
					// Master Worrywort, Hobbit Farmer: [shire] Hobbit ally; a trigger on Bilbo's win and a skirmish ability
					put("worrywort", "33_53");
					put("dain", "32_2");        // Dáin Ironfoot: [dwarven] Dwarf ally; a regroup trigger only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only with 3 Ents
					put("card1", "1_177");      // Goblin Patrol Troop: a card with no text, to fill a draw deck
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AWizardIsNeverLateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: A Wizard Is Never Late
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Play Gandalf or an ally (except a Dwarf) from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wizard");

		assertEquals("A Wizard Is Never Late", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAnAllyFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		// Nothing in the discard pile: the draw deck is the one place to play from.
		scn.SetFreepsDrawDeck(gandalf, radagast, thranduil, dain, avenger, scn.GetFreepsCard("card1"));
		scn.MoveCardsToHand(wizard);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, gandalf, radagast, thranduil, dain, avenger);
		assertInZone(Zone.HAND, wizard);
		assertEquals(0, scn.GetFreepsDiscardCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(wizard));
		scn.FreepsPlayCard(wizard);
		scn.FreepsDismissRevealedCards();
		// Gandalf, and allies of two cultures and races; not a Dwarf ally, nor a companion who is no Gandalf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast, thranduil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, avenger));
		// A play from a draw deck is chosen from 0 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(thranduil);

		assertInZone(Zone.SUPPORT, thranduil);
		assertInZone(Zone.DECK, gandalf, radagast, dain, avenger);
		assertInZone(Zone.DISCARD, wizard);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysGandalfFromTheDiscardPileWithNoShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var worrywort = scn.GetFreepsCard("worrywort");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		var shadowRadagast = scn.GetShadowCard("radagast");

		scn.StartGame();
		scn.MoveCardsToDiscard(gandalf, worrywort, dain, avenger);
		// An ally in the other player's discard pile, of a title the player's own piles do not hold.
		scn.MoveCardsToDiscard(shadowRadagast);
		scn.MoveCardsToHand(wizard, radagast);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DISCARD, gandalf, worrywort, dain, avenger, shadowRadagast);
		assertInZone(Zone.HAND, wizard, radagast);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(wizard);
		// Thranduil is still in the draw deck, so either pile can be played from.
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard pile"));
		scn.FreepsChoose("discard pile");
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, worrywort));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, avenger, shadowRadagast));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DISCARD, worrywort, dain, avenger, shadowRadagast, wizard);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDrawDeckTheDiscardPileIsPlayedFromUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var worrywort = scn.GetFreepsCard("worrywort");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SetFreepsDrawDeck(dain, avenger, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(gandalf, worrywort);
		scn.MoveCardsToHand(wizard);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, dain, avenger);
		assertInZone(Zone.DISCARD, gandalf, worrywort);
		assertInZone(Zone.HAND, wizard);

		scn.FreepsPlayCard(wizard);
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, worrywort));
		scn.FreepsChooseCardBPFromSelection(worrywort);

		assertInZone(Zone.SUPPORT, worrywort);
		assertInZone(Zone.DECK, dain, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNothingToPlayInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		// A Dwarf ally in the draw deck and a companion who is no Gandalf in the discard pile.
		scn.SetFreepsDrawDeck(dain, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(avenger);
		scn.MoveCardsToHand(wizard);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, dain);
		assertInZone(Zone.DISCARD, avenger);
		assertInZone(Zone.HAND, wizard);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizard));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyGandalfToPlayIsBarredByAnotherInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var dain = scn.GetFreepsCard("dain");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf2);
		scn.SetFreepsDrawDeck(dain, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(gandalf);
		scn.MoveCardsToHand(wizard);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, gandalf2);
		assertInZone(Zone.DECK, dain);
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.HAND, wizard);

		// Gandalf is unique: a second cannot be played while one is in play.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizard));
	}
}
