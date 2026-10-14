package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

/**
 * These functions are for progressing through the game itself.  For instance, if your test is really about battle
 * attrition and the phases before that point are just in the way, here you will find functions to skip past phases
 * so that your test can remain mostly clean of irrelevant procedure.
 *
 * Do be warned that these functions assume a best-case scenario that can be safely passed through; if a card has a
 * required decision that does not have an obvious "decline" option, then these functions will fail and you will have
 * to do it manually.  If you actually do need such a pestering card on the table, it is advised that you only place
 * it at the last possible second rather than putting it down early and requiring you to do all the manual procedure.
 */
public interface GameProcedures extends Actions, GameProperties, PileProperties {

	default void SkipToArcheryWounds() {
		SkipToPhase(Phase.ARCHERY);
		PassCurrentPhaseActions();
	}


	/**
	 * Plays on into the assignment phase ({@link #SkipToPhase}) and passes its actions for both players, so that it
	 * rests at the Free Peoples player's "Assign minions" decision, nothing assigned.  It answers no optional
	 * trigger of its own: where something is offered as the maneuver phase starts, the passes fall out of step.  A
	 * Shadow follower's Aid (Saruman 17_38 in the support area or borne by a minion) is such an offer: the call then
	 * rests at the Shadow player's "Play Assignment action or Pass", and an assignment after it finds no "Assign
	 * minions" to answer.  There, skip to the maneuver phase, decline the Aid ({@code ShadowDeclineOptionalTrigger()})
	 * and then call this.
	 */
	default void SkipToAssignments() {
		SkipToPhase(Phase.ASSIGNMENT);
		PassCurrentPhaseActions();
	}

	default void FreepsResolveSkirmish(PhysicalCardImpl comp) { FreepsChooseCard(comp); }
	default void ShadowResolveSkirmish(PhysicalCardImpl comp) { ShadowChooseCard(comp); }

	/**
	 * Stops at the regroup "do you want to make another move?" decision of whichever player's turn is
	 * under way or, where the mover is not asked that, at his reconcile or in the other player's fellowship phase.
	 * On player 2's turn the roles are swapped, so the inverted primitives are used; on
	 * player 1's turn the behaviour is unchanged.
	 *
	 * The question is asked only of a mover who may choose to move again.  Where he may not (the fellowship is
	 * at the move limit, a card says it cannot move, no companion of his is unhindered) the game goes straight
	 * on to his reconcile, and this does not stop where its name says: with cards in hand the mover is left at
	 * his own "Reconcile" decision, still in the regroup phase; with an empty hand nothing is asked of him, the
	 * turn ends, and this returns in the other player's fellowship phase.  Read which it was before answering.
	 */
	default void SkipToMovementDecision() {
		if(GetCurrentPlayer().equals(P1())) {
			SkipToPhase(Phase.REGROUP);
			PassCurrentPhaseActions();
			PassReconcile(P2());
		}
		else {
			SkipToPhaseInverted(Phase.REGROUP);
			//Not BothPassInverted(): that passes player 1 first, but the Free Peoples player of the
			//turn - player 2 here - is the one who acts first in the regroup phase.
			PassCurrentPhaseActions();
			PassReconcile(P1());
		}
	}

	/**
	 * Plays out the rest of the turn that is under way and stops at the start of the other player's
	 * fellowship phase; the moving fellowship declines the optional regroup move.  Calling this a
	 * second time therefore returns to player 1's fellowship phase.
	 */
	default void SkipToOtherPlayersTurn() {
		boolean playerOnesTurn = GetCurrentPlayer().equals(P1());
		SkipToMovementDecision();

		if(playerOnesTurn) {
			if(FreepsDecisionAvailable("another move")) {
				FreepsChooseToStay();
			}
			PassReconcile(P1());
		}
		else {
			if(ShadowDecisionAvailable("another move")) {
				ShadowChooseToStay();
			}
			PassReconcile(P2());
		}
	}


