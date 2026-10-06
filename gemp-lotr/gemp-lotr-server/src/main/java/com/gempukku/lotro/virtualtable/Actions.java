package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actions are top-level decisions that involve legal game operations available to players.  For example, deploying
 * a card, playing an interrupt, and activating a card ability are all Actions.
 */
public interface Actions extends Decisions, Choices {

	/**
	 * @return Gets the text descriptions of all current actions available to the Free Peoples player. Will return an empty
	 * list if that player does not have a currently pending decision.
	 */
	default List<String> FreepsGetAvailableActions() { return GetAvailableActions(P1()); }
	/**
	 * @return Gets the text descriptions of all current actions available to the Shadow player. Will return an empty
	 * list if that player does not have a currently pending decision.
	 */
	default List<String> ShadowGetAvailableActions() { return GetAvailableActions(P2()); }
	/**
	 * @param playerID The player with a current decision
	 * @return Gets the text descriptions of all current actions available to the given player.  Will return an empty
	 * list if that player does not have a currently pending decision.
	 */
	default List<String> GetAvailableActions(String playerID) {
		AwaitingDecision decision = GetAwaitingDecision(playerID);
		if(decision == null) {
			return new ArrayList<>();
		}
		return Arrays.asList(decision.getDecisionParameters().get("actionText"));
	}

	/**
	 * @return True if an action is available as part of the current decision, false if there are no actions or the
	 * Free Peoples player has no pending decisions.
	 */
	default Boolean FreepsAnyActionsAvailable() { return AnyActionsAvailable(P1()); }

	/**
	 * @return True if an action is available as part of the current decision, false if there are no actions or the
	 * Shadow player has no pending decisions.
	 */
	default Boolean ShadowAnyActionsAvailable() { return AnyActionsAvailable(P2()); }

	/**
	 * Returns whether the given player has any action at all available as part of the currently pending decision.
	 * @param player The player to check for.
	 * @return True if an action is available as part of the current decision, false if there are no actions or the
	 * current player has no pending decisions.
	 */
	default Boolean AnyActionsAvailable(String player) {
		List<String> actions = GetAvailableActions(player);
		return !actions.isEmpty();
	}



	/**
	 * @param card The card whose special ability is being asked about.
	 * @return True if the Free Peoples player is currently being offered the use of that particular card's special
	 * ability.  Another copy of the same card being usable does not count.
	 */
	default Boolean FreepsActionAvailable(PhysicalCardImpl card) { return CardActionAvailable(P1(), card, "Use"); }
	/**
	 * @param card The card whose special ability is being asked about.
	 * @return True if the Shadow player is currently being offered the use of that particular card's special
	 * ability.  Another copy of the same card being usable does not count.
	 */
	default Boolean ShadowActionAvailable(PhysicalCardImpl card) { return CardActionAvailable(P2(), card, "Use"); }

	/**
	 * @param card The card being asked about.
	 * @return True if the Free Peoples player is currently being offered the play of that particular card.  Another
	 * copy of the same card being playable does not count.
	 */
	default Boolean FreepsPlayAvailable(PhysicalCardImpl card) { return CardActionAvailable(P1(), card, "Play"); }
	/**
	 * @param card The card being asked about.
	 * @return True if the Shadow player is currently being offered the play of that particular card.  Another copy
	 * of the same card being playable does not count.
	 */
	default Boolean ShadowPlayAvailable(PhysicalCardImpl card) { return CardActionAvailable(P2(), card, "Play"); }

	/**
	 * @param card The card being asked about.
	 * @return True if the Free Peoples player is currently being offered the transfer of that particular card.
	 */
	default Boolean FreepsTransferAvailable(PhysicalCardImpl card) { return CardActionAvailable(P1(), card, "Transfer"); }
	/**
	 * @param card The card being asked about.
	 * @return True if the Shadow player is currently being offered the transfer of that particular card.
	 */
	default Boolean ShadowTransferAvailable(PhysicalCardImpl card) { return CardActionAvailable(P2(), card, "Transfer"); }

