package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.json.simple.JSONObject;

public class AddRace implements ModifierSourceProducer {
	@Override
	public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object, "filter", "race", "requires");

		final String filter = FieldUtils.getString(object.get("filter"), "filter");
		final Race race = FieldUtils.getEnum(Race.class, object.get("race"), "race");

		final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

		final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
		final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

		return actionContext ->
				new IsAdditionalRaceModifier(actionContext.getSource(),
						filterableSource.getFilterable(actionContext),
						RequirementCondition.createCondition(requirements, actionContext),
						race);
	}
}
