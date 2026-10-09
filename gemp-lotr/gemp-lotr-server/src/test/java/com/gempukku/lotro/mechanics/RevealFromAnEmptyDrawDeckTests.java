package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A reveal from a draw deck that a cost buys, with nothing in that deck: the action is offered, and carries the
 * no-effect flag (see NoEffectFlagTests for the flag itself).
 *
 * Ketura's ruling on Q-0163 (2026-10-09), of "<cost> to reveal the top N cards of a draw deck. <What is done with
 * the cards revealed.>" while that deck is empty: "b is fine", B being "as your rule for the no-effect flag has it
 * for an action with a cost that would do nothing: offered, and flagged."  It is the rule for an event of the shape
 * as well: an event is offered whatever it will do (Q-0034).
 *
 * The flag is "effect" where the reveal is the action's first effect and everything after it acts on the cards
 * revealed and on nothing else: taking them into hand, discarding them, putting them back, an "if it is a ...
 * card".  The deck is the one the reveal names, whoever uses the action.  A deck with fewer cards in it than are
 * to be revealed still shows what is there, and is not flagged.  Where what follows the reveal has anything else
 * in it the action is not flagged on this ground, since the game cannot tell that it will come to nothing.
 *
 * Not here: a reveal that the rest of an ability hangs on with no cost before it ("Skirmish: Reveal the top card
 * of your draw deck. ...") is not offered at all with an empty deck (Q-0148, Glorfindel 9_16).
 */
public class RevealFromAnEmptyDrawDeckTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Master Proudfoot, Distant Relative of Bilbo: a [shire] ally.  "Fellowship: If the twilight
					// pool has fewer than 3 twilight tokens, add (2) to reveal the top 3 cards of your draw deck.
					// Take all [shire] cards revealed into hand and discard the rest."
					put("proudfoot", "1_301");
					// Wealth of Moria: a [dwarven] Fellowship event, cost 2.  "Spot a Dwarf to reveal the top 3
					// cards of your draw deck.  Take all Free Peoples cards revealed into hand and discard the
					// rest."
					put("wealth", "1_28");
					put("wealth2", "1_28");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf
					// The Dark Lord's Summons: a [sauron] condition.  "Shadow: Remove (3) to reveal the top card
					// of your draw deck.  If it is a [sauron] card, take it into hand.  Otherwise, discard it and
					// one other card from hand."  To play, spot a [sauron] Orc: it is put in the support area.
					put("summons", "1_242");
					// Forewarned: an [elven] condition.  "Fellowship: Add (1) to look at the top card of your draw
					// deck.  You may discard this condition to discard that card."  To play, spot 2 Elves: it is
					// put in the support area.
					put("forewarned", "6_17");

					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("soldier", "1_271");    // Orc Soldier: a [sauron] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARevealACostBuysIsOfferedAndFlaggedOnlyWhileThatDrawDeckIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proudfoot = scn.GetFreepsCard("proudfoot");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(proudfoot);

		scn.StartGame();
		// One card is left in his draw deck: fewer than the 3 to be revealed.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(sword);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(proudfoot));
		assertEquals("false", scn.FreepsActionNoEffect(proudfoot));

		// The one card is shown to both players and, being a [shire] card, taken into hand.
		scn.FreepsUseCardAction(proudfoot);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, false));
		scn.DismissRevealedCards();
		assertInHand(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Now his deck is empty, and the other player's is not.  The cost can be paid, so the ability is offered;
		// it is marked.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.GetShadowDeckCount() > 0);
		assertTrue(scn.FreepsActionAvailable(proudfoot));
		assertEquals("effect", scn.FreepsActionNoEffect(proudfoot));

		// It may be used all the same: the twilight is added, nobody is shown anything and nothing is taken.
		int discarded = scn.GetFreepsDiscardCount();
		scn.FreepsUseCardAction(proudfoot);
		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(discarded, scn.GetFreepsDiscardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEventThatRevealsFromAnEmptyDrawDeckIsOfferedAndFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wealth = scn.GetFreepsCard("wealth");
		var wealth2 = scn.GetFreepsCard("wealth2");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(wealth, wealth2);

		scn.StartGame();
		// One card is left in his draw deck.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(sword);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(wealth));
		assertEquals("false", scn.FreepsActionNoEffect(wealth));
		assertEquals("false", scn.FreepsActionNoEffect(wealth2));

		scn.FreepsPlayCard(wealth);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, false));
		scn.DismissRevealedCards();
		assertInHand(sword);
		assertInDiscard(wealth);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With nothing left in the deck the second copy is still offered, and is marked.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsPlayAvailable(wealth2));
		assertEquals("effect", scn.FreepsActionNoEffect(wealth2));

		// Played all the same, it is paid for and spent, and nobody is shown anything.
		int discarded = scn.GetFreepsDiscardCount();
		scn.FreepsPlayCard(wealth2);
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(wealth2);
		assertEquals(discarded + 1, scn.GetFreepsDiscardCount());
		assertInHand(sword);
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDeckThatMustBeEmptyIsTheOneTheRevealNames() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var summons = scn.GetShadowCard("summons");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();
		// One card is left in the Shadow player's draw deck, a [sauron] card.  The Free Peoples player's deck is
		// left as it is.
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetShadowDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.MoveCardsToTopOfDeck(soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowActionAvailable(summons));
		assertEquals("false", scn.ShadowActionNoEffect(summons));

		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, false));
		scn.DismissRevealedCards();
		assertInHand(soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// His own deck is now empty and the Free Peoples player's is not: the ability is offered, and marked.
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertTrue(scn.ShadowActionAvailable(summons));
		assertEquals("effect", scn.ShadowActionNoEffect(summons));

		// Used all the same, it takes the twilight and nothing else happens: no card is revealed, so none is
		// discarded, and neither is "one other card from hand".
		scn.ShadowUseCardAction(summons);
		assertEquals(twilight - 6, scn.GetTwilight());
		assertInHand(soldier);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARevealFollowedByAnOfferWithAPriceOfItsOwnIsNotFlagged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forewarned = scn.GetFreepsCard("forewarned");
		scn.MoveCardsToSupportArea(forewarned);

		scn.StartGame();
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsRefreshDecision();

		// What follows the look is an offer the player pays for with the condition itself, which is something
		// other than dealing with the card revealed: the game does not judge the action to do nothing.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(forewarned));
		assertEquals("false", scn.FreepsActionNoEffect(forewarned));

		// And it need not: the offer is made, and taking it discards the condition.
		scn.FreepsUseCardAction(forewarned);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, forewarned);
		scn.FreepsChooseYes();
		assertInDiscard(forewarned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