	/**
	 * Checks whether the given player is currently being offered an action of the given kind on one particular
	 * card, and not on another copy of it.
	 *
	 * A play or a transfer is recognised by its description, "Play [the card's full name]" or "Transfer [...]".
	 * The use of a special ability is described as "Use [the card's full name]" unless the card's definition gives
	 * the ability wording of its own, in which case, among the actions a player may take in a phase, it is any
	 * action of that card that is not a play or a transfer of it.
	 *
	 * How an action is tied to its card depends on what the decision says about its actions.  A choice of card
	 * actions names the physical card each action belongs to, and that is what is compared.  A decision that names
	 * its actions' cards only by blueprint is compared by blueprint.  A decision that names neither is matched on
	 * the description alone.
	 * @param player The player who would be offered the action.
	 * @param card The card the action must belong to.
	 * @param verb How the action's description starts: "Use", "Play" or "Transfer".
	 * @return True if such an action is on offer, false if it is not or the player has no pending decision.
	 */
	default Boolean CardActionAvailable(String player, PhysicalCardImpl card, String verb) {
		String[] texts = GetAwaitingDecisionParam(player, "actionText");
		if(texts == null)
			return false;
		String[] cardIds = GetAwaitingDecisionParam(player, "cardId");
		String[] blueprintIds = GetAwaitingDecisionParam(player, "blueprintId");
		String name = GameUtils.getFullName(card).toLowerCase();
		String wanted = verb.toLowerCase() + " " + name;
		String id = String.valueOf(card.getCardId());
		// "Play Maneuver action or Pass", "Choose action to play or Pass": the actions of a phase, as opposed to
		// the triggers and responses a player is asked about at other times.
		String asked = userFeedback().getAwaitingDecision(player).getText();
		boolean phaseActions = asked != null && asked.toLowerCase().contains("action") && asked.toLowerCase().contains("pass");

		for (int i = 0; i < texts.length; i++) {
			String text = texts[i].toLowerCase();
			boolean described = text.contains(wanted);
			if(cardIds != null && i < cardIds.length) {
				if(!cardIds[i].equals(id))
					continue;
				if(described)
					return true;
				// An ability with wording of its own.
				if(verb.equalsIgnoreCase("Use") && phaseActions
						&& !text.contains("play " + name) && !text.contains("transfer " + name) && !text.startsWith("heal by discard"))
					return true;
			}
			else if(blueprintIds != null && i < blueprintIds.length) {
				if(described && blueprintIds[i].equals(card.getBlueprintId()))
					return true;
			}
			else if(described) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks whether the Free Peoples player has an action available containing the provided text.
	 * @param action The text to search for.
	 * @return True if an active decision has an action matching text, otherwise false.
	 */
	default Boolean FreepsActionAvailable(String action) { return ActionAvailable(P1(), action); }
	/**
	 * Checks whether the Shadow player has an action available containing the provided text.
	 * @param action The text to search for.
	 * @return True if an active decision has an action matching text, otherwise false.
	 */
	default Boolean ShadowActionAvailable(String action) { return ActionAvailable(P2(), action); }
	/**
	 * Checks whether the given player has an action available containing the provided text.
	 * @param player The player to check for.
	 * @param action The text to search for.
	 * @return True if an active decision has an action matching text, otherwise false.
	 */
	default Boolean ActionAvailable(String player, String action) {
		List<String> actions = GetAvailableActions(player);
		if(actions == null)
			return false;
		String lowerAction = action.toLowerCase();
		return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerAction));
	}


	default void FreepsUseCardAction(PhysicalCardImpl card) { FreepsDecided(GetCardActionId(P1(), card)); }
	default void ShadowUseCardAction(PhysicalCardImpl card) { ShadowDecided(GetCardActionId(P2(), card)); }

	/**
	 * Causes the Free Peoples player to take one particular action of a card that offers several.
	 * @param card The card the action belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 */
	default void FreepsUseCardAction(PhysicalCardImpl card, String text) { UseCardAction(P1(), card, text); }
	/**
	 * Causes the Shadow player to take one particular action of a card that offers several.
	 * @param card The card the action belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 */
	default void ShadowUseCardAction(PhysicalCardImpl card, String text) { UseCardAction(P2(), card, text); }
	/**
	 * Causes the given player to take the action of the given card whose description contains the given words.
	 * @param player The player facing a choice of actions.
	 * @param card The card the action belongs to.
	 * @param text Words from the action's description.  Case is ignored.
	 */
	default void UseCardAction(String player, PhysicalCardImpl card, String text) {
		String actionId = FindCardAction(player, card, text);
		Verify(actionId != null, GameUtils.getFullName(card) + " is not offering " + player + " an action described as '" + text + "'.");
		PlayerDecided(player, actionId);
	}

