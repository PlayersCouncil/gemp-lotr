package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.OverrideUniquenessModifier;
import org.json.simple.JSONObject;

public class OverrideUniqueness implements ModifierSourceProducer {
	@Override
	public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object, "filter", "uniqueness", "loosen", "requires");

		final String filter = FieldUtils.getString(object.get("filter"), "filter");
		final int uniqueness = FieldUtils.getInteger(object.get("uniqueness"), "uniqueness");
		// Overrides restrict by default, and a restricting override beats any loosening one.  A card whose text
		// makes its targets less unique must say so explicitly.
		final boolean loosen = FieldUtils.getBoolean(object.get("loosen"), "loosen", false);

		if (uniqueness < 1 || uniqueness > 4)
			throw new InvalidCardDefinitionException("Uniqueness must be between 1 and 4");

		final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

		final JSONObject[] requirementsArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
		final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requirementsArray, environment);

		return actionContext ->
				new OverrideUniquenessModifier(actionContext.getSource(),
						filterableSource.getFilterable(actionContext),
						RequirementCondition.createCondition(requirements, actionContext),
						uniqueness,
						loosen);
	}
}
