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
	 * card.  The action has to be described as "[verb] [the card's full name]", as before, and now also has to
	 * belong to that card and not to another copy of it.
	 *
	 * How an action is tied to its card depends on what the decision says about its actions.  A choice of card
	 * actions names the physical card each action belongs to, and that is what is compared.  A decision that names
	 * its actions' cards only by blueprint (a choice among triggers, say, where there may be no card on the table
	 * to point at) is compared by blueprint.  A decision that names neither is matched on the description alone.
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
		String wanted = (verb + " " + GameUtils.getFullName(card)).toLowerCase();
		String id = String.valueOf(card.getCardId());

		for (int i = 0; i < texts.length; i++) {
			if(!texts[i].toLowerCase().contains(wanted))
				continue;
			if(cardIds != null && i < cardIds.length) {
				if(cardIds[i].equals(id))
					return true;
			}
			else if(blueprintIds != null && i < blueprintIds.length) {
				if(blueprintIds[i].equals(card.getBlueprintId()))
					return true;
			}
			else {
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
