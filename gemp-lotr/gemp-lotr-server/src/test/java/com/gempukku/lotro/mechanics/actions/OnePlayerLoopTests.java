package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.loops.OnePlayerLoopTracker;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A loop of one player's actions is found, and put to him (CR 4.2 "loops"; Ketura, answers/2026-10-16-c.md).
 *
 * Comprehensive Rules 4.2, "loops", names Sam, Faithful Companion ("Fellowship: Play Bill the Pony from your draw
 * deck") with no Bill the Pony to play.  Ketura's word: "it should let the player do it three times and then prompt
 * with: "LOOP DETECTED: Choose a number of times to repeat this action."  It should then perform that many, and then
 * put the tag on that card action to block its future use."  Ketura's scenarios (rulings log, 2026-10-16): Sam with no
 * Bill in play or in the deck; The Bridge of Khazad-dûm with no Balrog in play or in the deck; two Hobbit Party
 * Guests healing each other.
 *
 * So a round, one action or two taken one after the other, goes three times unasked, the board the same at the
 * start of each round and after the third (the order of the draw deck left out: Sam and the Bridge look through it
 * and shuffle it); as the player would be offered it a fourth time, the prompt is asked of him instead, in Ketura's words,
 * from 0 to 99, filled in with 3.  The round is repeated that many times, unseen but for the last round, and then
 * each ability of the round is barred to the end of the phase: still offered, flagged "loopBanned", and refused.
 * Each repetition of Sam's or the Bridge's ability shuffles the deck once more, which is how the number carried out
 * is read.  The finite repeats of one player Ketura named are in FiniteRepeatsAreNoLoopTests.
 */
public class OnePlayerLoopTests
{
	// Sam, Faithful Companion 1_310, and three Lórien Elves 1_53 (no text but "To play, spot an Elf") in the deck.
	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_310");
					put("elf1", "1_53");
					put("elf2", "1_53");
					put("elf3", "1_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same, with Bill the Pony 3_106 in the deck: the first use plays him, and the loop begins after it.
	protected VirtualTableScenario GetSamWithBillScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_310");
					put("bill", "3_106");
					put("elf1", "1_53");
					put("elf2", "1_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Bridge of Khazad-dûm 1_349 is site 5 of the Fellowship path: "Shadow: Play The Balrog from your draw deck
	// or hand; The Balrog's twilight cost is -6."  No Balrog anywhere; Goblin Runners 1_178 in the Shadow deck.
	protected VirtualTableScenario GetBridgeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Hobbit Party Guest 1_297, ally, home 1F, vitality 2: "Fellowship: Exert this ally to heal another Hobbit ally
	// whose home is site 1."  Its start-of-turn heal is not reached in the phase.
	protected VirtualTableScenario GetGuestsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guest1", "1_297");
					put("guest2", "1_297");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamWithNoBillIsUsedThreeTimesUnaskedAndThePromptComesInPlaceOfTheFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		for (int use = 1; use <= 3; use++) {
			assertTrue(scn.AwaitingFellowshipPhaseActions());
			assertTrue(scn.FreepsActionAvailable(sam));
			assertFalse(scn.FreepsActionLoopBanned(sam));
			scn.FreepsUseCardAction(sam);
			scn.FreepsDismissRevealedCards();
			assertEquals(shuffles + use, scn.GetFreepsDeckShuffleCount());
			if (use < 3) {
				assertTrue(scn.AwaitingFellowshipPhaseActions());
				assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));
			}
		}

		// The fourth: the prompt, in Ketura's words, to him alone.
		assertFalse(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsDecisionAvailable(OnePlayerLoopTracker.PROMPT));
		assertEquals("LOOP DETECTED: Choose a number of times to repeat this action.", scn.FreepsGetAwaitingDecision().getText());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(99, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChoose("5");

		// Five more uses, each shuffling the deck, with nothing asked; then the ability is barred.
		assertEquals(shuffles + 8, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DECK, scn.GetFreepsCard("elf1"), scn.GetFreepsCard("elf2"), scn.GetFreepsCard("elf3"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.FreepsActionLoopBanned(sam));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void SamsBarLastsToTheEndOfTheFellowshipPhaseAndNotIntoTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		for (int use = 1; use <= 3; use++) {
			scn.FreepsUseCardAction(sam);
			scn.FreepsDismissRevealedCards();
		}
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// 0 repeats nothing and bars the ability.
		scn.FreepsChoose("0");
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(sam));

		// His next fellowship phase: offered, not barred, and the count begins again.
		scn.SkipToSite(2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sam));
		assertFalse(scn.FreepsActionLoopBanned(sam));
		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));
	}

	@Test
	public void SamWhoPlaysBillFirstGoesThreeTimesMoreOnTheSameBoardBeforeThePrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamWithBillScenario();

		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		// The first use plays Bill: the board is not as it was.
		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bill);
		scn.FreepsChooseCard(sam);
		assertAttachedTo(bill, sam);

		// Three uses that find nothing, each on the board the one before left.
		for (int use = 1; use <= 3; use++) {
			assertTrue(scn.AwaitingFellowshipPhaseActions());
			assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));
			scn.FreepsUseCardAction(sam);
			scn.FreepsDismissRevealedCards();
		}

		assertTrue(scn.FreepsDecisionAvailable(OnePlayerLoopTracker.PROMPT));
		scn.FreepsChoose("1");
		assertAttachedTo(bill, sam);
		assertTrue(scn.FreepsActionLoopBanned(sam));
	}

	@Test
	public void TheBridgeWithNoBalrogIsFoundForTheShadowPlayerAndBarredToTheEndOfHisShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBridgeScenario();

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToTopOfDeck(runner1, runner2, runner3);
		scn.FreepsPass();

		var bridge = scn.GetCurrentSite();
		assertEquals("The Bridge of Khazad-dûm", bridge.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int shuffles = scn.GetShadowDeckShuffleCount();

		for (int use = 1; use <= 3; use++) {
			assertTrue(scn.AwaitingShadowPhaseActions());
			assertFalse(scn.ShadowDecisionAvailable("LOOP DETECTED"));
			scn.ShadowUseCardAction(bridge);
			scn.ShadowDismissRevealedCards();
			assertEquals(shuffles + use, scn.GetShadowDeckShuffleCount());
		}

		assertFalse(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowDecisionAvailable(OnePlayerLoopTracker.PROMPT));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChoose("2");

		assertEquals(shuffles + 5, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.DECK, runner1, runner2, runner3);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(bridge));

		// His pass ends the phase as ever (with no minion in play the regroup phase is next).
		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void TwoHobbitPartyGuestsHealingEachOtherAreARoundOfTwoActionsAndBothAreBarred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGuestsScenario();

		var guest1 = scn.GetFreepsCard("guest1");
		var guest2 = scn.GetFreepsCard("guest2");
		scn.MoveAlliesToTable(guest1, guest2);

		scn.StartGame();
		scn.AddWoundsToChar(guest2, 1);

		for (int round = 1; round <= 3; round++) {
			assertTrue(scn.AwaitingFellowshipPhaseActions());
			assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));
			scn.FreepsUseCardAction(guest1);
			assertEquals(1, scn.GetWoundsOn(guest1));
			assertEquals(0, scn.GetWoundsOn(guest2));
			assertTrue(scn.AwaitingFellowshipPhaseActions());
			assertFalse(scn.FreepsDecisionAvailable("LOOP DETECTED"));
			scn.FreepsUseCardAction(guest2);
			assertEquals(0, scn.GetWoundsOn(guest1));
			assertEquals(1, scn.GetWoundsOn(guest2));
		}

		assertTrue(scn.FreepsDecisionAvailable(OnePlayerLoopTracker.PROMPT));
		scn.MarkGameLog();
		scn.FreepsChoose("3");

		// Three rounds, two of them unseen, the last shown; the board as it was.
		assertTrue(scn.GameLogSinceMarkSays("was carried out 2 times without being shown"));
		assertEquals(0, scn.GetWoundsOn(guest1));
		assertEquals(1, scn.GetWoundsOn(guest2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// The first Guest's heal is on offer, and barred; the second's, with nobody to heal, is not offered.
		assertTrue(scn.FreepsActionLoopBanned(guest1));
		assertFalse(scn.FreepsActionAvailable(guest2));
	}

	@Test
	public void BothGuestsAbilitiesAreBarredAndBagEndsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGuestsScenario();

		var guest1 = scn.GetFreepsCard("guest1");
		var guest2 = scn.GetFreepsCard("guest2");
		scn.MoveAlliesToTable(guest1, guest2);

		scn.StartGame();
		scn.AddWoundsToChar(guest2, 1);

		for (int round = 1; round <= 3; round++) {
			scn.FreepsUseCardAction(guest1);
			scn.FreepsUseCardAction(guest2);
		}
		scn.FreepsChoose("0");
		assertTrue(scn.FreepsActionLoopBanned(guest1));

		// The second Guest's wound taken off by a cheat, so that he can exert; then Bag End 1_319, "Fellowship: Exert
		// a Hobbit to play The Gaffer from your draw deck", which is no part of the loop, exerts the first Guest and
		// gives the second someone to heal.  His heal is offered, and barred too.
		scn.RemoveWoundsFromChar(guest2, 1);
		var bagend = scn.GetCurrentSite();
		assertFalse(scn.FreepsActionLoopBanned(bagend));
		scn.FreepsUseCardAction(bagend);
		scn.FreepsChooseCard(guest1);
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(guest1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(guest2));
		assertTrue(scn.FreepsActionLoopBanned(guest2));
	}
}
