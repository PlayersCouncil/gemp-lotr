package com.gempukku.lotro.mechanics.game;

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
 * Reconciling a hand, and when each player does it.
 *
 * Comprehensive Rules 4.2, "reconcile" (5.0 has the same three steps): "A player reconciles his hand as follows:
 * He may first discard one card from his hand.  If he then has less than eight cards in his hand, he must draw
 * cards until he has eight.  Otherwise (when he has more than eight cards in his hand), he must discard from his
 * hand until he has only eight."
 *
 * "Regroup phase": "When all players consecutively pass, each Shadow player must reconcile his hand.  Then, at the
 * end of the regroup phase, if you are the Free Peoples player, you must select one of the following two choices:
 * Moving your fellowship to the next site, subject to the move limit, and returning to the Shadow phase(s).  Or,
 * reconciling your hand.  Then the Shadow players discard all minions in play (and cards borne by them), and your
 * turn ends."
 *
 * A player with more than eight cards is asked once, for exactly the cards over eight: the one card he "may first
 * discard" and the cards he then must come to the same hand, since with nine he ends at eight either way.
 *
 * Comprehensive Rules 5.0, "Summary of Differences From Decipher Era": "The game ends at site 9 when the Shadow
 * player is about to reconcile, instead of at the start of the Regroup phase."  CR 4.2, "winning the game": "A
 * player wins the game when his fellowship is at site 9 and his Ring-bearer survives all skirmish phases.  The
 * game ends, and there is no regroup phase on the last turn."  Which of the two a format plays by is the format's
 * own (winAtEndOfRegroup in lotrFormats.hjson).
 *
 * What a reconcile restores (CR 5.0 "reconcile", "restore") is ReconcilingRestoresHinderedCardsTests; the
 * followers that go back to the support area between the Free Peoples player's reconcile and the discarding of
 * the minions are AidTests; a draw deck that runs out under any other draw is DrawingCardsTests.  A text that
 * answers a reconcile ("each time you reconcile", "if a player reconciles") is its card's own.
 *
 * The cards in the hands are Dwarf Guards, which do nothing there.
 */
