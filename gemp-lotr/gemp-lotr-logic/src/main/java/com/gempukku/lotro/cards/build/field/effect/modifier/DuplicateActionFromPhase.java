package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.modifiers.AddActionToCardModifier;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DuplicateActionFromPhase implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "oldPhase", "newPhase", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
		final String[] oldphaseArray = FieldUtils.getStringArray(object.get("oldPhase"), "oldPhase");
		final String[] newphaseArray = FieldUtils.getStringArray(object.get("newPhase"), "newPhase");
		final JSONObject[] requiresArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

		if (oldphaseArray.length == 0)
			throw new InvalidCardDefinitionException("'oldPhase' is required on DuplicateActionFromPhase modifier");

		if (newphaseArray.length == 0)
			throw new InvalidCardDefinitionException("'newPhase' is required on DuplicateActionFromPhase modifier");

		final HashSet<Phase> oldPhases = new HashSet<>();
		final HashSet<Phase> newPhases = new HashSet<>();
		final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
		final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requiresArray, environment);

		Arrays.stream(oldphaseArray).forEach(x -> oldPhases.add(Phase.findPhase(x)));
		Arrays.stream(newphaseArray).forEach(x -> newPhases.add(Phase.findPhase(x)));


        return actionContext -> {

			var targetPhaseAndRequirements = new AndCondition(RequirementCondition.createCondition(requirements, actionContext),
					game -> newPhases.contains(game.getGameState().getCurrentPhase()));

			return new AddActionToCardModifier(actionContext.getSource(), targetPhaseAndRequirements, actionContext.getPerformingPlayer(),
					filterableSource.getFilterable(actionContext)) {
				@Override
				protected List<? extends ActivateCardAction> createExtraPhaseActions(LotroGame game, PhysicalCard card) {
					LinkedList<ActivateCardAction> result = new LinkedList<>();

					for (var phase : oldPhases) {
						var currentActualPhase = game.getGameState().setFakePhase(phase);
						var actions = card.getBlueprint().getPhaseActionsInPlay(card.getOwner(), game, card);
						game.getGameState().setCurrentPhase(currentActualPhase);

						if(actions != null) {
							result.addAll(actions);
						}
					}

					return result;
				}
			};
		};
    }
}
