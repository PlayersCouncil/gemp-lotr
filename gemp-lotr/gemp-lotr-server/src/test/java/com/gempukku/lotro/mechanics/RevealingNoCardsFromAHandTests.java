package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A reveal from a hand that has no card to show: nobody is shown a list of no cards to dismiss, and the game log
 * says that there was nothing to reveal.
 *
 * Ketura's ruling on Q-0144 (2026-10-08): "Both of these cards should behave the same....not sure which one is
 * better UX.  Let's go ahead and leave out the pop-up and just go to the log, but ensure that the log has some sort
 * of message to the tune of "there were no cards in hand to reveal" or something so the user isn't confused."
 *
 * Three definition words reveal from a hand, and each is here against an empty hand and against a hand of one
 * card, where the showing is as it always was (the other player is shown the card, the owner of the hand is
 * shown nothing: Q-0024, Q-0036):
 * `revealHand` (the whole hand), `revealRandomCardsFromHand` (cards at random) and `revealCardsFromHand` (cards
 * its owner chooses, or all of them by its `select`).
 *
 * A chosen reveal that finds no card of the kind asked for, in a hand that holds cards or in its owner's empty
 * hand, is no longer shown here.  The one printed card that could come to it was Mallorn-trees 1_54, whose
 * ability is not offered with no [elven] card in hand (F-0420; Ketura on Q-0034), and no card that exists reaches
 * it now (Ketura, 2026-10-11: a test shows what a card that exists does; Q-0260).  Card_01_054_Tests has the offer.
 *
 * `revealCardsFromHand` given ANOTHER player's hand is no longer shown here either.  The one card written so was
 * Storm of Argument 3_6 ("Reveal a card at random from an opponent's hand"), and two tests stood on it: the card
 * was shown to the player who does not own the hand, and an empty hand was named in the game log as its owner's
 * (F-0274).  Written that way the event went past No Business of Ours 2_44 (F-0391), and it is now written as
 * its three siblings are, `revealRandomCardsFromHand` (Friends of Old 3_16 above; His First Serious Check 3_33,
 * Might of Númenor 3_43).  Ketura, 2026-10-11: "Seems pretty clear that it should be rewritten to use
 * revealRandomCardsFromHand.  If the other test can't then be tested, then it seems that edge case isn't
 * something we really need to worry about, if there are no other candidates in the entire card base."  There are
 * none: of the 13 other definitions that write `revealCardsFromHand`, twelve name no hand, which is the hand of
 * the player acting, and Porter Troll 11_135 names the Free Peoples player's inside a prevention that is the
 * Free Peoples player's own to make.  So the two tests are gone, and what is left of that word is the hand of
 * the player who reveals: one card of it chosen (Mallorn-trees), and the whole of it when it is empty (Slippery
 * as Fishes 9_29, laid when the two went so that the word is still shown against an empty hand: Q-0263).
 */
public class RevealingNoCardsFromAHandTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Treachery Deeper Than You Know: "Fellowship: Spot Gandalf to reveal an opponent's hand."
					put("treachery", "1_86");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only a strength bonus of his own
					// Troll's Keyward: "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each
					// [orc] Troll revealed."  Vitality 3.
					put("keyward", "12_105");
					// Friends of Old: "Fellowship: Reveal a card at random from an opponent's hand.  Heal X
					// [elven] allies, where X is the twilight cost of the card revealed."
					put("friends", "3_16");
					// Mallorn-trees: "Fellowship: Reveal an [elven] card from hand and place it beneath your draw
					// deck."
					put("mallorn", "1_54");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] card
					// Slippery as Fishes: "Regroup: Spot Gollum to reveal your hand.  Place all Shadow cards
					// revealed beneath your draw deck.  Discard this condition."  Its reveal is written
					// revealCardsFromHand, select all(any).
					put("slippery", "9_29");
					// Gollum, Nasty Treacherous Creature: strength 5, vitality 4.  "Regroup: Discard Gollum to exert
					// a companion or ally", used only on purpose.
					put("gollum", "5_24");
					put("runner", "1_178");     // Goblin Runner: a card for the Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWholeOfAnEmptyHandIsShownToNobodyAndTheLogSaysThereWasNothingToReveal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());

		scn.MarkGameLog();
		scn.FreepsPlayCard(treachery);
		// Nobody has anything to dismiss: the event is over and the phase goes on.
		assertFalse(scn.FreepsDecisionAvailable("Hand of"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, treachery);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("to reveal"));
	}

	@Test
	public void AHandOfOneCardIsShownToTheOtherPlayerAsBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.MarkGameLog();
		scn.FreepsPlayCard(treachery);
		assertTrue(scn.FreepsDecisionAvailable("Hand of"));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards"));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DISCARD, treachery);
	}

	@Test
	public void APlayersOwnEmptyHandIsShownToNobodyEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");

		scn.StartGame();
		scn.MoveMinionsToTable(keyward);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetShadowHandCount());
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(keyward));

		scn.MarkGameLog();
		scn.ShadowUseCardAction(keyward);
		// The Keyward exerts and reveals a hand of no cards: the Free Peoples player is shown nothing.
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
	}

	@Test
	public void CardsAtRandomFromAnEmptyHandAreShownToNobodyAndTheLogSaysThereWasNothingToReveal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		scn.MoveCardsToHand(friends);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());

		scn.MarkGameLog();
		scn.FreepsPlayCard(friends);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, friends);
		// The same words as for the whole hand.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("to reveal"));
	}

	@Test
	public void ACardAtRandomFromAHandOfOneIsShownToTheOtherPlayerAsBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(friends);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		scn.MarkGameLog();
		scn.FreepsPlayCard(friends);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards"));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheWholeOfAPlayersOwnEmptyHandRevealedByTheWordForChosenCardsIsShownToNobodyEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(slippery);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(slippery));

		scn.MarkGameLog();
		scn.ShadowUseCardAction(slippery);
		// A hand of no cards is revealed: the Free Peoples player is shown nothing, and the rest of the ability
		// is carried out.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsDecisionAvailable("revealed card(s) in hand"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, slippery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// The same words as for the other two.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("to reveal"));
	}

	@Test
	public void AChosenCardFromAPlayersOwnHandIsShownToTheOtherPlayerAsBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(arwen);

		scn.StartGame();

		scn.MarkGameLog();
		scn.FreepsUseCardAction(mallorn);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, false));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(arwen));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards"));

		scn.ShadowDismissRevealedCards();
		assertInZone(Zone.DECK, arwen);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