public class ReconcilingTests
{
	protected VirtualTableScenario GetScenario(String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: "To play, spot a Dwarf."
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
					put("guard7", "1_7");
					put("guard8", "1_7");
					put("guard9", "1_7");
					put("guard10", "1_7");
					put("guard11", "1_7");
					put("guard12", "1_7");

					// Legolas, Greenleaf: an Elf
					put("legolas", "1_50");
					// Careful Study: [elven] Regroup event, cost 1.  "Spot an Elf and discard 3 cards from hand
					// to reconcile your hand."
					put("study", "7_19");

					// Goblin Runner: strength 5, vitality 1
					put("runner", "1_178");
					// Goblin Scimitar: a hand weapon a [moria] Orc bears
					put("scimitar", "1_180");
					// Teeth of Mordor: a Shadow condition in the support area; its abilities are not used
					put("teeth", "5_110");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.Multipath);
	}

	@Test
	public void APlayerWithFewerThanEightCardsMayDiscardOneAndThenDrawsUntilHeHasEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discarded = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SetShadowHand(discarded, scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"));
		scn.SetFreepsHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.SkipToPhase(Phase.REGROUP);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowPass();

		// The Shadow player, with five cards: "he may first discard one card from his hand."
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(5, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(discarded);

		// He does, has four, and draws four.
		assertInZone(Zone.DISCARD, discarded);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 4, scn.GetShadowDeckCount());

		// The Free Peoples player, with three, chooses to reconcile, discards none and draws five.
		assertEquals(3, scn.GetFreepsHandCount());
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsDeclineReconciliation();

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 5, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void APlayerWithExactlyEightCardsMayDiscardOneAndDrawOneOrLeaveHisHandAsItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var discarded = scn.GetFreepsCard("guard1");

		scn.StartGame();
		scn.SetShadowHand(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"), scn.GetShadowCard("guard6"),
				scn.GetShadowCard("guard7"), scn.GetShadowCard("guard8"));
		scn.SetFreepsHand(discarded, scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"), scn.GetFreepsCard("guard6"),
				scn.GetFreepsCard("guard7"), scn.GetFreepsCard("guard8"));
		scn.SkipToPhase(Phase.REGROUP);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowPass();

		// With eight he is still offered the one discard, and declining it nothing is drawn.
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(8, scn.ShadowGetCardChoiceCount());
		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// Taking it, he "then has less than eight" and draws the one card back.
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(discarded);
		assertInZone(Zone.DISCARD, discarded);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void APlayerWithMoreThanEightCardsDiscardsUntilHeHasEightAndDrawsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowFirst = scn.GetShadowCard("guard1");
		var shadowSecond = scn.GetShadowCard("guard2");
		var freepsOnly = scn.GetFreepsCard("guard1");

		scn.StartGame();
		scn.SetShadowHand(shadowFirst, shadowSecond, scn.GetShadowCard("guard3"), scn.GetShadowCard("guard4"),
				scn.GetShadowCard("guard5"), scn.GetShadowCard("guard6"), scn.GetShadowCard("guard7"),
				scn.GetShadowCard("guard8"), scn.GetShadowCard("guard9"), scn.GetShadowCard("guard10"));
		scn.SetFreepsHand(freepsOnly, scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"), scn.GetFreepsCard("guard6"),
				scn.GetFreepsCard("guard7"), scn.GetFreepsCard("guard8"), scn.GetFreepsCard("guard9"));
		scn.SkipToPhase(Phase.REGROUP);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowPass();

		// Ten cards: two must go, no fewer and no more.
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to discard down to 8"));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(10, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(shadowFirst, shadowSecond);
		assertInZone(Zone.DISCARD, shadowFirst, shadowSecond);
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// Nine cards: one must go.
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to discard down to 8"));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(9, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(freepsOnly);
		assertInZone(Zone.DISCARD, freepsOnly);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void APlayerWithNoCardsInHandIsAskedNothingAndDrawsEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());

		// The Shadow player has reconciled, with nothing asked, by the time the Free Peoples player chooses.
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 8, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsChooseToStay();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 8, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ADrawDeckWithFewerCardsThanAreWantedGivesTheCardsItHas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SetFreepsHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		// Three cards are left in the draw deck: the rest of it is in the discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"),
				scn.GetFreepsCard("guard6"), scn.GetFreepsCard("guard7"), scn.GetFreepsCard("guard8"),
				scn.GetFreepsCard("guard9"), scn.GetFreepsCard("legolas"), scn.GetFreepsCard("study"),
				scn.GetFreepsCard("runner"), scn.GetFreepsCard("scimitar"), scn.GetFreepsCard("teeth"));
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(12, scn.GetFreepsDiscardCount());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();

		// Six are wanted and three are there: he has five, and the game goes on.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(12, scn.GetFreepsDiscardCount());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheShadowPlayerReconcilesEachRegroupPhaseAndTheFreePeoplesPlayerOnlyWhenHeDoesNotMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var discarded = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SetShadowHand(discarded, scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"));
		scn.SetFreepsHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// "When all players consecutively pass, each Shadow player must reconcile his hand": before the Free
		// Peoples player is asked whether he moves.
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		assertEquals(5, scn.GetFreepsHandCount());

		// He moves: he does not reconcile, the minion stays, and the game is back in a Shadow phase.
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup phase at the new site: the Shadow player reconciles again.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		scn.ShadowChooseCard(discarded);
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		// The move limit is reached: the Free Peoples player is not asked whether he moves, and reconciles.
		assertFalse(scn.FreepsDecisionAvailable("Do you want to make another move?"));
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(5, scn.GetFreepsHandCount());
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WhenTheFreePeoplesPlayerHasReconciledTheMinionsAndTheCardsTheyBearAreDiscardedAndTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var teeth = scn.GetShadowCard("teeth");

		scn.StartGame();
		scn.SetFreepsHand(scn.GetFreepsCard("guard1"));
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.MoveCardsToSupportArea(teeth);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsChooseToStay();

		// While the Free Peoples player is still reconciling, the minion is in play.
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertAttachedTo(scimitar, runner);
		scn.FreepsDeclineReconciliation();

		// "Then the Shadow players discard all minions in play (and cards borne by them), and your turn ends."
		// A Shadow card that is no minion, and is borne by none, stays.
		assertInZone(Zone.DISCARD, runner, scimitar);
		assertInZone(Zone.SUPPORT, teeth);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AReconcileThatACardBringsAboutIsCarriedOutTheSameWayInTheMiddleOfThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var study = scn.GetFreepsCard("study");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SetFreepsHand(study, guard1, guard2, guard3, guard4, guard5);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		int freepsDeck = scn.GetFreepsDeckCount();

		// "Discard 3 cards from hand to reconcile your hand": two are left.
		scn.FreepsPlayCard(study);
		scn.FreepsChooseCards(guard1, guard2, guard3);
		assertInZone(Zone.DISCARD, guard1, guard2, guard3);

		// The reconcile's own discard is offered, of one card ...
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(guard4);
		assertInZone(Zone.DISCARD, guard4);

		// ... and he draws from one card to eight.  The regroup phase goes on.
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 7, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, guard5);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The reconcile the phase ends with is carried out as well.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(8, scn.FreepsGetCardChoiceCount());
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void InAPlayersCouncilFormatTheGameAtSiteNineIsWonWhenTheShadowPlayerIsAboutToReconcile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario("pc_fotr_block");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		// The regroup phase at site 9 is played ...
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// ... and when both have passed the game is over, with the Shadow player's hand as it was.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasWon());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void InADecipherFormatThereIsNoRegroupPhaseAtSiteNineAndNobodyReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(VirtualTableScenario.Fellowship);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion is played: the Ring-bearer has survived every skirmish phase there will be.
		scn.ShadowPass();
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.FreepsHasWon());
		assertTrue(scn.GameIsFinished());
	}
}
