package com.gempukku.lotro.mechanics.deck;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A forced discard from a hand that a bar protects ("Shadow cards may not discard cards from your hand", Frodo,
 * Courteous Halfling 4_301 and Frodo, Little Master 19_28): where the player must choose the cards to discard, he
 * is not asked to, since none of them can be discarded, and none is (F-0514; F-0116, a choice with nothing to act on
 * is not asked).  The definition word `discardFromHand` with `forced: true` reads the bar as its choice comes up,
 * as the random discard and the discard from the top of a draw deck already did.  A discard the player chooses to
 * make (`forced: false`, "may discard") is never barred (CR 4.2, Frodo, Courteous Halfling: "If a Shadow card
 * gives you a choice, you may choose to discard").
 *
 * The Dark Lord Advances 3_87 is the Shadow card: "Exert a [sauron] minion to reveal a card at random from the Free
 * Peoples player's hand.  That player must discard X cards from hand, where X is the twilight cost of the card
 * revealed."  Both cards in his hand cost 1, so X is 1 and he has a card to choose.
 *
 * A "you may" whose yes is such a discard is not asked under the bar either, since yes would do nothing (F-0515;
 * F-0379): the definition says so with the requirement `canDiscardFromHand` on its `optional`, which is the question
 * the discard asks.  Alive and Unspoiled 1_120 is the Shadow card: "Shadow: Remove (3) and spot X burdens to make the
 * Free Peoples player reveal X cards at random from hand.  You may discard 1 revealed card."  It is the one Shadow
 * card with a forced discard from the Free Peoples hand inside an `optional`; an optional trigger whose whole effect
 * is one (Orc Inquisitor 1_268) is withheld under the bar by the rule of Q-0224, with no requirement.
 */
public class ABarOnDiscardingFromHandTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advances", "3_87");
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, to exert; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion; a skirmish ability only
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion and no archer
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion, and so not one of the three
					put("guard", "1_7");        // Dwarf Guard: twilight cost 1, a card in the Free Peoples hand
					put("bow", "1_41");         // Elven Bow: twilight cost 1, a card in the Free Peoples hand
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAliveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alive", "1_120");      // Alive and Unspoiled
					put("gimli", "1_13");
					put("arwen", "7_16");
					put("boromir", "1_97");
					put("sam", "1_311");
					put("guard", "1_7");
					put("bow", "1_41");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AForcedDiscardFromABarredHandAsksForNoCardAndDiscardsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var advances = scn.GetShadowCard("advances");
		var soldier = scn.GetShadowCard("soldier");
		// Three unbound companions: Frodo's bar is in force.
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToHand(guard, bow, advances);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertInHand(guard, bow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.GetWoundsOn(soldier));
		// The card revealed costs 1 whichever it is.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// He must discard 1 card, and none can be discarded: he is not asked to choose one.
		assertFalse(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertInHand(guard, bow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInDiscard(advances);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutTheBarTheSameDiscardAsksForACardAndDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var advances = scn.GetShadowCard("advances");
		var soldier = scn.GetShadowCard("soldier");
		// Two unbound companions, one short, beside a Ring-bound one: the bar is not in force.
		scn.MoveCompanionsToTable(gimli, arwen, sam);
		scn.MoveCardsToHand(guard, bow, advances);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertInHand(guard, bow);
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, bow));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bow);
		assertInDiscard(bow, advances);
		assertInHand(guard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AYouMayWhoseYesIsADiscardFromABarredHandIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAliveScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var alive = scn.GetShadowCard("alive");
		// Three unbound companions: Frodo's bar is in force.
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToHand(guard, bow);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(alive));
		scn.ShadowUseCardAction(alive);
		assertTrue(scn.ShadowDecisionAvailable("Choose how many burdens to spot"));
		scn.ShadowChoose("1");
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Yes would discard nothing: the Shadow player is not asked, and the phase goes on.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard one of the revealed cards?"));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(guard, bow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
	}

	@Test
	public void WithoutTheBarTheYouMayIsAskedAndYesDiscardsTheRevealedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAliveScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var alive = scn.GetShadowCard("alive");
		// Two unbound companions, one short, beside a Ring-bound one: the bar is not in force.
		scn.MoveCompanionsToTable(gimli, arwen, sam);
		scn.MoveCardsToHand(guard, bow);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(alive);
		scn.ShadowChoose("1");
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard one of the revealed cards?"));
		scn.ShadowChooseYes();
		// The one card revealed is the one the choice can take, and it is discarded.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
