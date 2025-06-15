package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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

	default void SkipToArcheryWounds() throws DecisionResultInvalidException {
		SkipToPhase(Phase.ARCHERY);
		PassCurrentPhaseActions();
	}


	default void SkipToAssignments() throws DecisionResultInvalidException {
		SkipToPhase(Phase.ASSIGNMENT);
		PassCurrentPhaseActions();
	}

	default void FreepsResolveSkirmish(PhysicalCardImpl comp) throws DecisionResultInvalidException { FreepsChooseCard(comp); }

	default void SkipToMovementDecision() throws DecisionResultInvalidException {
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
	 * @throws DecisionResultInvalidException
	 */
	default void PassManeuverActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Archery phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassArcheryActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Assignment phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassAssignmentActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Fierce Assignment phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassFierceAssignmentActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during a Skirmish phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassSkirmishActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during a fierce Skirmish phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassFierceSkirmishActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }
	/**
	 * Causes both players to pass during the Regroup phase.
	 * @throws DecisionResultInvalidException
	 */
	default void PassRegroupActions() throws DecisionResultInvalidException { PassCurrentPhaseActions(); }


	default void FreepsChooseToMove() throws DecisionResultInvalidException { PlayerDecided(P1, "0"); }
	default void FreepsChooseToStay() throws DecisionResultInvalidException { PlayerDecided(P1, "1"); }

	//These two are only used in situations where the play is inverted, such as when skipping to a site.
	default void ShadowChooseToMove() throws DecisionResultInvalidException { PlayerDecided(P2, "0"); }
	default void ShadowChooseToStay() throws DecisionResultInvalidException { PlayerDecided(P2, "1"); }

	default void FreepsDeclineReconciliation() throws DecisionResultInvalidException { FreepsPassCurrentPhaseAction(); }
	default void ShadowDeclineReconciliation() throws DecisionResultInvalidException { ShadowPassCurrentPhaseAction(); }



	/**
	 * Causes both players to pass, first by making the current player pass and then their opponent. Both will check
	 * to ensure that they have a currently available decision to be passing first.
	 * @throws DecisionResultInvalidException
	 */
	default void PassCurrentPhaseActions() throws DecisionResultInvalidException {
		FreepsPassCurrentPhaseAction();
		ShadowPassCurrentPhaseAction();
	}

	default void FreepsPassCurrentPhaseAction() throws DecisionResultInvalidException {
		if(userFeedback().getAwaitingDecision(P1) != null) {
			PlayerDecided(P1, "");
		}
	}

	default void ShadowPassCurrentPhaseAction() throws DecisionResultInvalidException {
		if(userFeedback().getAwaitingDecision(P2) != null) {
			PlayerDecided(P2, "");
		}
	}

	/**
	 * Causes both players to pass any decisions that contain the provided text.
	 * @param text
	 * @throws DecisionResultInvalidException
	 */
	default void BothPass(String text) throws DecisionResultInvalidException {
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
	 * @throws DecisionResultInvalidException
	 */
	default void BothPassInverted() throws DecisionResultInvalidException {
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
	 * @throws DecisionResultInvalidException
	 */
	default void BothPassInverted(String text) throws DecisionResultInvalidException {
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
	 * @throws DecisionResultInvalidException
	 */
    default void SkipToPhase(Phase target) throws DecisionResultInvalidException {
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
				throw new DecisionResultInvalidException("Could not arrive at target '" + target + "' after 20 attempts!");
			}
		}
    }

	default void SkipToPhaseInverted(Phase target) throws DecisionResultInvalidException {
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
				throw new DecisionResultInvalidException("Could not arrive at target '" + target + "' after 20 attempts!");
			}
		}
	}

	default void SkipToSite(int siteNum) throws DecisionResultInvalidException {
		for(int i = GetCurrentSite().getSiteNumber(); i < siteNum; i = GetCurrentSite().getSiteNumber())
		{
			SkipCurrentSite();
		}
	}

	default void SkipCurrentSite() throws DecisionResultInvalidException {
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




	default void FreepsDismissRevealedCards() throws DecisionResultInvalidException { FreepsPassCurrentPhaseAction(); }
	default void ShadowDismissRevealedCards() throws DecisionResultInvalidException { ShadowPassCurrentPhaseAction(); }
	default void DismissRevealedCards() throws DecisionResultInvalidException {
		FreepsDismissRevealedCards();
		ShadowDismissRevealedCards();
	}

}
