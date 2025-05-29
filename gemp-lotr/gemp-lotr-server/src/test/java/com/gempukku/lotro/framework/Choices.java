package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Decisions will always come with at least one choice, even if that single choice is "pass".  These functions will
 * let you inspect the choices available and offer shortcuts for e.g. selecting a physical card which you have
 * previously stored.
 */
public interface Choices extends Decisions {

	default void FreepsDeclineChoosing() { PlayerDecided(P1, ""); }
	default void ShadowDeclineChoosing() { PlayerDecided(P2, ""); }

	/**
	 * Determines whether the Free Peoples player has any choices on the current decision whose description matches the
	 * provided search text.
	 * @param choice The search text the choice description must contain.
	 * @return True if the Free Peoples player has an active decision with a choice description matching the given text.
	 */
	default Boolean FreepsChoiceAvailable(String choice) { return ChoiceAvailable(P1, choice); }
	/**
	 * Determines whether the Shadow player has any choices on the current decision whose description matches the
	 * provided search text.
	 * @param choice The search text the choice description must contain.
	 * @return True if the Shadow player has an active decision with a choice description matching the given text.
	 */
	default Boolean ShadowChoiceAvailable(String choice) { return ChoiceAvailable(P2, choice); }
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
		if(FreepsGetChoiceCount() > 0) {
			FreepsChooseOption(choice);
		}
		else {
			PlayerDecided(P1, choice);
		}
	}
	/**
	 * Causes the Free Peoples player to choose the given options.  This will automatically format the response to contain
	 * all the provided options in a comma-separated list.
	 * @param choices The choices the player wishes to make.
	 */
	default void FreepsChoose(String...choices) { PlayerDecided(
			P1, String.join(",", choices)); }

	/**
	 * Causes the Shadow player to choose the given option.
	 * This is a catch-all that either selects the provided choice if part of a multiple choice decision, or else
	 * falls back on providing the provided choice as a top-level response to the current decision.
	 * @param choice The choice (or decision response)
	 */
	default void ShadowChoose(String choice) {
		if(ShadowGetChoiceCount() > 0) {
			ShadowChooseOption(choice);
		}
		else {
			PlayerDecided(P2, choice);
		}
	}
	/**
	 * Causes the Shadow player to choose the given options.  This will automatically format the response to contain
	 * all the provided options in a comma-separated list.
	 * @param choices The choices the player wishes to make.
	 */
	default void ShadowChoose(String...choices) { PlayerDecided(
			P2, String.join(",", choices)); }




	/**
	 * Causes the Free Peoples player to return a canned "Yes" response to a Yes or No question.
	 */
	default void FreepsChooseYes() { ChooseOption(P1, "Yes"); }
	/**
	 * Causes the Shadow player to return a canned "Yes" response to a Yes or No question.
	 */
	default void ShadowChooseYes() { ChooseOption(P2, "Yes"); }
	/**
	 * Causes the Free Peoples player to return a canned "No" response to a Yes or No question.
	 */
	default void FreepsChooseNo() { ChooseOption(P1, "No"); }
	/**
	 * Causes the Shadow player to return a canned "No" response to a Yes or No question.
	 */
	default void ShadowChooseNo() { ChooseOption(P2, "No"); }

	default void FreepsChooseOption(String option) { ChooseOption(P1, option); }
	default void ShadowChooseOption(String option) { ChooseOption(P2, option); }
	default void ChooseOption(String playerID, String option) {
		ChooseAction(playerID, "results", option);
	}

	default void FreepsChooseAction(String paramName, String option) { ChooseAction(P1, paramName, option); }
	default void FreepsChooseAction(String option) { ChooseAction(P1, "actionText", option); }
	default void ShadowChooseAction(String paramName, String option) { ChooseAction(P2, paramName, option); }
	default void ShadowChooseAction(String option) { ChooseAction(P2, "actionText", option); }
	default void ChooseAction(String playerID, String paramName, String option) {
		List<String> choices = GetADParamAsList(playerID, paramName);
		for(String choice : choices){
			if(option == null && choice == null // This only happens when a rule is the source of an action
					|| choice.toLowerCase().contains(option.toLowerCase())) {
				PlayerDecided(playerID, String.valueOf(choices.indexOf(choice)));
				return;
			}
		}
		//couldn't find an exact match, so maybe it's a direct index:
		PlayerDecided(playerID, option);
	}

	default void FreepsChooseAny() {
		if (GetChoiceCount(FreepsGetActionChoices()) > 0){
			ChooseAction(P1, "actionId", FreepsGetActionChoices().getFirst());
		}
		else if(FreepsGetBPChoices().size() > 1) {
			ChooseCardBPFromSelection(P1, FreepsGetBPChoices().getFirst());
		}
		else {
			FreepsResolveRuleFirst();
		}
	}

    default void FreepsResolveRuleFirst() {
		FreepsResolveActionOrder(GetADParamAsList(P1, "actionText").getFirst());
	}
    default void FreepsResolveActionOrder(String option) {
		ChooseAction(P1, "actionText", option);
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
		return GetADParamEqualsCount(P1, "selectable", "true");
	}

	/**
	 * @return Gets how many of the currently displayed cards are selectable for a Shadow decision.
	 */
	default int ShadowGetSelectableCount() {
		return GetADParamEqualsCount(P2, "selectable", "true");
	}

	default boolean FreepsHasBPChoice(PhysicalCardImpl card) { return FreepsGetBPChoices().contains(card.getBlueprintId()); }
	default boolean ShadowHasBPChoice(PhysicalCardImpl card) { return ShadowGetBPChoices().contains(card.getBlueprintId()); }
	default List<String> FreepsGetBPChoices() { return GetADParamAsList(P1, "blueprintId"); }
	default List<String> ShadowGetBPChoices() { return GetADParamAsList(P2, "blueprintId"); }
	default List<String> FreepsGetActionChoices() { return GetADParamAsList(P1, "actionId"); }
	default List<String> ShadowGetActionChoices() { return GetADParamAsList(P2, "actionId"); }
	default List<String> FreepsGetMultipleChoices() { return GetADParamAsList(P1, "results"); }
	default List<String> ShadowGetMultipleChoices() { return GetADParamAsList(P2, "results"); }
	default List<String> FreepsGetCardChoices() { return GetADParamAsList(P1, "cardId"); }
	default List<String> ShadowGetCardChoices() { return GetADParamAsList(P2, "cardId"); }
	default int FreepsGetChoiceCount() { return GetChoiceCount(FreepsGetMultipleChoices()); }
	default int ShadowGetChoiceCount() { return GetChoiceCount(ShadowGetMultipleChoices()); }

	default int GetChoiceCount(List<String> list) {
		if(list == null)
			return 0;
		return list.size();
	}

	default List<String> FreepsGetFreepsAssignmentTargets() { return GetADParamAsList(P1, "freeCharacters"); }
	default List<String> FreepsGetShadowAssignmentTargets() { return GetADParamAsList(P1, "minions"); }

	default List<String> ShadowGetFreepsAssignmentTargets() { return GetADParamAsList(P2, "freeCharacters"); }
	default List<String> ShadowGetShadowAssignmentTargets() { return GetADParamAsList(P2, "minions"); }

	default List<String> FreepsGetADParamAsList(String paramName) { return GetADParamAsList(P1, paramName); }
	default List<String> ShadowGetADParamAsList(String paramName) { return GetADParamAsList(P2, paramName); }
	default List<String> GetADParamAsList(String playerID, String paramName) {
		var paramList = GetAwaitingDecisionParam(playerID, paramName);
		if(paramList == null)
			return null;

		return Arrays.asList(paramList);
	}

	default int GetADParamEqualsCount(String playerID, String paramName, String value) {
		return (int) Arrays.stream(GetAwaitingDecisionParam(playerID, paramName)).filter(s -> s.equals(value)).count();
	}
	default String[] FreepsGetADParam(String paramName) { return GetAwaitingDecisionParam(P1, paramName); }
	default String[] ShadowGetADParam(String paramName) { return GetAwaitingDecisionParam(P2, paramName); }
	default String FreepsGetFirstADParam(String paramName) { return GetAwaitingDecisionParam(P1, paramName)[0]; }
	default String ShadowGetFirstADParam(String paramName) { return GetAwaitingDecisionParam(P2, paramName)[0]; }
	default String[] GetAwaitingDecisionParam(String playerID, String paramName) {
		var decision = userFeedback().getAwaitingDecision(playerID);
		return decision.getDecisionParameters().get(paramName);
	}

	default Map<String, String[]> GetAwaitingDecisionParams(String playerID) {
		var decision = userFeedback().getAwaitingDecision(playerID);
		return decision.getDecisionParameters();
	}




	default void FreepsChooseAnyCard() { FreepsChoose(FreepsGetCardChoices().getFirst()); }
	default void ShadowChooseAnyCard() { ShadowChoose(ShadowGetCardChoices().getFirst()); }

	default void FreepsChooseCard(PhysicalCardImpl card) { FreepsChooseCards(card); }
	default void FreepsChooseCards(PhysicalCardImpl...cards) {
		if(GetChoiceCount(FreepsGetBPChoices()) > 0) {
			ChooseCardBPFromSelection(P1, cards);
		}
		else {
			ChooseCards(P1, cards);
		}
	}
	default void ShadowChooseCard(PhysicalCardImpl card) { ShadowChooseCards(card); }
	default void ShadowChooseCards(PhysicalCardImpl...cards) {
		if(GetChoiceCount(ShadowGetBPChoices()) > 0) {
			ChooseCardBPFromSelection(P2, cards);
		}
		else {
			ChooseCards(P2, cards);
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

	default boolean FreepsCanChooseCharacter(PhysicalCardImpl card) { return FreepsGetCardChoices().contains(String.valueOf(card.getCardId())); }
	default boolean ShadowCanChooseCharacter(PhysicalCardImpl card) { return ShadowGetCardChoices().contains(String.valueOf(card.getCardId())); }


	default int FreepsGetCardChoiceCount() { return FreepsGetCardChoices().size(); }
	default int ShadowGetCardChoiceCount() { return ShadowGetCardChoices().size(); }

	default void FreepsChooseCardBPFromSelection(PhysicalCardImpl...cards) {
		ChooseCardBPFromSelection(P1, cards);
	}
	default void ShadowChooseCardBPFromSelection(PhysicalCardImpl...cards) {
		ChooseCardBPFromSelection(P2, cards);
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

	default boolean FreepsHasCardChoiceAvailable(PhysicalCardImpl card) {
		return HasCardChoiceAvailable(P1, card);
	}
	default boolean ShadowHasCardChoiceAvailable(PhysicalCardImpl card) {
		return HasCardChoiceAvailable(P2, card);
	}

	default boolean FreepsHasCardChoicesAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(!HasCardChoiceAvailable(P1, card))
				return false;
		}
		return true;
	}
	default boolean ShadowHasCardChoicesAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(!HasCardChoiceAvailable(P2, card))
				return false;
		}
		return true;
	}

	default boolean FreepsHasCardChoiceNotAvailable(PhysicalCardImpl card) {
		return !HasCardChoiceAvailable(P1, card);
	}
	default boolean ShadowHasCardChoicenotAvailable(PhysicalCardImpl card) {
		return !HasCardChoiceAvailable(P2, card);
	}

	default boolean FreepsHasCardChoicesNotAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(HasCardChoiceAvailable(P1, card))
				return false;
		}
		return true;
	}
	default boolean ShadowHasCardChoicesNotAvailable(PhysicalCardImpl...cards) {
		for(var card : cards) {
			if(HasCardChoiceAvailable(P2, card))
				return false;
		}
		return true;
	}

	default boolean HasCardChoiceAvailable(String player, PhysicalCardImpl card) {

		String[] choices = GetAwaitingDecisionParam(player,"blueprintId");
		if(choices != null) {
			for (String choice : choices) {
				if (card.getBlueprintId().equals(choice))
					return true;
			}
			return false;
		}

		choices = GetAwaitingDecisionParam(player,"cardId");
		if(choices != null) {
			for (String choice : choices) {
				if (card.getCardId() == Integer.parseInt(choice))
					return true;
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
		ChooseCardIDFromSelection(P1, cards);
	}
	/**
	 * Causes the Shadow player to issue a decision response composed of a comma-separated list of the provided
	 * card IDs.  This is used when e.g. the player must choose one or more targets for an effect.  This will only
	 * succeed if being used to target currently live cards; it will not work if being presented with a choice of
	 * out-of-play cards (such as when choosing from the reserve deck).
	 * @param cards The cards to include in the decision response.
	 */
	default void ShadowChooseCardIDFromSelection(PhysicalCardImpl...cards) {
		ChooseCardIDFromSelection(P2, cards);
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
