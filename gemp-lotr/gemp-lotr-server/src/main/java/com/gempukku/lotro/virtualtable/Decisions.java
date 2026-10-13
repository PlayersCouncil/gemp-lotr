package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A set of functions within the test rig that pertain to decisions.  Decisions in Gemp are a catch-all term referring
 * to any point where the game simulation cannot continue until a player makes a choice.  Thus, awaiting passes,
 * choosing targets, selecting cards from a group, or initiating actions are all "decisions".
 *
 * These functions should give you everything you need to assert that a decision is or is not available, or give you
 * the tools to make a decision properly.  See Choices for selecting among multiple options, and see Actions for
 * top-level card actions.
 */
public interface Decisions extends TableBase {

	/**
	 * @return Gets the Free Peoples decision that Gemp is currently waiting on.  Will be null if Freeps is not currently
	 * pending any decision.
	 */
	default AwaitingDecision FreepsGetAwaitingDecision() { return GetAwaitingDecision(P1()); }
	/**
	 * @return Gets the Shadow decision that Gemp is currently waiting on.  Will be null if Shadow is not currently
	 * pending any decision.
	 */
	default AwaitingDecision ShadowGetAwaitingDecision() { return GetAwaitingDecision(P2()); }

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
	default Boolean FreepsDecisionAvailable(String text) { return DecisionAvailable(P1(), text); }
	/**
	 * Determines if the Shadow player is currently presented with a decision which contains the given text.
	 * @param text The text snippet to search for.
	 * @return False if Shadow has no current decisions or if the current decision does not contain the given text.
	 */
	default Boolean ShadowDecisionAvailable(String text) { return DecisionAvailable(P2(), text); }

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
	default Boolean FreepsAnyDecisionsAvailable() { return AnyDecisionsAvailable(P1()); }
	/**
	 * @return Returns true if Shadow is currently presented with any decision at all.
	 */
	default Boolean ShadowAnyDecisionsAvailable() { return AnyDecisionsAvailable(P2()); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Fellowship phase, or else false.
	 */
	default boolean AwaitingFellowshipPhaseActions() { return FreepsDecisionAvailable("Play Fellowship action or Pass"); }

	/**
	 * Has the Free Peoples player's pending choice of phase actions worked out afresh and sent again, so that it
	 * offers what the board allows now.  The game works out what a decision offers when it sends it, so a card
	 * the rig moved or changed while the choice was pending is otherwise not reflected in it.  Fails if the Free
	 * Peoples player is not at a choice of phase actions ("Play ... action or Pass").
	 */
	default void FreepsRefreshDecision() { RefreshDecision(P1()); }
	/**
	 * Has the Shadow player's pending choice of phase actions worked out afresh and sent again, so that it offers
	 * what the board allows now.  The game works out what a decision offers when it sends it, so a card the rig
	 * moved or changed while the choice was pending is otherwise not reflected in it.  Fails if the Shadow player
	 * is not at a choice of phase actions ("Play ... action or Pass").
	 */
	default void ShadowRefreshDecision() { RefreshDecision(P2()); }
	/**
	 * Has the given player's pending choice of phase actions worked out afresh and sent again.  It is the same
	 * decision, asked again with the actions the board allows now: nothing is answered, nothing moves, and no
	 * other decision (a trigger, a response, a choice of cards) can be refreshed this way.
	 * @param player The player facing a choice of phase actions.  Fails if that player is facing anything else,
	 * or nothing.
	 */
	default void RefreshDecision(String player) {
		AwaitingDecision decision = GetAwaitingDecision(player);
		String asked = decision == null ? null : decision.getText();
		boolean phaseActions = decision instanceof CardActionSelectionDecision && asked != null
				&& asked.toLowerCase().contains("action") && asked.toLowerCase().contains("pass");
		Verify(phaseActions, player + " is not at a choice of phase actions"
				+ (decision == null ? " (nothing is being asked)" : " (the decision pending is '" + asked + "')")
				+ ", so there is nothing to refresh.");
		((CardActionSelectionDecision) decision).refreshActions(game().getActionsEnvironment().getPhaseActions(player));
		userFeedback().sendAwaitingDecision(player, decision);
		userFeedback().atRest();
	}

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Shadow phase, or else false.
	 */
	default boolean AwaitingShadowPhaseActions() { return ShadowDecisionAvailable("Play Shadow action or Pass"); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Maneuver phase, or else false.
	 */
	default boolean AwaitingFreepsManeuverPhaseActions() { return FreepsDecisionAvailable("Play Maneuver action or Pass"); }

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Maneuver phase, or else false.
	 */
	default boolean AwaitingShadowManeuverPhaseActions() { return ShadowDecisionAvailable("Play Maneuver action or Pass"); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Archery phase, or else false.
	 */
	default boolean AwaitingFreepsArcheryPhaseActions() { return FreepsDecisionAvailable("Play Archery action or Pass"); }

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Archery phase, or else false.
	 */
	default boolean AwaitingShadowArcheryPhaseActions() { return ShadowDecisionAvailable("Play Archery action or Pass"); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Assignment phase, or else false.
	 */
	default boolean AwaitingFreepsAssignmentPhaseActions() { return FreepsDecisionAvailable("Play Assignment action or Pass"); }

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Assignment phase, or else false.
	 */
	default boolean AwaitingShadowAssignmentPhaseActions() { return ShadowDecisionAvailable("Play Assignment action or Pass"); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Skirmish phase, or else false.
	 */
	default boolean AwaitingFreepsSkirmishPhaseActions() { return FreepsDecisionAvailable("Choose action to play or Pass"); }

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Skirmish phase, or else false.
	 */
	default boolean AwaitingShadowSkirmishPhaseActions() { return ShadowDecisionAvailable("Choose action to play or Pass"); }

	/**
	 * @return True if the Free Peoples player (player 1) is being asked which skirmish to resolve next.  See
	 * AwaitingSkirmishChoice.
	 */
	default boolean FreepsAwaitingSkirmishChoice() { return AwaitingSkirmishChoice(P1()); }
	/**
	 * @return True if the Shadow player (player 2) is being asked which skirmish to resolve next, as he is on his
	 * own turn, when his is the fellowship that is fighting.  See AwaitingSkirmishChoice.
	 */
	default boolean ShadowAwaitingSkirmishChoice() { return AwaitingSkirmishChoice(P2()); }
	/**
	 * Whether the given player is being asked "Choose next skirmish to resolve": the moment after the assignment
	 * phase, when every assignment has been made, and again after each skirmish while another is waiting.  The
	 * game asks it of the player whose fellowship is fighting, and asks even when one skirmish is all there is.
	 * It is answered with FreepsResolveSkirmish or ShadowResolveSkirmish.
	 * @param player The player who may be choosing.
	 * @return True if that is the decision in front of that player; false at any other, or with nothing asked.
	 */
	default boolean AwaitingSkirmishChoice(String player) { return DecisionAvailable(player, "Choose next skirmish to resolve"); }

	/**
	 * @return True if the Free Peoples player is currently deciding on a top-level phase action to use during the Regroup phase, or else false.
	 */
	default boolean AwaitingFreepsRegroupPhaseActions() { return FreepsDecisionAvailable("Play Regroup action or Pass"); }

	/**
	 * @return True if the Shadow player is currently deciding on a top-level phase action to use during the Regroup phase, or else false.
	 */
	default boolean AwaitingShadowRegroupPhaseActions() { return ShadowDecisionAvailable("Play Regroup action or Pass"); }



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
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void FreepsPass() {
		if(FreepsAnyDecisionsAvailable()) {
			PlayerDecided(P1(), "");
		}
	}
	/**
	 * Causes the Free Peoples player to pass a decision with a given description.
	 * @param text The action text to be passing for.
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void FreepsPass(String text) {
		if(FreepsDecisionAvailable(text)) {
			PlayerDecided(P1(), "");
		}
	}
	/**
	 * Causes the Shadow player to pass the current decision.
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void ShadowPass() {
		if(ShadowAnyDecisionsAvailable()) {
			PlayerDecided(P2(), "");
		}
	}
	/**
	 * Causes the Shadow player to pass the current decision.
	 * @param text The action text to be passing for.
	 */
	// If this seems out of place organization-wise, it's because of the chain of inheritance between the various test interfaces.
	default void ShadowPass(String text) {
		if(ShadowDecisionAvailable(text)) {
			PlayerDecided(P2(), "");
		}
	}

	/**
	 * Causes the given player to pass whatever decision they are currently being presented with, if any.
	 * @param player The player who is currently pending a decision.
	 */
	default void PlayerPass(String player) {
		if(AnyDecisionsAvailable(player)) {
			PlayerDecided(player, "");
		}
	}

	/**
	 * Causes Free Peoples to decide to use the given answer.  Integers usually indicate an index between multiple choices,
	 * but they may be literal integers if the player is asked to choose a number.
	 * @param answer The integer answer to return to the server
	 * decision.
	 */
	default void FreepsDecided(int answer) { PlayerDecided(P1(), String.valueOf(answer));}

	/**
	 * Causes Free Peoples to decide to use the given answer.  Answers may take different forms depending on the exact
	 * nature of the decision at hand.
	 * @param answer The answer to return to the server
	 * decision.
	 */
	default void FreepsDecided(String answer) { PlayerDecided(P1(), answer);}

	/**
	 * Causes Shadow to decide to use the given answer.  Integers usually indicate an index between multiple choices,
	 * but they may be literal integers if the player is asked to choose a number.
	 * @param answer The integer answer to return to the server
	 * decision.
	 */
	default void ShadowDecided(int answer) { PlayerDecided(P2(), String.valueOf(answer));}
	/**
	 * Causes Shadow to decide to use the given answer.  Answers may take different forms depending on the exact
	 * nature of the decision at hand.
	 * @param answer The answer to return to the server
	 * decision.
	 */
	default void ShadowDecided(String answer) { PlayerDecided(P2(), answer);}

	// As this is actually related to the heart of the table simulation, this is left to be implemented on the main Scenario class.
	void PlayerDecided(String player, String answer);

	default boolean FreepsHasOptionalTriggerAvailable() { return FreepsDecisionAvailable("Optional"); }
	/**
	 * @param card The card whose optional trigger or response is being asked about.
	 * @return True if the Free Peoples player is being asked about optional actions and one of them is that
	 * particular card's, whatever its wording.
	 */
	default boolean FreepsHasOptionalTriggerAvailable(PhysicalCardImpl card) { return HasOptionalTriggerAvailable(P1(), card); }
	default boolean ShadowHasOptionalTriggerAvailable() { return ShadowDecisionAvailable("Optional"); }
	/**
	 * @param card The card whose optional trigger or response is being asked about.
	 * @return True if the Shadow player is being asked about optional actions and one of them is that particular
	 * card's, whatever its wording.
	 */
	default boolean ShadowHasOptionalTriggerAvailable(PhysicalCardImpl card) { return HasOptionalTriggerAvailable(P2(), card); }
	/**
	 * Checks by the physical card each offered action belongs to, so a trigger whose definition words its own
	 * prompt is found, and another copy of the same card does not answer for this one.  A decision that does not
	 * say which card its actions belong to is matched on the card's name appearing in the prompt, as before.
	 * @param player The player being asked about optional actions.
	 * @param card The card whose optional trigger or response is being asked about.
	 * @return True if one of the optional actions on offer is that card's.
	 */
	default boolean HasOptionalTriggerAvailable(String player, PhysicalCardImpl card) {
		if(!DecisionAvailable(player, "Optional"))
			return false;
		String[] cardIds = GetAwaitingDecisionParam(player, "cardId");
		if(cardIds != null)
			return Arrays.asList(cardIds).contains(String.valueOf(card.getCardId()));
		String[] texts = GetAwaitingDecisionParam(player, "actionText");
		if(texts == null)
			return false;
		String name = GameUtils.getFullName(card).toLowerCase();
		return Arrays.stream(texts).anyMatch(x -> x.toLowerCase().contains(name));
	}

	/**
	 * For a card that offers more than one optional action at the same moment (a trigger and a response to the
	 * same win, say), where the card alone does not say which is meant.
	 * @param card The card whose optional trigger or response is being asked about.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 * @return True if the Free Peoples player is being asked about optional actions and one of them is that
	 * particular card's and is so described.
	 */
	default boolean FreepsHasOptionalTriggerAvailable(PhysicalCardImpl card, String text) { return HasOptionalTriggerAvailable(P1(), card, text); }
	/**
	 * For a card that offers more than one optional action at the same moment (a trigger and a response to the
	 * same win, say), where the card alone does not say which is meant.
	 * @param card The card whose optional trigger or response is being asked about.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 * @return True if the Shadow player is being asked about optional actions and one of them is that particular
	 * card's and is so described.
	 */
	default boolean ShadowHasOptionalTriggerAvailable(PhysicalCardImpl card, String text) { return HasOptionalTriggerAvailable(P2(), card, text); }
	/**
	 * @param player The player being asked about optional actions.
	 * @param card The card whose optional trigger or response is being asked about.
	 * @param text Words the action's description must contain.  Case is ignored.
	 * @return True if one of the optional actions on offer is that card's and is so described.
	 */
	default boolean HasOptionalTriggerAvailable(String player, PhysicalCardImpl card, String text) {
		return FindOptionalTrigger(player, card, text) != null;
	}
	/**
	 * Finds the optional trigger or response of one particular card whose description contains the given words,
	 * among the optional actions the given player is being offered.  Card and words are matched as
	 * {@link Actions#FindCardAction} matches them for the actions of a phase.
	 * @param player The player being asked about optional actions.
	 * @param card The card the action must belong to.
	 * @param text Words the action's description must contain.  Case is ignored.
	 * @return The id of the action, which answers the decision, or null if there is none, or the player is not
	 * being asked about optional actions.
	 */
	default String FindOptionalTrigger(String player, PhysicalCardImpl card, String text) {
		if(!DecisionAvailable(player, "Optional"))
			return null;
		String[] texts = GetAwaitingDecisionParam(player, "actionText");
		String[] cardIds = GetAwaitingDecisionParam(player, "cardId");
		String[] actionIds = GetAwaitingDecisionParam(player, "actionId");
		if(texts == null || cardIds == null || actionIds == null)
			return null;
		String id = String.valueOf(card.getCardId());
		String wanted = text.toLowerCase();
		for (int i = 0; i < texts.length && i < cardIds.length && i < actionIds.length; i++) {
			if(cardIds[i].equals(id) && texts[i] != null && texts[i].toLowerCase().contains(wanted))
				return actionIds[i];
		}
		return null;
	}

	/**
	 * @return True if the Free Peoples player is being asked in which order required actions that happen at the
	 * same moment are to be resolved.  By rule it is always the Free Peoples player who is asked, whoever's the
	 * actions are.  The game words the question in two ways: "Required responses" for actions that answer
	 * something that has happened, and '&lt;effect&gt; - Required "is about to" responses' for those that answer
	 * something about to happen.  Either is such a question, and nothing else is.
	 */
	default boolean FreepsAwaitingRequiredResponseOrder() {
		AwaitingDecision ad = GetAwaitingDecision(P1());
		if(ad == null)
			return false;
		String text = ad.getText();
		return text.equals("Required responses") || text.endsWith(" - Required \"is about to\" responses");
	}

	default boolean FreepsHasOptionalTriggerAvailable(String text) {
		if(!FreepsDecisionAvailable("Optional"))
			return false;

		var texts = GetADParamAsList(P1(), "actionText");

		for(var choice : texts) {
			if(choice.toLowerCase().contains(text.toLowerCase()))
				return true;
		}

		return false;
	}

	default boolean ShadowHasOptionalTriggerAvailable(String text) {
		if(!ShadowDecisionAvailable("Optional"))
			return false;

		var texts = GetADParamAsList(P2(), "actionText");

		for(var choice : texts) {
			if(choice.toLowerCase().contains(text.toLowerCase()))
				return true;
		}

		return false;
	}

	/**
	 * Causes the Free Peoples player to take the first action on offer at a choice of actions: an optional trigger
	 * or response ("Optional responses"), the order of required responses, or the actions of a phase.  Fails,
	 * saying what is being asked, at any other decision (a yes/no, a choice of cards, a number, an assignment) or
	 * with no action on offer, and then nothing is answered: the answer it gives, "0", means something else at
	 * each of those.  To take one card's trigger among several, FreepsChooseOptionalTrigger(card).
	 */
	default void FreepsAcceptOptionalTrigger() { AcceptOptionalTrigger(P1()); }
	default void FreepsDeclineOptionalTrigger() { PlayerDecided(P1(), ""); }

	/**
	 * Causes the Shadow player to take the first action on offer at a choice of actions: an optional trigger or
	 * response ("Optional responses"), the order of required responses, or the actions of a phase.  Fails, saying
	 * what is being asked, at any other decision (a yes/no, a choice of cards, a number, an assignment) or with no
	 * action on offer, and then nothing is answered: the answer it gives, "0", means something else at each of
	 * those.  To take one card's trigger among several, ShadowChooseOptionalTrigger(card).
	 */
	default void ShadowAcceptOptionalTrigger() { AcceptOptionalTrigger(P2()); }
	default void ShadowDeclineOptionalTrigger() { PlayerDecided(P2(), ""); }

	// "0" is the first action only where actions are on offer.
	private void AcceptOptionalTrigger(String player) {
		DecisionGuard.requiredActionChoice(this, player, player + " was to accept an optional trigger");
		PlayerDecided(player, "0");
	}


	/**
	 * Gets all of the options for a particular parameter of the current awaiting decision as an array of strings.
	 * @param playerID The player making the current decision.
	 * @param paramName The parameter options to return.
	 * @return An array of strings, which are all options for the given parameter.  This list is indexed according to the
	 * options on the current decision, so option 0 is tied to the first choice on the decision, option 1 is the second
	 * choice on the decision, etc.
	 */
	default String[] GetADParam(String playerID, String paramName) { return GetAwaitingDecisionParam(playerID, paramName); }

	default List<String> FreepsGetADParamAsList(String paramName) { return GetADParamAsList(P1(), paramName); }
	default List<String> ShadowGetADParamAsList(String paramName) { return GetADParamAsList(P2(), paramName); }
	default List<String> GetADParamAsList(String playerID, String paramName) {
		var paramList = GetAwaitingDecisionParam(playerID, paramName);
		if(paramList == null)
			return null;

		return Arrays.asList(paramList);
	}

	/**
	 * How many of the entries of the given parameter of the given player's decision equal the given value.  Fails,
	 * saying what each player is being asked, if the player is not being asked anything or the decision has no
	 * such parameter.
	 */
	default int GetADParamEqualsCount(String playerID, String paramName, String value) {
		return (int) Arrays.stream(DecisionGuard.readParam(this, playerID, paramName)).filter(s -> s.equals(value)).count();
	}
	default String[] FreepsGetADParam(String paramName) { return GetAwaitingDecisionParam(P1(), paramName); }
	default String[] ShadowGetADParam(String paramName) { return GetAwaitingDecisionParam(P2(), paramName); }
	/**
	 * The first entry of the given parameter of the Free Peoples player's decision.  Fails, saying what each player
	 * is being asked, if that player is not being asked anything or the decision has no such parameter.
	 */
	default String FreepsGetFirstADParam(String paramName) { return DecisionGuard.readParam(this, P1(), paramName)[0]; }
	/**
	 * The first entry of the given parameter of the Shadow player's decision.  Fails, saying what each player
	 * is being asked, if that player is not being asked anything or the decision has no such parameter.
	 */
	default String ShadowGetFirstADParam(String paramName) { return DecisionGuard.readParam(this, P2(), paramName)[0]; }
	/**
	 * Gets all of the options for a particular parameter of the given player's current decision.
	 * @param playerID The player making the current decision.
	 * @param paramName The parameter options to return.
	 * @return An array of strings indexed like the choices on the decision, or null if the decision has no such
	 * parameter or the player is not being asked anything at all.
	 */
	default String[] GetAwaitingDecisionParam(String playerID, String paramName) {
		var decision = userFeedback().getAwaitingDecision(playerID);
		if(decision == null)
			return null;
		return decision.getDecisionParameters().get(paramName);
	}

	/**
	 * @param playerID The player making the current decision.
	 * @return Every parameter of the given player's current decision, or null if the player is not being asked
	 * anything.
	 */
	default Map<String, String[]> GetAwaitingDecisionParams(String playerID) {
		var decision = userFeedback().getAwaitingDecision(playerID);
		if(decision == null)
			return null;
		return decision.getDecisionParameters();
	}

}