	/**
	 * @param card The card the action must belong to.
	 * @param text Words the action's description must contain.  Case is ignored.
	 * @return True if the Free Peoples player is being offered such an action of that particular card.
	 */
	default Boolean FreepsActionAvailable(PhysicalCardImpl card, String text) { return FindCardAction(P1(), card, text) != null; }
	/**
	 * @param card The card the action must belong to.
	 * @param text Words the action's description must contain.  Case is ignored.
	 * @return True if the Shadow player is being offered such an action of that particular card.
	 */
	default Boolean ShadowActionAvailable(PhysicalCardImpl card, String text) { return FindCardAction(P2(), card, text) != null; }
	/**
	 * Finds the action of one particular card whose description contains the given words, among the actions the
	 * given player is being offered.
	 * @param player The player facing a choice of actions.
	 * @param card The card the action must belong to.
	 * @param text Words the action's description must contain.  Case is ignored.
	 * @return The id of the action, which answers the decision, or null if there is none or nothing is being asked.
	 */
	default String FindCardAction(String player, PhysicalCardImpl card, String text) {
		String[] texts = GetAwaitingDecisionParam(player, "actionText");
		String[] cardIds = GetAwaitingDecisionParam(player, "cardId");
		String[] actionIds = GetAwaitingDecisionParam(player, "actionId");
		if(texts == null || cardIds == null || actionIds == null)
			return null;
		String id = String.valueOf(card.getCardId());
		String wanted = text.toLowerCase();
		for (int i = 0; i < texts.length && i < cardIds.length && i < actionIds.length; i++) {
			if(cardIds[i].equals(id) && texts[i].toLowerCase().contains(wanted))
				return actionIds[i];
		}
		return null;
	}

	/**
	 * @param card The card whose action is meant, which must be offering the Free Peoples player exactly one.
	 * @return What the pending decision says of that action's having no effect: "false" if it is not flagged, or
	 * the reason it is: "effect" (its effect will do nothing), "limit" (the ability or its bonus has reached its
	 * limit) or "skirmish" (a skirmish action that reaches nobody in the skirmish being fought).
	 */
	default String FreepsActionNoEffect(PhysicalCardImpl card) { return CardActionNoEffect(P1(), card, null); }
	/**
	 * @param card The card whose action is meant, which must be offering the Shadow player exactly one.
	 * @return What the pending decision says of that action's having no effect: "false" if it is not flagged, or
	 * the reason it is: "effect", "limit" or "skirmish".
	 */
	default String ShadowActionNoEffect(PhysicalCardImpl card) { return CardActionNoEffect(P2(), card, null); }
	/**
	 * @param card The card the action belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 * @return What the pending decision says of that action's having no effect: "false", "effect", "limit" or
	 * "skirmish".
	 */
	default String FreepsActionNoEffect(PhysicalCardImpl card, String text) { return CardActionNoEffect(P1(), card, text); }
	/**
	 * @param card The card the action belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 * @return What the pending decision says of that action's having no effect: "false", "effect", "limit" or
	 * "skirmish".
	 */
	default String ShadowActionNoEffect(PhysicalCardImpl card, String text) { return CardActionNoEffect(P2(), card, text); }
	/**
	 * Reads the no-effect flag of one action of one particular card in the choice of actions the given player is
	 * facing.  The game flags an action it can tell beforehand will do nothing; the action is offered all the same
	 * and may be taken.  Fails the test if the card is offering that player no such action, or more than one.
	 * @param player The player facing a choice of actions.
	 * @param card The card the action belongs to.
	 * @param text Words the action's description must contain, or null if the card offers only one action.  Case
	 * is ignored.
	 * @return "false" if the action is not flagged; otherwise the reason it is: "effect" (its effect will do
	 * nothing), "limit" (the ability or its bonus has reached its limit) or "skirmish" (a skirmish action that
	 * reaches nobody in the skirmish being fought).
	 */
	default String CardActionNoEffect(String player, PhysicalCardImpl card, String text) {
		String[] texts = GetAwaitingDecisionParam(player, "actionText");
		String[] cardIds = GetAwaitingDecisionParam(player, "cardId");
		String[] noEffects = GetAwaitingDecisionParam(player, "noEffect");
		String name = GameUtils.getFullName(card);
		String described = text == null ? "an action" : "an action described as '" + text + "'";
		Verify(texts != null && cardIds != null, player + " is not being offered a choice of actions, so " + name + " is not offering " + described + ".");
		Verify(noEffects != null && noEffects.length == texts.length, "The decision " + player + " is facing does not say, action by action, which of them have no effect.");
		String id = String.valueOf(card.getCardId());
		String wanted = text == null ? "" : text.toLowerCase();
		String found = null;
		int count = 0;
		for (int i = 0; i < texts.length && i < cardIds.length; i++) {
			if(cardIds[i].equals(id) && texts[i].toLowerCase().contains(wanted)) {
				found = noEffects[i];
				count++;
			}
		}
		Verify(count > 0, name + " is not offering " + player + " " + described + ".");
		Verify(count == 1, name + " is offering " + player + " " + count + " actions that fit; say which is meant by words of its description.");
		return found;
	}

