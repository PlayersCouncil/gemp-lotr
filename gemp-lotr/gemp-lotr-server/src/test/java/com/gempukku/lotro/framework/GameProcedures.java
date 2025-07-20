package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Assert;

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


	default void SkipToAssignments() {
		SkipToPhase(Phase.ASSIGNMENT);
		PassCurrentPhaseActions();
	}

	default void FreepsResolveSkirmish(PhysicalCardImpl comp) { FreepsChooseCard(comp); }

	default void SkipToMovementDecision() {
		SkipToPhase(Phase.REGROUP);
		PassCurrentPhaseActions();
		if(ShadowDecisionAvailable("reconcile")) {
			ShadowDeclineReconciliation();
		}
		while(ShadowDecisionAvailable("discard down")) {
			ShadowChooseCard((PhysicalCardImpl) GetShadowHand().getFirst());
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


	default void FreepsChooseToMove() { PlayerDecided(P1, "0"); }
	default void FreepsChooseToStay() { PlayerDecided(P1, "1"); }

	//These two are only used in situations where the play is inverted, such as when skipping to a site.
	default void ShadowChooseToMove() { PlayerDecided(P2, "0"); }
	default void ShadowChooseToStay() { PlayerDecided(P2, "1"); }

	default void FreepsDeclineReconciliation() { FreepsPassCurrentPhaseAction(); }
	default void ShadowDeclineReconciliation() { ShadowPassCurrentPhaseAction(); }

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

	default void FreepsPassCurrentPhaseAction() {
		if(userFeedback().getAwaitingDecision(P1) != null) {
			PlayerDecided(P1, "");
		}
	}

	default void ShadowPassCurrentPhaseAction() {
		if(userFeedback().getAwaitingDecision(P2) != null) {
			PlayerDecided(P2, "");
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
		for(int attempts = 1; attempts <= 20; attempts++)
		{
			Phase current = gameState().getCurrentPhase();
			if(current == target)
				break;

			if(current == Phase.FELLOWSHIP) {
				FreepsPassCurrentPhaseAction();
				if(game().getFormat().getSiteBlock() == SitesBlock.SHADOWS) {
					ShadowChooseAnyCard();
				}
			}
			else if(current == Phase.SHADOW) {
				ShadowPassCurrentPhaseAction();
			}
			else {
				var freeps = FreepsGetAwaitingDecision();
				var shadow = ShadowGetAwaitingDecision();
				if(freeps != null && freeps.getText().toLowerCase().contains("required")) {
					FreepsChooseAction("0");
				}
				else if(shadow != null && shadow.getText().toLowerCase().contains("required")){
					ShadowChooseAction("0");
				}
				else {
					PassCurrentPhaseActions();
				}
			}

			if(attempts == 20)
			{
				throw new RuntimeException("Could not arrive at target '" + target + "' after 20 attempts!");
			}
		}
    }

	default void SkipToPhaseInverted(Phase target) {
		for(int attempts = 1; attempts <= 20; attempts++)
		{
			Phase current = gameState().getCurrentPhase();
			if(current == target)
				break;

			if(current == Phase.FELLOWSHIP) {
				ShadowPassCurrentPhaseAction();
			}
			else if(current == Phase.SHADOW) {
				FreepsPassCurrentPhaseAction();
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
		if(ShadowDecisionAvailable("reconcile"))
		{
			ShadowDeclineReconciliation();
		}
		while(ShadowDecisionAvailable("discard down"))
		{
			ShadowChooseCard((PhysicalCardImpl) GetShadowHand().getFirst());
		}
		if(FreepsDecisionAvailable("another move"))
		{
			FreepsChooseToStay();
		}
		if(FreepsDecisionAvailable("reconcile"))
		{
			FreepsDeclineReconciliation();
		}
		if(FreepsDecisionAvailable("discard down"))
		{
			FreepsChooseCard((PhysicalCardImpl) GetFreepsHand().getFirst());
		}

		//Shadow player
		SkipToPhaseInverted(Phase.REGROUP);
		ShadowPassCurrentPhaseAction(); // actually freeps with the swap
		FreepsPassCurrentPhaseAction(); // actually shadow with the swap
		if(FreepsDecisionAvailable("reconcile"))
		{
			FreepsDeclineReconciliation();
		}
		if(FreepsDecisionAvailable("discard down"))
		{
			FreepsChooseCard((PhysicalCardImpl) GetFreepsHand().getFirst());
		}
		if(ShadowDecisionAvailable("another move"))
		{
			ShadowChoose("1"); // Choose to stay
		}
		if(ShadowDecisionAvailable("reconcile"))
		{
			ShadowDeclineReconciliation();
		}
		if(ShadowDecisionAvailable("discard down"))
		{
			ShadowChooseCard((PhysicalCardImpl) GetShadowHand().getFirst());
		}

		Assert.assertTrue(GetCurrentPhase() == Phase.BETWEEN_TURNS
				|| GetCurrentPhase() == Phase.FELLOWSHIP);
	}




	default void FreepsDismissRevealedCards() { FreepsPassCurrentPhaseAction(); }
	default void ShadowDismissRevealedCards() { ShadowPassCurrentPhaseAction(); }
	default void DismissRevealedCards() {
		FreepsDismissRevealedCards();
		ShadowDismissRevealedCards();
	}

}