	/**
	 * Causes both players to pass during the Maneuver phase.
	 */
	default void PassManeuverActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Archery phase.
	 */
	default void PassArcheryActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Assignment phase.
	 */
	default void PassAssignmentActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Fierce Assignment phase.
	 */
	default void PassFierceAssignmentActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during a Skirmish phase.
	 */
	default void PassSkirmishActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during a fierce Skirmish phase.
	 */
	default void PassFierceSkirmishActions() { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Regroup phase.
	 */
	default void PassRegroupActions() { PassCurrentPhaseActions(); }


	default void FreepsChooseToMove() { PlayerDecided(P1(), "0"); }
	default void FreepsChooseToStay() { PlayerDecided(P1(), "1"); }

	//These two are only used in situations where the play is inverted, such as when skipping to a site.
	default void ShadowChooseToMove() { PlayerDecided(P2(), "0"); }
	default void ShadowChooseToStay() { PlayerDecided(P2(), "1"); }

	default void FreepsDeclineReconciliation() { FreepsPass(); }
	default void ShadowDeclineReconciliation() { ShadowPass(); }

	/**
	 * Gets the given player through a reconcile, if that player is at one, keeping as much of the hand as the game
	 * allows.  With a hand within the hand size the offer to discard a card is declined (and the hand is then drawn
	 * up).  With a hand over it, "Choose cards to discard down to 8" wants every card to be discarded in one answer:
	 * the first cards of the hand are given, as many as the decision asks for.  Every helper that plays a turn out
	 * answers its reconciles with this.  Does nothing when the player is being asked something else, or nothing.
	 * @param player The player who may be reconciling.
	 */
	default void PassReconcile(String player) {
		if(DecisionAvailable(player, "reconcile")) {
			PlayerDecided(player, "");
		}
		if(DecisionAvailable(player, "discard down")) {
			int count = Integer.parseInt(GetAwaitingDecisionParam(player, "min")[0]);
			var hand = GetHand(player);
			PhysicalCardImpl[] cards = new PhysicalCardImpl[count];
			for(int i = 0; i < count; i++) {
				cards[i] = (PhysicalCardImpl) hand.get(i);
			}
			ChooseCards(player, cards);
		}
	}

	/**
	 * Causes both players to pass, first the player with a current decision and then the other.
	 */
	default void PassResponses() {
		var decider = GetDecidingPlayer();
		var offPlayer = GetNextDecider();

		PlayerPass(decider);

		if(AnyDecisionsAvailable(offPlayer)) {
			PlayerPass(offPlayer);
		}
	}

	/**
	 * Causes both players to pass any decisions that contain the provided text.  First the current decider will pass,
	 * and then the other.
	 * @param text Text which must be contained inside the decision
	 */
	default void PassResponses(String text) {
		var decider = GetDecidingPlayer();
		var offPlayer = GetNextDecider();

		if(DecisionAvailable(decider, text)) {
			PlayerPass(decider);

			if(DecisionAvailable(offPlayer, text)) {
				PlayerPass(offPlayer);
			}
		}
	}

	/**
	 * Causes both players to pass, first the player with a current decision and then the other. Both will check
	 * to ensure that they have a currently available decision to be passing first.
	 */
	default void PassCurrentPhaseActions() {
		var decider = GetDecidingPlayer();
		var offPlayer = GetNextDecider();

		PlayerPass(decider);

		if(AnyDecisionsAvailable(offPlayer)) {
			PlayerPass(offPlayer);
		}
	}


	/**
	 * Causes both players to pass. Both will check to ensure that they have a currently available decision to be passing first.
	 */
	default void BothPass() {
		var currentPlayer = GetCurrentPlayer();
		var offPlayer = GetOffPlayer();

		if(AnyDecisionsAvailable(currentPlayer)) {
			PlayerDecided(currentPlayer, "");
		}

		if(AnyDecisionsAvailable(offPlayer)) {
			PlayerDecided(offPlayer, "");
		}
	}

	/**
	 * Causes both players to pass any decisions that contain the provided text.
	 * @param text
	 */
	default void BothPass(String text) {
		var currentPlayer = GetCurrentPlayer();
		var offPlayer = GetOffPlayer();
		if(DecisionAvailable(currentPlayer, text)) {
			PlayerDecided(currentPlayer, "");
		}

		if(DecisionAvailable(offPlayer, text)) {
			PlayerDecided(offPlayer, "");
		}
	}

	/**
	 * Causes both players to pass, but makes the opponent pass before the current player. Both will check
	 * to ensure that they have a currently available decision to be passing first.
	 */
	default void BothPassInverted() {
		var currentPlayer = GetCurrentPlayer();
		var offPlayer = GetOffPlayer();

		if(AnyDecisionsAvailable(offPlayer)) {
			PlayerDecided(offPlayer, "");
		}

		if(AnyDecisionsAvailable(currentPlayer)) {
			PlayerDecided(currentPlayer, "");
		}
	}

	/**
	 * Causes both players to pass any decisions that contain the provided text.  First the off-player will pass, and
	 * then the current player.
	 * @param text
	 */
	default void BothPassInverted(String text) {
		var currentPlayer = GetCurrentPlayer();
		var offPlayer = GetOffPlayer();

		if(DecisionAvailable(offPlayer, text)) {
			PlayerDecided(offPlayer, "");
		}

		if(DecisionAvailable(currentPlayer, text)) {
			PlayerDecided(currentPlayer, "");
		}
	}



	/**
	 * Causes players to spam pass until the provided target phase is current.  This process attempts to choose the
	 * first option of any required triggers, but may be brittle if there are any reacts that interrupt the pass-fest.
	 * Only 20 rounds of passing will be attempted to avoid infinite loops.
	 * @param target The phase the tester actually wants to be in
	 */
    default void SkipToPhase(Phase target) {
		Phase current = gameState().getCurrentPhase();
		try {
			for (int attempts = 1; attempts <= 20; attempts++) {
				current = gameState().getCurrentPhase();
				if (current == target)
					break;

				if (current == Phase.FELLOWSHIP) {
					FreepsPass();
					if (game().getFormat().getSiteBlock() == SitesBlock.SHADOWS) {
						ShadowChooseAnyCard();
					}
				} else if (current == Phase.SHADOW) {
					ShadowPass();
				} else {
					var freeps = FreepsGetAwaitingDecision();
					var shadow = ShadowGetAwaitingDecision();
					if (freeps != null && freeps.getText().toLowerCase().contains("required")) {
						FreepsChooseAction("0");
					} else if (shadow != null && shadow.getText().toLowerCase().contains("required")) {
						ShadowChooseAction("0");
					} else {
						PassCurrentPhaseActions();
					}
				}

				if (attempts == 20) {
					if (current == Phase.ARCHERY)
						throw new RuntimeException(
								"You left an archer in the scenario; can't reach '" + target + "' after 20 attempts because of the archery assignment.");

					throw new RuntimeException("Could not arrive at target '" + target + "' after 20 attempts!");
				}
			}
		}
		catch(RuntimeException ex) {
			throw new RuntimeException("Attempting to skip to phase " + target + ", halted in phase " + current, ex);
		}
    }

	/**
	 * SkipToPhase for player 2's turn, when player 2 is the one moving a fellowship and player 1 is the Shadow
	 * player: both players pass until the given phase is current, player 2 passing the fellowship phase and
	 * player 1 the Shadow phase, and in every other phase whoever is being asked and then the other.  It is the
	 * plainer of the two: it takes no required response for either player and lays no site on a Shadows-block
	 * path, so a turn that asks for either is driven by hand.  Only 20 rounds of passing are attempted.
	 * @param target The phase of player 2's turn that the tester wants to be in.
	 */
	default void SkipToPhaseInverted(Phase target) {
		for(int attempts = 1; attempts <= 20; attempts++)
		{
			Phase current = gameState().getCurrentPhase();
			if(current == target)
				break;

			if(current == Phase.FELLOWSHIP) {
				ShadowPass();
			}
			else if(current == Phase.SHADOW) {
				FreepsPass();
			}
			else {
				PassCurrentPhaseActions();
			}

			if(attempts == 20)
			{
				throw new RuntimeException("Could not arrive at target '" + target + "' after 20 attempts!");
			}
		}
	}

	default void SkipToSite(int siteNum) {
		for(int i = GetCurrentSite().getSiteNumber(); i < siteNum; i = GetCurrentSite().getSiteNumber())
		{
			SkipCurrentSite();
		}
	}

	default void SkipCurrentSite() {
		SkipToPhase(Phase.REGROUP);
		PhysicalCardImpl site = GetCurrentSite();
		if(site.getSiteNumber() == 9)
			return; // Game finished
		PassCurrentPhaseActions();
		PassReconcile(P2());
		if(FreepsDecisionAvailable("another move"))
		{
			FreepsChooseToStay();
		}
		PassReconcile(P1());

		//Shadow player
		SkipToPhaseInverted(Phase.REGROUP);
		ShadowPass(); // actually freeps with the swap
		FreepsPass(); // actually shadow with the swap
		PassReconcile(P1());
		if(ShadowDecisionAvailable("another move"))
		{
			ShadowChoose("1"); // Choose to stay
		}
		PassReconcile(P2());

		Verify(GetCurrentPhase() == Phase.BETWEEN_TURNS
				|| GetCurrentPhase() == Phase.FELLOWSHIP,
				"SkipCurrentSite expected to end between turns or in the Fellowship phase, but ended in " + GetCurrentPhase());
	}




	/**
	 * Dismisses the cards the Free Peoples player is being shown.  It is an error to call this when that player
	 * is not being shown cards.
	 */
	default void FreepsDismissRevealedCards() { DismissRevealedCards(P1()); }

	/**
	 * Dismisses the cards the Shadow player is being shown.  It is an error to call this when that player is not
	 * being shown cards.
	 */
	default void ShadowDismissRevealedCards() { DismissRevealedCards(P2()); }

	default void DismissRevealedCards(String playerID) {
		Verify(BeingShownCards(playerID), playerID + " is not being shown any cards to dismiss.");
		PlayerDecided(playerID, "");
	}

	default boolean BeingShownCards(String playerID) {
		AwaitingDecision decision = userFeedback().getAwaitingDecision(playerID);
		return decision != null && decision.getDecisionType() == AwaitingDecisionType.ARBITRARY_CARDS;
	}

	/**
	 * Dismisses the cards being shown to whichever players are being shown some, the Free Peoples player first.
	 * It is an error to call this when neither player is being shown cards.
	 */
	default void DismissRevealedCards() {
		boolean freeps = BeingShownCards(P1());
		if(freeps)
			PlayerDecided(P1(), "");
		boolean shadow = BeingShownCards(P2());
		if(shadow)
			PlayerDecided(P2(), "");
		Verify(freeps || shadow, "Neither player is being shown any cards to dismiss.");
	}

}