	default void FreepsTransferCard(PhysicalCardImpl card) { FreepsDecided(GetCardActionId(P1(), card, "Transfer")); }
	default void ShadowTransferCard(PhysicalCardImpl card) { ShadowDecided(GetCardActionId(P2(), card, "Transfer ")); }

	/**
	 * Causes the Free Peoples player to play that card from hand as if they had clicked it in the UI.
	 * @param card The card to play.
	 * legal to play (due to costs, requirements, or other rules).
	 */
	default void FreepsPlayCard(PhysicalCardImpl card) { FreepsDecided(GetCardActionId(P1(), card, "Play")); }
	/**
	 * Causes the Shadow player to play that card from hand as if they had clicked it in the UI.
	 * @param card The card to play.
	 * legal to play (due to costs, requirements, or other rules).
	 */
	default void ShadowPlayCard(PhysicalCardImpl card) { ShadowDecided(GetCardActionId(P2(), card, "Play")); }


	/**
	 * Causes the Free Peoples player to select a unique character in hand, which will then be discarded to heal a
	 * copy of that character in play.
	 * @return Whether a discard-to-heal action is currently available
	 */
	default boolean FreepsHasDiscardToHealAvailable() { return ActionAvailable(P1(), "Heal by discard"); }

	/**
	 * Causes the Free Peoples player to select a unique character in hand, which will then be discarded to heal a
	 * copy of that character in play.
	 * @param card The card to discard from hand.
	 * legal to use (due to costs, requirements, or other rules).
	 */
	default void FreepsDiscardToHeal(PhysicalCardImpl card) { FreepsDecided(GetCardActionId(P1(), card, "Heal by discard")); }
	/**
	 * Searches the currently available actions on the current decision for the given player and returns the ID of an
	 * action which contains the provided text in its description.
	 * @param playerId The player which must have a currently active decision.
	 * @param text Constrains the result to only actions whose description contains the provided text
	 * @return The action ID of a matching action (which can be passed as a decision answer).  Returns null if no
	 * actions matched.
	 */
	default String GetCardActionId(String playerId, String text) { return GetCardActionId(playerId, null, text); }
	/**
	 * Searches the currently available actions on the current decision for the given player and returns the ID of an
	 * action which was sourced by the provided card's ID.
	 * @param playerId The player which must have a currently active decision.
	 * @param card Constrains the result to only actions which are source from this card.
	 * @return The action ID of a matching action (which can be passed as a decision answer).  Returns null if no
	 * actions matched.
	 */
	default String GetCardActionId(String playerId, PhysicalCardImpl card) { return GetCardActionId(playerId, card, null); }

	/**
	 * Searches the currently available actions on the current decision for the given player.  If card is provided, the
	 * card's ID must be the source of one of the given actions.  If text is provided, the action description must match
	 * the given text.  If both are provided, both are checked.
	 * @param playerId The player which must have a currently active decision.
	 * @param card If provided, constrains the result to only actions which are source from this card.
	 * @param text If provided, constrains the result to only actions whose description contains the provided text
	 * @return The action ID of a matching action (which can be passed as a decision answer).  Returns null if no
	 * actions matched.
	 */
	default String GetCardActionId(String playerId, PhysicalCardImpl card, String text) {
		String id = card != null ? String.valueOf(card.getCardId()) : null;
		String[] cardIds = GetAwaitingDecisionParam(playerId, "cardId");
		String[] actionTexts = GetAwaitingDecisionParam(playerId, "actionText");

		for (int i = 0; i < cardIds.length; i++) {
			if ((id == null || cardIds[i].equals(id)) && (text == null || actionTexts[i].contains(text))) {
				return GetAwaitingDecisionParam(playerId, "actionId")[i];
			}
		}
		return null;
	}

}
