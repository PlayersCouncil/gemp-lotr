package com.gempukku.lotro.framework;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

/**
 * A set of functions within the test rig that pertain to decisions.  Decisions in Gemp are a catch-all term referring
 * to any point where the game simulation cannot continue until a player makes a choice.  Thus, awaiting passes,
 * choosing targets, selecting cards from a group, or initiating actions are all "decisions".
 *
 * These functions should give you everything you need to assert that a decision is or is not available, or give you
 * the tools to make a decision properly.  See Choices for selecting among multiple options, and see Actions for
 * top-level card actions.
 */
public interface Decisions extends TestBase  {

	/**
	 * @return Gets the Free Peoples decision that Gemp is currently waiting on.  Will be null if Freeps is not currently
	 * pending any decision.
	 */
	default AwaitingDecision FreepsGetAwaitingDecision() { return GetAwaitingDecision(P1); }
	/**
	 * @return Gets the Shadow decision that Gemp is currently waiting on.  Will be null if Shadow is not currently
	 * pending any decision.
	 */
	default AwaitingDecision ShadowGetAwaitingDecision() { return GetAwaitingDecision(P2); }

	/**
	 * Gets a decision for a given player that Gemp is currently waiting on.  Will be null if that player is not
	 * currently pending any decision.
	 * @param playerID The player's decision to retrieve
	 * @return
	 */
	default AwaitingDecision GetAwaitingDecision(String playerID) { return userFeedback().getAwaitingDecision(playerID); }

	/**
	 * @return Gets the currently pending decision.  Defaults to returning the Free Peoples decision if both players are
	 * currently pending (which only rarely happens in situations such as the starting popup or dismissing cards
	 * revealed to both players).
	 */
	default AwaitingDecision GetCurrentDecision() {
		var freeps = FreepsGetAwaitingDecision();
		if(freeps != null)
			return freeps;
		return ShadowGetAwaitingDecision();
	}

	/**
	 * Determines if the Free Peoples player is currently presented with a decision which contains the given text.
	 * @param text The text snippet to search for.
	 * @return False if Free Peoples has no current decisions or if the current decision does not contain the given text.
	 */
	default Boolean FreepsDecisionAvailable(String text) { return DecisionAvailable(P1, text); }
	/**
	 * Determines if the Shadow player is currently presented with a decision which contains the given text.
	 * @param text The text snippet to search for.
	 * @return False if Shadow has no current decisions or if the current decision does not contain the given text.
	 */
	default Boolean ShadowDecisionAvailable(String text) { return DecisionAvailable(P2, text); }

	/**
	 * Determines if the given player is currently presented with a decision which contains the given text.
	 * @param text The text snippet to search for.
	 * @return False if the given player has no current decisions or if the current decision does not contain the given text.
	 */
	default Boolean DecisionAvailable(String playerID, String text)
	{
		AwaitingDecision ad = GetAwaitingDecision(playerID);
		if(ad == null)
			return false;
		String lowerText = text.toLowerCase();
		return ad.getText().toLowerCase().contains(lowerText);
	}

	/**
	 * @return Returns true if Free Peoples is currently presented with any decision at all.
	 */
	default Boolean FreepsAnyDecisionsAvailable() { return AnyDecisionsAvailable(P1); }
	/**
	 * @return Returns true if Shadow is currently presented with any decision at all.
	 */
	default Boolean ShadowAnyDecisionsAvailable() { return AnyDecisionsAvailable(P2); }

	/**
	 * Returns whether the given player is currently presented with any decision at all.
	 * @param player The player to check for pending decisions
	 * @return True if the given player has a pending decision, else false.
	 */
	default Boolean AnyDecisionsAvailable(String player) {
		var ad = GetAwaitingDecision(player);
		return ad != null;
	}

	/**
	 * Causes the Free Peoples player to pass the current decision.
	 * @throws DecisionResultInvalidException This operation will fail if the current decision is not passable.
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void FreepsPass() throws DecisionResultInvalidException {
		if(FreepsAnyDecisionsAvailable()) {
			PlayerDecided(P1, "");
		}
	}
	/**
	 * Causes the Shadow player to pass the current decision.
	 * @throws DecisionResultInvalidException This operation will fail if the current decision is not passable.
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void ShadowPass() throws DecisionResultInvalidException {
		if(ShadowAnyDecisionsAvailable()) {
			PlayerDecided(P2, "");
		}
	}

	/**
	 * Causes Free Peoples to decide to use the given answer.  Integers usually indicate an index between multiple choices,
	 * but they may be literal integers if the player is asked to choose a number.
	 * @param answer The integer answer to return to the server
	 * @throws DecisionResultInvalidException This operation will fail if the answer is incompatible with the current
	 * decision.
	 */
	default void FreepsDecided(int answer) throws DecisionResultInvalidException { PlayerDecided(P1, String.valueOf(answer));}

	/**
	 * Causes Free Peoples to decide to use the given answer.  Answers may take different forms depending on the exact
	 * nature of the decision at hand.
	 * @param answer The answer to return to the server
	 * @throws DecisionResultInvalidException This operation will fail if the answer is incompatible with the current
	 * decision.
	 */
	default void FreepsDecided(String answer) throws DecisionResultInvalidException { PlayerDecided(P1, answer);}

	/**
	 * Causes Shadow to decide to use the given answer.  Integers usually indicate an index between multiple choices,
	 * but they may be literal integers if the player is asked to choose a number.
	 * @param answer The integer answer to return to the server
	 * @throws DecisionResultInvalidException This operation will fail if the answer is incompatible with the current
	 * decision.
	 */
	default void ShadowDecided(int answer) throws DecisionResultInvalidException { PlayerDecided(P2, String.valueOf(answer));}
	/**
	 * Causes Shadow to decide to use the given answer.  Answers may take different forms depending on the exact
	 * nature of the decision at hand.
	 * @param answer The answer to return to the server
	 * @throws DecisionResultInvalidException This operation will fail if the answer is incompatible with the current
	 * decision.
	 */
	default void ShadowDecided(String answer) throws DecisionResultInvalidException { PlayerDecided(P2, answer);}

	// As this is actually related to the heart of the table simulation, this is left to be implemented on the main Scenario class.
	void PlayerDecided(String player, String answer) throws DecisionResultInvalidException;

	default boolean FreepsHasOptionalTriggerAvailable() { return FreepsDecisionAvailable("Optional"); }
	default boolean ShadowHasOptionalTriggerAvailable() { return ShadowDecisionAvailable("Optional"); }

	default void FreepsAcceptOptionalTrigger() throws DecisionResultInvalidException { PlayerDecided(P1, "0"); }
	default void FreepsDeclineOptionalTrigger() throws DecisionResultInvalidException { PlayerDecided(P1, ""); }
	default void ShadowAcceptOptionalTrigger() throws DecisionResultInvalidException { PlayerDecided(P2, "0"); }
	default void ShadowDeclineOptionalTrigger() throws DecisionResultInvalidException { PlayerDecided(P2, ""); }

}
