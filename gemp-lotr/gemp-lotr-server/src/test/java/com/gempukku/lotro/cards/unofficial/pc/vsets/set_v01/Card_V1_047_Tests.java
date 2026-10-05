package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("betrayed", "101_47");
					put("betrayed2", "101_47");

					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability that is never used
					put("soldier2", "1_271");
					put("slayer", "3_93");      // Morgul Slayer: another [sauron] Orc; a regroup ability that is never used
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItBetrayedIsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: It Betrayed Isildur
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 5 burdens and discard a [Sauron] Orc to choose one: make the move limit for this turn -1;
		 * or make the Free Peoples player choose to move again (if the move limit allows).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("betrayed");

		assertEquals("It Betrayed Isildur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsASauronOrcToMakeTheMoveLimitOneLessForThisTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var slayer = scn.GetShadowCard("slayer");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		// A [sauron] Orc in hand is not one that can be discarded from play.
		scn.MoveCardsToHand(betrayed, soldier2);

		scn.StartGame();
		scn.AddBurdens(5);
		scn.MoveMinionsToTable(soldier, slayer, guard, troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(betrayed));

		scn.ShadowPlayCard(betrayed);
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop, soldier2));

		scn.ShadowChooseCard(soldier);
		assertInZone(Zone.DISCARD, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, slayer, guard, troop);
		assertTrue(scn.ShadowChoiceAvailable("move limit -1"));
		assertTrue(scn.ShadowChoiceAvailable("move again"));

		scn.ShadowChooseOption("move limit -1");
		assertEquals(1, scn.GetMoveLimit());
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, betrayed);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The fellowship has moved once and may not move again: when the regroup phase ends the Free Peoples
		// player is asked nothing and the turn passes to the other player.
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		// The next turn has its 2 moves again.
		assertEquals(2, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void TheMoveLimitIsExactlyOneLessAndStaysSoInALaterRegroupPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(betrayed);
		// A move limit of 3, so that one less still leaves a second move: 2 by rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.AddBurdens(5);
		scn.MoveMinionsToTable(soldier, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(3, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());

		scn.ShadowPlayCard(betrayed);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseOption("move limit -1");
		// 3 - 1
		assertEquals(2, scn.GetMoveLimit());

		// One move made of 2: the Free Peoples player is asked as usual, and moves.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// A new regroup phase of the same turn.  The limit is still 2 and not 3, so there is no third move.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveLimit());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}

	@Test
	public void DiscardsASauronOrcToMakeTheFreePeoplesPlayerMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(betrayed);

		scn.StartGame();
		scn.AddBurdens(5);
		scn.MoveMinionsToTable(soldier, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowPlayAvailable(betrayed));

		scn.ShadowPlayCard(betrayed);
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, slayer));
		scn.ShadowChooseCard(slayer);
		assertInZone(Zone.DISCARD, slayer);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		scn.ShadowChooseOption("move again");
		assertEquals(2, scn.GetMoveLimit());
		assertInZone(Zone.DISCARD, betrayed);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// When the regroup phase ends the Free Peoples player is not asked: the fellowship moves.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(VirtualTableScenario.P1, scn.GetCurrentPlayer());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(betrayed);
		// A move limit of 3, so that after the move the event demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.AddBurdens(5);
		scn.MoveMinionsToTable(soldier, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(3, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());

		scn.ShadowPlayCard(betrayed);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseOption("move again");
		assertEquals(3, scn.GetMoveLimit());

		// The second move is made without the Free Peoples player being asked.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// A new regroup phase of the same turn.  The event named no duration, so what it did ended with the phase
		// it was played in: the move limit allows a third move, and the Free Peoples player is asked about it.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void CannotBePlayedWhileOnlyFourBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(betrayed);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(soldier, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, slayer);
		assertFalse(scn.ShadowPlayAvailable(betrayed));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSauronOrcInPlayToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var soldier2 = scn.GetShadowCard("soldier2");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(betrayed, soldier2);

		scn.StartGame();
		scn.AddBurdens(5);
		// A [sauron] minion that is not an Orc and an Orc that is not [sauron] in play, and a [sauron] Orc in hand.
		scn.MoveMinionsToTable(guard, troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(5, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, guard, troop);
		assertFalse(scn.ShadowPlayAvailable(betrayed));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheFellowshipIsNotMadeToMoveAgainOnceTheMoveLimitNoLongerAllowsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayed = scn.GetShadowCard("betrayed");
		var betrayed2 = scn.GetShadowCard("betrayed2");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(betrayed, betrayed2);

		scn.StartGame();
		scn.AddBurdens(5);
		scn.MoveMinionsToTable(soldier, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// The first copy: the Free Peoples player is to move again, which the move limit of 2 allows.
		scn.ShadowPlayCard(betrayed);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseOption("move again");
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());

		// The second copy takes the move limit down to the one move already made.  The Morgul Slayer is the
		// only [sauron] Orc left and is discarded without a choice.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(betrayed2));
		scn.ShadowPlayCard(betrayed2);
		assertInZone(Zone.DISCARD, slayer);
		scn.ShadowChooseOption("move limit -1");
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The move limit does not allow a second move: the fellowship stays at site 2 and the turn ends.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}
}
