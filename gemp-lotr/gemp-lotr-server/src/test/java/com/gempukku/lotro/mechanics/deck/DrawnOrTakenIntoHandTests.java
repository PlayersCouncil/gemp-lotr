package com.gempukku.lotro.mechanics.deck;

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
 * A card drawn and a card taken into hand are two things (F-0376).
 *
 * The rules name them apart (Comprehensive Rules 4.2, "The Rule of 4": "You cannot draw (or take into hand) more
 * than 4 cards"), and so do the cards: Morgul on the March answers "Each time the Free Peoples player draws a
 * card", while Verily I Come and Betrayal of Isengard answer "draws a card (or takes a card into hand)".  A card
 * is drawn when a text says "draw"; a card taken into hand from a discard pile, or from a draw deck by a look, a
 * reveal or a search, is taken and not drawn.
 *
 * In a definition the trigger `playerDrawsCard` answers a draw alone, and with `orTakesIntoHand: true` a card
 * taken into hand as well.  Each test below gives the same cards to the same hand in three ways (taken from the
 * discard pile, taken from the draw deck, drawn) and counts what the text under it added.
 *
 * The one take that IS a draw is a card taken in place of a card about to be drawn (Q-0205: B, on Race Text
 * 94_28: "that card *did* come from the top of your draw deck"): the last test, and Card_15_022_Tests for The
 * Mirror of Galadriel, whose definition marks its take `inPlaceOfDraw: true`.  Each card drawn being answered
 * once is DrawingCardsTests; the Rule of 4 counting both is RuleOfFourTests.
 */
public class DrawnOrTakenIntoHandTests
{
	protected VirtualTableScenario GetFreePeoplesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Morgul on the March: [wraith] support-area condition.  "Each time the Free Peoples player
					// draws a card (except during the regroup phase), add a threat."  To play, spot a Nazgûl:
					// it is cheated in.
					put("march", "7_195");
					// Verily I Come: [sauron] condition on the Ring-bearer.  "Each time the Free Peoples player
					// draws a card (or takes a card into hand) during the fellowship phase, add a burden."
					put("verily", "2_94");

					// Barliman Butterbur: ally.  "Fellowship: Exert Barliman Butterbur to take a [gandalf] event
					// into hand from your discard pile."
					put("barliman", "1_70");
					put("wizard", "1_78");      // Mysterious Wizard: a [gandalf] skirmish event, for the discard pile
					// Master Proudfoot: ally.  "Fellowship: If the twilight pool has fewer than 3 twilight tokens,
					// add (2) to reveal the top 3 cards of your draw deck. Take all [shire] cards revealed into
					// hand and discard the rest."
					put("proudfoot", "1_301");
					put("sword", "1_299");      // Hobbit Sword: the [shire] card among the three
					put("axe1", "1_3");         // Axe Strike: [dwarven], twice, the other two
					put("axe2", "1_3");
					// Elrond, Lord of Rivendell: ally.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("blow", "1_5");         // Cleaving Blow: the card drawn

					// Two companions beside the Ring-bearer: room for three threats
					put("aragorn", "1_89");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Betrayal of Isengard, as printed in set 3 and in set 12: [gandalf] support-area condition,
					// not unique.  "Each time an opponent draws a card (or takes a card into hand) during the
					// Shadow phase, you may remove (1)."
					put("betrayal3", "3_29");
					put("betrayal12", "12_25");

					// The Underdeeps of Moria: [moria] support-area condition.  "Shadow: Remove (3) to reveal the
					// bottom card of your draw deck. If it is a [moria] Orc, take it into hand. Otherwise,
					// discard it."
					put("underdeeps", "1_200");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, for the bottom of the draw deck
					// Seeking Its Master: [sauron] support-area condition.  "Shadow: Exert a [sauron] Orc and
					// remove (2) to draw a card."
					put("seeking", "1_276");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc of vitality 2, to exert
					put("troop", "1_177");      // Goblin Patrol Troop: the card drawn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInPlaceOfADrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "7_195");      // Morgul on the March: as above
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: "Fellowship: Exert Elrond to draw a card."
					put("sword", "1_299");      // Hobbit Sword: the top card of the draw deck
					put("blow", "1_5");         // Cleaving Blow: the card under it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				// Race Text 94_28, the Free Peoples player's: "Each time you are about to draw a card (except
				// when reconciling), instead your opponent looks at the 2 top cards from your draw deck, chooses
				// 1 for you to take into hand, and the other is placed beneath your draw deck."
				"94_28", null
		);
	}

	@Test
	public void ATextThatAnswersACardDrawnDoesNotAnswerACardTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreePeoplesScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var sword = scn.GetFreepsCard("sword");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var elrond = scn.GetFreepsCard("elrond");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveAlliesToTable(barliman, proudfoot, elrond);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToDiscard(wizard);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));

		scn.StartGame();
		// The draw deck from the top: Hobbit Sword, Axe Strike, Axe Strike, Cleaving Blow
		scn.MoveCardsToTopOfDeck(blow, axe2, axe1, sword);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// A card taken into hand from the discard pile: in hand, and not drawn
		scn.FreepsUseCardAction(barliman);
		assertInZone(Zone.HAND, wizard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// A card taken into hand from the draw deck: the top card of the deck, in hand, and not drawn
		scn.FreepsUseCardAction(proudfoot);
		scn.DismissRevealedCards();
		assertInZone(Zone.HAND, sword);
		assertInZone(Zone.DISCARD, axe1, axe2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// A card drawn, on the same table: one threat, where three companions leave room for three
		scn.FreepsUseCardAction(elrond);
		assertInZone(Zone.HAND, blow);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ATextThatSaysOrTakesACardIntoHandAnswersBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreePeoplesScenario();

		var frodo = scn.GetRingBearer();
		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var sword = scn.GetFreepsCard("sword");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var elrond = scn.GetFreepsCard("elrond");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveAlliesToTable(barliman, proudfoot, elrond);
		scn.MoveCardsToDiscard(wizard);
		scn.AttachCardsTo(frodo, scn.GetShadowCard("verily"));

		scn.StartGame();
		// The draw deck from the top: Hobbit Sword, Axe Strike, Axe Strike, Cleaving Blow
		scn.MoveCardsToTopOfDeck(blow, axe2, axe1, sword);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// A card taken into hand from the discard pile
		scn.FreepsUseCardAction(barliman);
		assertInZone(Zone.HAND, wizard);
		assertEquals(1, scn.GetBurdens());

		// A card taken into hand from the draw deck: one card taken, one burden, and none for the two
		// revealed with it and discarded
		scn.FreepsUseCardAction(proudfoot);
		scn.DismissRevealedCards();
		assertInZone(Zone.HAND, sword);
		assertInZone(Zone.DISCARD, axe1, axe2);
		assertEquals(2, scn.GetBurdens());

		// A card drawn
		scn.FreepsUseCardAction(elrond);
		assertInZone(Zone.HAND, blow);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// The same words about an opponent, in the Shadow phase: each printing of Betrayal of Isengard offers its
	// "you may remove (1)" for the card the Shadow player takes into hand and for the card he draws.
	@Test
	public void ATextThatSaysOrTakesACardIntoHandAnswersBothForAnOpponentInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var betrayal3 = scn.GetFreepsCard("betrayal3");
		var betrayal12 = scn.GetFreepsCard("betrayal12");
		var underdeeps = scn.GetShadowCard("underdeeps");
		var runner = scn.GetShadowCard("runner");
		var seeking = scn.GetShadowCard("seeking");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(betrayal3, betrayal12, underdeeps, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToBottomOfDeck(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertSame(runner, scn.GetShadowBottomOfDeck());

		// A card taken into hand from the draw deck: (3) removed for it, and each Betrayal offers
		scn.ShadowUseCardAction(underdeeps);
		scn.DismissRevealedCards();
		assertInZone(Zone.HAND, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal3));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal12));
		scn.FreepsChooseOptionalTrigger(betrayal3);
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(betrayal3));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal12));
		scn.FreepsChooseOptionalTrigger(betrayal12);
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// A card drawn: (2) removed for it, and each offers again
		scn.ShadowUseCardAction(seeking);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.HAND, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal3));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal12));
		scn.FreepsChooseOptionalTrigger(betrayal12);
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(betrayal3);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// "About to draw a card ... instead ... take [one] into hand": the card taken is the card drawn.
	@Test
	public void ACardTakenInPlaceOfACardAboutToBeDrawnIsACardDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInPlaceOfADrawScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));

		scn.StartGame();
		// The draw deck from the top: Hobbit Sword, Cleaving Blow
		scn.MoveCardsToTopOfDeck(blow, sword);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// The draw is replaced: the opponent is shown the top two and nothing has been drawn
		scn.FreepsUseCardAction(elrond);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, blow));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// The second card is given, and the card that was about to be drawn goes beneath the deck: one card
		// in hand, taken in place of the draw, and Morgul on the March answers it once
		scn.ShadowChooseCardBPFromSelection(blow);
		assertInZone(Zone.HAND, blow);
		assertSame(sword, scn.GetFreepsBottomOfDeck());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
