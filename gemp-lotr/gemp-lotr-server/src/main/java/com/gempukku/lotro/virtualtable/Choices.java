package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Decisions will always come with at least one choice, even if that single choice is "pass".  These functions will
 * let you inspect the choices available and offer shortcuts for e.g. selecting a physical card which you have
 * previously stored.
 */
public interface Choices extends Decisions {

	default void FreepsDeclineChoosing() { PlayerDecided(P1(), ""); }
	default void ShadowDeclineChoosing() { PlayerDecided(P2(), ""); }

	default void FreepsDeclineSanctuaryHealing() {
		PlayerDecided(P1(), "");
		PlayerDecided(P1(), "");
		PlayerDecided(P1(), "");
		PlayerDecided(P1(), "");
		PlayerDecided(P1(), "");
	}

	/**
	 * Determines whether the Free Peoples player has any choices on the current decision whose description matches the
	 * provided search text.
	 * @param choice The search text the choice description must contain.
	 * @return True if the Free Peoples player has an active decision with a choice description matching the given text.
	 */
	default Boolean FreepsChoiceAvailable(String choice) { return ChoiceAvailable(P1(), choice); }
	/**
	 * Determines whether the Shadow player has any choices on the current decision whose description matches the
	 * provided search text.
	 * @param choice The search text the choice description must contain.
	 * @return True if the Shadow player has an active decision with a choice description matching the given text.
	 */
	default Boolean ShadowChoiceAvailable(String choice) { return ChoiceAvailable(P2(), choice); }
	/**
	 * Determines whether the given player has any choices on the current decision whose description matches the
	 * provided search text.
	 * @param player The player which must have a currently active decision.
	 * @param choice The search text the choice description must contain.
	 * @return True if the given player has an active decision with a choice description matching the given text.
	 */
	default Boolean ChoiceAvailable(String player, String choice) {
		List<String> actions = GetADParamAsList(player, "results");
		if(actions == null)
			return false;
		String lowerChoice = choice.toLowerCase();
		return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerChoice));
	}

	/**
	 * Causes the Free Peoples player to choose the given option.
	 * This is a catch-all that either selects the provided choice if part of a multiple choice decision, or else
	 * falls back on providing the provided choice as a top-level response to the current decision.
	 * @param choice The choice (or decision response)
	 */
	default void FreepsChoose(String choice) {
		if(FreepsGetMultipleChoiceCount() > 0) {
			FreepsChooseOption(choice);
		}
		else if(FreepsGetActionCount() > 0) {
			FreepsChooseAction(choice);
		}
		else {
			PlayerDecided(P1(), choice);
		}
	}
	/**
	 * Causes the Free Peoples player to choose the given options.  This will automatically format the response to contain
	 * all the provided options in a comma-separated list.
	 * @param choices The choices the player wishes to make.
	 */
	default void FreepsChoose(String...choices) { PlayerDecided(
			P1(), String.join(",", choices)); }

	/**
	 * Causes the Shadow player to choose the given option.
	 * This is a catch-all that either selects the provided choice if part of a multiple choice decision, or else
	 * falls back on providing the provided choice as a top-level response to the current decision.
	 * @param choice The choice (or decision response)
	 */
	default void ShadowChoose(String choice) {
		if(ShadowGetMultipleChoiceCount() > 0) {
			ShadowChooseOption(choice);
		}
		else if(ShadowGetActionCount() > 0) {
			ShadowChooseAction(choice);
		}
		else {
			PlayerDecided(P2(), choice);
		}
	}
	/**
	 * Causes the Shadow player to choose the given options.  This will automatically format the response to contain
	 * all the provided options in a comma-separated list.
	 * @param choices The choices the player wishes to make.
	 */
	default void ShadowChoose(String...choices) { PlayerDecided(
			P2(), String.join(",", choices)); }




	/**
	 * Causes the Free Peoples player to return a canned "Yes" response to a Yes or No question.
	 */
	default void FreepsChooseYes() { ChooseOption(P1(), "Yes"); }
	/**
	 * Causes the Shadow player to return a canned "Yes" response to a Yes or No question.
	 */
	default void ShadowChooseYes() { ChooseOption(P2(), "Yes"); }
	/**
	 * Causes the Free Peoples player to return a canned "No" response to a Yes or No question.
	 */
	default void FreepsChooseNo() { ChooseOption(P1(), "No"); }
	/**
	 * Causes the Shadow player to return a canned "No" response to a Yes or No question.
	 */
	default void ShadowChooseNo() { ChooseOption(P2(), "No"); }

	default void FreepsChooseOption(String option) { ChooseOption(P1(), option); }
	default void ShadowChooseOption(String option) { ChooseOption(P2(), option); }

	/**
	 * Used in situations where there are simultaneous triggers from cards with the same title and we need to
	 * pick a specific one.
	 * @param card Which of the triggering cards to pick
	 */
	default void FreepsChooseOptionalTrigger(PhysicalCardImpl card) { ChooseOptionalTrigger(P1(), card); }

	/**
	 * Used in situations where there are simultaneous triggers from cards with the same title and we need to
	 * pick a specific one.
	 * @param card Which of the triggering cards to pick
	 */
	default void ShadowChooseOptionalTrigger(PhysicalCardImpl card) { ChooseOptionalTrigger(P2(), card); }

	/**
	 * Takes the trigger or response that the given card is offering the given player.  The card is matched exactly:
	 * with cards 4 and 40 both offering, asking for 4 takes 4.
	 * @param playerID The player being offered the actions.
	 * @param card Which of the offering cards to pick.
	 */
	default void ChooseOptionalTrigger(String playerID, PhysicalCardImpl card) {
		String[] cardIds = GetAwaitingDecisionParam(playerID, "cardId");
		int index = cardIds == null ? -1 : Arrays.asList(cardIds).indexOf(String.valueOf(card.getCardId()));
		Verify(index >= 0, GameUtils.getFullName(card) + " is not offering " + playerID + " a trigger or response to choose.");
		PlayerDecided(playerID, String.valueOf(index));
	}
	/**
	 * Takes one particular optional trigger or response of a card that is offering more than one at the same
	 * moment.  Fails if that card is not offering the Free Peoples player an optional action so described.
	 * @param card The card the trigger or response belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 */
	default void FreepsChooseOptionalTrigger(PhysicalCardImpl card, String text) { ChooseOptionalTrigger(P1(), card, text); }
	/**
	 * Takes one particular optional trigger or response of a card that is offering more than one at the same
	 * moment.  Fails if that card is not offering the Shadow player an optional action so described.
	 * @param card The card the trigger or response belongs to.
	 * @param text Words from the action's description that tell it from the card's other actions.  Case is ignored.
	 */
	default void ShadowChooseOptionalTrigger(PhysicalCardImpl card, String text) { ChooseOptionalTrigger(P2(), card, text); }
	/**
	 * Takes the optional trigger or response of the given card whose description contains the given words.
	 * @param playerID The player being offered the actions.
	 * @param card The card the trigger or response belongs to.
	 * @param text Words from the action's description.  Case is ignored.
	 */
	default void ChooseOptionalTrigger(String playerID, PhysicalCardImpl card, String text) {
		String actionId = FindOptionalTrigger(playerID, card, text);
		Verify(actionId != null, GameUtils.getFullName(card) + " is not offering " + playerID
				+ " a trigger or response described as '" + text + "'.");
		PlayerDecided(playerID, actionId);
	}
	default void ChooseOption(String playerID, String option) {
		ChooseAction(playerID, "results", option);
	}

	default void FreepsChooseAction(String paramName, String option) { ChooseAction(P1(), paramName, option); }
	default void FreepsChooseAction(String option) { ChooseAction(P1(), "actionText", option); }
	default void ShadowChooseAction(String paramName, String option) { ChooseAction(P2(), paramName, option); }
	default void ShadowChooseAction(String option) { ChooseAction(P2(), "actionText", option); }
	default void ChooseAction(String playerID, String paramName, String option) {
		List<String> choices = GetADParamAsList(playerID, paramName);
		for (int i = 0; i < choices.size(); i++) {
			String choice = choices.get(i);
			// A choice has no text only when a rule and not a card is the source of an action.  Such a choice
			// matches no words, and words match no such choice.
			boolean matches = option == null || choice == null
					? option == null && choice == null
					: choice.toLowerCase().contains(option.toLowerCase());
			if(matches) {
				PlayerDecided(playerID, String.valueOf(i));
				return;
			}
		}
		Verify(option != null, "ChooseAction: " + playerID + " is being offered no choice without text.");
		//couldn't find an exact match, so maybe it's a direct index:
		PlayerDecided(playerID, option);
	}

	default void FreepsChooseAny() {
		if (GetChoiceCount(FreepsGetActionChoices()) > 0){
			ChooseAction(P1(), "actionId", FreepsGetActionChoices().getFirst());
		}
		else if(FreepsGetBPChoices().size() > 1) {
			ChooseCardBPFromSelection(P1(), FreepsGetBPChoices().getFirst());
		}
		else {
			ChooseAction(P1(), "actionText", GetADParamAsList(P1(), "actionText").getFirst());
		}
	}

	/**
	 * Where required actions happen at the same moment (the wound for losing a skirmish and a card's "each time
	 * ... loses a skirmish", say), the Free Peoples player is asked which goes first.  This takes the game's own
	 * rule, the action that belongs to no card, wherever it stands in the list and whether or not the game gives
	 * it any wording (the rule that follows a death has none).  It is an error to call it when the Free Peoples
	 * player is not being asked to order required responses (an optional trigger on offer is not that), and an
	 * error when no rule is among the actions to be ordered: two triggers of cards are ordered by naming one,
	 * with FreepsResolveActionOrder.
	 */
	default void FreepsResolveRuleFirst() {
		Verify(FreepsAwaitingRequiredResponseOrder(),
				"FreepsResolveRuleFirst: the Free Peoples player is not being asked to order required responses.");
		String[] sources = GetAwaitingDecisionParam(P1(), "blueprintId");
		String[] actionIds = GetAwaitingDecisionParam(P1(), "actionId");
		int rule = sources == null ? -1 : Arrays.asList(sources).indexOf("rules");
		Verify(rule >= 0 && actionIds != null && rule < actionIds.length,
				"FreepsResolveRuleFirst: no rule is among the required responses to be ordered, which are "
						+ GetADParamAsList(P1(), "actionText") + ".  Name the one to resolve first with FreepsResolveActionOrder.");
		PlayerDecided(P1(), actionIds[rule]);
	}

	/**
	 * As FreepsResolveRuleFirst, but takes the required action whose description contains the given text: a
	 * card's trigger, by the card's name.  A rule among the actions, which may have no description at all, is
	 * passed over.
	 * @param option Text to look for in the description of the action to resolve first.
	 */
	default void FreepsResolveActionOrder(String option) {
		Verify(FreepsAwaitingRequiredResponseOrder(),
				"FreepsResolveActionOrder: the Free Peoples player is not being asked to order required responses.");
		ChooseAction(P1(), "actionText", option);
	}


	/**
	 * @return Gets the min parameter on the current choice for the Free Peoples player.  This may be a minimum
	 * number of responses, or the smallest acceptable numeric answer, depending on context.
	 */
	default int FreepsGetChoiceMin() { return Integer.parseInt(FreepsGetFirstADParam("min")); }
	/**
	 * @return Gets the max parameter on the current choice for the Free Peoples player.  This may be a maximum
	 * number of responses, or the largest acceptable numeric answer, depending on context.
	 */
	default int FreepsGetChoiceMax() { return Integer.parseInt(FreepsGetFirstADParam("max")); }
	/**
	 * @return Gets the min parameter on the current choice for the Shadow player.  This may be a minimum
	 * number of responses, or the smallest acceptable numeric answer, depending on context.
	 */
	default int ShadowGetChoiceMin() { return Integer.parseInt(ShadowGetFirstADParam("min")); }
	/**
	 * @return Gets the max parameter on the current choice for the Shadow player.  This may be a maximum
	 * number of responses, or the largest acceptable numeric answer, depending on context.
	 */
	default int ShadowGetChoiceMax() { return Integer.parseInt(ShadowGetFirstADParam("max")); }

	/**
	 * @return Gets how many of the currently displayed cards are selectable for a Free Peoples decision.
	 */
	default int FreepsGetSelectableCount() {
		return GetADParamEqualsCount(P1(), "selectable", "true");
	}

	/**
	 * @return Gets how many of the currently displayed cards are selectable for a Shadow decision.
	 */
	default int ShadowGetSelectableCount() {
		return GetADParamEqualsCount(P2(), "selectable", "true");
	}

	/**
	 * @return How many cards the Free Peoples player is currently being shown, whether or not any may be chosen.
	 */
	default int FreepsGetTotalRevealedCardCount() { return GetTotalRevealedCardCount(P1()); }

	/**
	 * @return How many cards the Shadow player is currently being shown, whether or not any may be chosen.
	 */
	default int ShadowGetTotalRevealedCardCount() { return GetTotalRevealedCardCount(P2()); }

	default int GetTotalRevealedCardCount(String playerID) {
		AwaitingDecision decision = GetAwaitingDecision(playerID);
		if(decision == null || decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
			return 0;
		String[] cardIds = GetAwaitingDecisionParam(playerID, "cardId");
		return cardIds == null ? 0 : cardIds.length;
	}

	default boolean FreepsHasBPChoice(PhysicalCardImpl card) { return FreepsGetBPChoices().contains(card.getBlueprintId()); }
	default boolean ShadowHasBPChoice(PhysicalCardImpl card) { return ShadowGetBPChoices().contains(card.getBlueprintId()); }
	default List<String> FreepsGetBPChoices() { return GetADParamAsList(P1(), "blueprintId"); }
	default List<String> ShadowGetBPChoices() { return GetADParamAsList(P2(), "blueprintId"); }
	default int FreepsGetBPChoiceCount() { return FreepsGetADParamAsList("blueprintId").size(); }
	default int ShadowGetBPChoiceCount() { return ShadowGetADParamAsList("blueprintId").size(); }
	default List<String> FreepsGetActionChoices() { return GetADParamAsList(P1(), "actionId"); }
	default List<String> ShadowGetActionChoices() { return GetADParamAsList(P2(), "actionId"); }
	default List<String> FreepsGetMultipleChoices() { return GetADParamAsList(P1(), "results"); }
	default List<String> ShadowGetMultipleChoices() { return GetADParamAsList(P2(), "results"); }
	default List<String> FreepsGetCardChoices() { return GetADParamAsList(P1(), "cardId"); }
	default List<String> ShadowGetCardChoices() { return GetADParamAsList(P2(), "cardId"); }
	default int FreepsGetMultipleChoiceCount() { return GetChoiceCount(FreepsGetMultipleChoices());	}
	default int ShadowGetMultipleChoiceCount() { return GetChoiceCount(ShadowGetMultipleChoices());	}
	default int FreepsGetActionCount() { return GetChoiceCount(FreepsGetActionChoices()); }
	default int ShadowGetActionCount() { return GetChoiceCount(ShadowGetActionChoices()); }

	default int GetChoiceCount(List<String> list) {
		if(list == null)
			return 0;
		return list.size();
	}

	default List<String> FreepsGetFreepsAssignmentTargets() { return GetADParamAsList(P1(), "freeCharacters"); }
	default List<String> FreepsGetShadowAssignmentTargets() { return GetADParamAsList(P1(), "minions"); }

	default List<String> ShadowGetFreepsAssignmentTargets() { return GetADParamAsList(P2(), "freeCharacters"); }
	default List<String> ShadowGetShadowAssignmentTargets() { return GetADParamAsList(P2(), "minions"); }

	/**
	 * @param card A companion, ally or minion.
	 * @return True if the assignment decision the Free Peoples player is facing offers that character, on either
	 * side of it.  False if it does not, or the player is not facing an assignment decision.
	 */
	default boolean FreepsHasAssignmentTargetAvailable(PhysicalCardImpl card) { return HasAssignmentTargetAvailable(P1(), card); }
	/**
	 * @param card A companion, ally or minion.
	 * @return True if the assignment decision the Shadow player is facing offers that character, on either side of
	 * it.  False if it does not, or the player is not facing an assignment decision.
	 */
	default boolean ShadowHasAssignmentTargetAvailable(PhysicalCardImpl card) { return HasAssignmentTargetAvailable(P2(), card); }
	/**
	 * Reads the decision in front of the player, where FreepsCanAssign and ShadowCanAssign read the game's rule.
	 * @param player The player facing an assignment decision.
	 * @param card A companion, ally or minion.
	 * @return True if that decision lists the character among those who may be assigned.
	 */
	default boolean HasAssignmentTargetAvailable(String player, PhysicalCardImpl card) {
		String id = String.valueOf(card.getCardId());
		for (String param : new String[] { "freeCharacters", "minions" }) {
			String[] ids = GetAwaitingDecisionParam(player, param);
			if(ids != null && Arrays.asList(ids).contains(id))
				return true;
		}
		return false;
	}






	default void FreepsChooseAnyCard() { FreepsChoose(FreepsGetCardChoices().getFirst()); }
	default void ShadowChooseAnyCard() { ShadowChoose(ShadowGetCardChoices().getFirst()); }

	default void FreepsChooseCard(PhysicalCardImpl card) { FreepsChooseCards(card); }
	default void FreepsChooseCards(PhysicalCardImpl...cards) {
		if(GetChoiceCount(FreepsGetBPChoices()) > 0) {
			ChooseCardBPFromSelection(P1(), cards);
		}
		else {
			ChooseCards(P1(), cards);
		}
	}
	default void ShadowChooseCard(PhysicalCardImpl card) { ShadowChooseCards(card); }
	default void ShadowChooseCards(PhysicalCardImpl...cards) {
		if(GetChoiceCount(ShadowGetBPChoices()) > 0) {
			ChooseCardBPFromSelection(P2(), cards);
		}
		else {
			ChooseCards(P2(), cards);
		}
	}
	default void ChooseCards(String player, PhysicalCardImpl...cards) {
		String[] ids = new String[cards.length];

		for(int i = 0; i < cards.length; i++)
		{
			ids[i] = String.valueOf(cards[i].getCardId());
		}

		PlayerDecided(player, String.join(",", ids));
	}

	default int FreepsGetCardChoiceCount() { return FreepsGetCardChoices().size(); }
	default int ShadowGetCardChoiceCount() { return ShadowGetCardChoices().size(); }

	default void FreepsChooseCardBPFromSelection(PhysicalCardImpl...cards) {
		ChooseCardBPFromSelection(P1(), cards);
	}
	default void ShadowChooseCardBPFromSelection(PhysicalCardImpl...cards) {
		ChooseCardBPFromSelection(P2(), cards);
	}
	default void FreepsChooseCardBPFromSelection(String...bpids) {
		ChooseCardBPFromSelection(P1(), bpids);
	}
	default void ShadowChooseCardBPFromSelection(String...bpids) {
		ChooseCardBPFromSelection(P2(), bpids);
	}

	/**
	 * Causes the given player to issue a decision response composed of a comma-separated list of the provided card
	 * blueprint IDs. This will only succeed if being used to target currently out-of-play cards such as when selecting
	 * cards from the reserve deck; it will not work if being presented with a choice of in-play cards to target (such
	 * as when choosing active cards to target for a card effect).
	 * @param player The player to issue a decision for.
	 * @param cards The cards to include in the decision response.
	 * IDs.
	 */
	default void ChooseCardBPFromSelection(String player, PhysicalCardImpl...cards) {
		String[] choices = GetAwaitingDecisionParam(player,"blueprintId");
		ArrayList<String> bps = new ArrayList<>();
		ArrayList<PhysicalCardImpl> found = new ArrayList<>();

		for(int i = 0; i < choices.length; i++)
		{
			for(PhysicalCardImpl card : cards)
			{
				if(found.contains(card))
					continue;

				if(card.getBlueprintId().equals(choices[i]))
				{
					// I have no idea why the spacing is required, but the BP parser skips to the fourth position
					bps.add("    " + i);
					found.add(card);
					break;
				}
			}
		}

		PlayerDecided(player, String.join(",", bps));
		//ChooseCardBPFromSelection(player, Arrays.stream(cards).distinct().map(PhysicalCardImpl::getBlueprintId).toArray(String[]::new));
	}


	/**
	 * Causes the given player to issue a decision response composed of a comma-separated list of the provided card
	 * blueprint IDs. This will only succeed if being used to target currently out-of-play cards such as when selecting
	 * cards from the reserve deck; it will not work if being presented with a choice of in-play cards to target (such
	 * as when choosing active cards to target for a card effect).
	 * @param player The player to issue a decision for.
	 * @param bpids The card blueprint IDs to include in the decision response.
	 * IDs.
	 */
	default void ChooseCardBPFromSelection(String player, String...bpids) {
		String[] choices = GetAwaitingDecisionParam(player,"blueprintId");
		ArrayList<String> bps = new ArrayList<>();
		ArrayList<String> found = new ArrayList<>();

		for(int i = 0; i < choices.length; i++)
		{
			for(String card : bpids)
			{
				if(found.contains(card))
					continue;
				if(card.equals(choices[i]))
				{
					// I have no idea why the spacing is required, but the BP parser skips to the fourth position
					bps.add("    " + i);
					found.add(card);
					break;
				}
			}
		}

		PlayerDecided(player, String.join(",", bps));
	}

	default boolean FreepsHasCardChoiceAvailable(PhysicalCardImpl card, boolean selectable) {
		return HasCardChoiceAvailable(P1(), card, selectable);
	}

	default boolean ShadowHasCardChoiceAvailable(PhysicalCardImpl card, boolean selectable) {
		return HasCardChoiceAvailable(P2(), card, selectable);
	}

	default boolean FreepsHasCardChoiceAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(!HasCardChoiceAvailable(P1(), card))
				return false;
		}
		return true;
	}
	default boolean ShadowHasCardChoiceAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(!HasCardChoiceAvailable(P2(), card))
				return false;
		}
		return true;
	}


	default boolean FreepsHasCardChoiceNotAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(HasCardChoiceAvailable(P1(), card))
				return false;
		}
		return true;
	}
	default boolean ShadowHasCardChoiceNotAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(HasCardChoiceAvailable(P2(), card))
				return false;
		}
		return true;
	}

	/**
	 * Checks whether the given card is one of the options being presented to the player.  Will work as either a blueprint
	 * choice (for e.g. selecting from the reserve deck) or as a physical id choice (for selecting among cards on the
	 * table).  If the choice presents selectable flags, then this will only return true if the card is selectable.
	 * @param player The player currently presented with a decision
	 * @param card The card to search for
	 * @return True if the given card is one of the options presented to the player, false otherwise.
	 */
	default boolean HasCardChoiceAvailable(String player, PhysicalCardImpl card) { return HasCardChoiceAvailable(player, card, true); }

	default boolean FreepsHasBPChoiceAvailable(String blueprintId) { return HasBPChoiceAvailable(P1(), blueprintId, true); }
	default boolean FreepsHasBPChoiceAvailable(String blueprintId, boolean selectable) { return HasBPChoiceAvailable(P1(), blueprintId, selectable); }
	default boolean ShadowHasBPChoiceAvailable(String blueprintId) { return HasBPChoiceAvailable(P2(), blueprintId, true); }
	default boolean ShadowHasBPChoiceAvailable(String blueprintId, boolean selectable) { return HasBPChoiceAvailable(P2(), blueprintId, selectable); }

	/**
	 * Checks whether a card with the given blueprint ID is among the selectable options presented to the player.
	 * Useful when the card was dynamically created (e.g. from a booster pack) and no PhysicalCardImpl reference is available.
	 * @param player The player currently presented with a decision
	 * @param blueprintId The blueprint ID to search for (e.g. "1_174")
	 * @param selectable True if we are looking for a selectable card, false otherwise
	 * @return True if a card with that blueprint ID is presented with the matching selectable state, false otherwise.
	 */
	default boolean HasBPChoiceAvailable(String player, String blueprintId, boolean selectable) {
		String selectString = selectable ? "true" : "false";
		String[] selectables = GetADParam(player, "selectable");
		String[] choices = GetADParam(player, "blueprintId");
		if (choices != null) {
			for (int i = 0; i < choices.length; i++) {
				if (blueprintId.equals(choices[i])) {
					if (selectables != null) {
						return selectables[i].equals(selectString);
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the given card is one of the options being presented to the player.  Will work as either a blueprint
	 * choice (for e.g. selecting from the reserve deck) or as a physical id choice (for selecting among cards on the
	 * table).  The choice will be filtered to match the provided selectable state.
	 * @param player The player currently presented with a decision
	 * @param card The card to search for
	 * @param selectable True if we are looking for a selectable card, false otherwise
	 * @return True if the given card is one of the options presented to the player, false otherwise.
	 */
	default boolean HasCardChoiceAvailable(String player, PhysicalCardImpl card, boolean selectable) {
		String selectString = selectable ? "true" : "false";
		String[] selectables = GetADParam(player,"selectable");

		String[] choices = GetADParam(player,"blueprintId");
		if(choices != null) {
			for (int i = 0; i < choices.length; i++) {
				String choice = choices[i];
				if (card.getBlueprintId().equals(choice)) {
					if(selectables != null) {
						return selectables[i].equals(selectString);
					}
					return true;
				}
			}
			return false;
		}

		choices = GetADParam(player,"cardId");
		if(choices != null) {
			for (int i = 0; i < choices.length; i++) {
				String choice = choices[i];
				if (card.getCardId() == Integer.parseInt(choice)) {
					if(selectables != null) {
						return selectables[i].equals(selectString);
					}

					return true;
				}
			}
			return false;
		}


		return false;
	}

	/**
	 * Causes the Free Peoples player to issue a decision response composed of a comma-separated list of the provided
	 * card IDs.  This is used when e.g. the player must choose one or more targets for an effect.  This will only
	 * succeed if being used to target currently live cards; it will not work if being presented with a choice of
	 * out-of-play cards (such as when choosing from the reserve deck).
	 * @param cards The cards to include in the decision response.
	 */
	default void FreepsChooseCardIDFromSelection(PhysicalCardImpl...cards) {
		ChooseCardIDFromSelection(P1(), cards);
	}
	/**
	 * Causes the Shadow player to issue a decision response composed of a comma-separated list of the provided
	 * card IDs.  This is used when e.g. the player must choose one or more targets for an effect.  This will only
	 * succeed if being used to target currently live cards; it will not work if being presented with a choice of
	 * out-of-play cards (such as when choosing from the reserve deck).
	 * @param cards The cards to include in the decision response.
	 */
	default void ShadowChooseCardIDFromSelection(PhysicalCardImpl...cards) {
		ChooseCardIDFromSelection(P2(), cards);
	}

	/**
	 * Causes the given player to issue a decision response composed of a comma-separated list of the provided card IDs.
	 * This will only succeed if being used to target currently live cards; it will not work if being presented with a
	 * choice of out-of-play cards (such as when choosing from the reserve deck).
	 * @param player The player to issue a decision for.
	 * @param cards The cards to include in the decision response.
	 */
	default void ChooseCardIDFromSelection(String player, PhysicalCardImpl...cards) {
		AwaitingDecision decision = userFeedback().getAwaitingDecision(player);
		//PlayerDecided(player, "" + card.getCardId());

		String[] choices = GetAwaitingDecisionParam(player,"cardId");
		ArrayList<String> ids = new ArrayList<>();
		ArrayList<PhysicalCardImpl> found = new ArrayList<>();

		for (String choice : choices) {
			for (PhysicalCardImpl card : cards) {
				if (found.contains(card))
					continue;

				if (("" + card.getCardId()).equals(choice)) {
					ids.add(choice);
					found.add(card);
					break;
				}
			}
		}

		PlayerDecided(player, String.join(",", ids));
	}
}
