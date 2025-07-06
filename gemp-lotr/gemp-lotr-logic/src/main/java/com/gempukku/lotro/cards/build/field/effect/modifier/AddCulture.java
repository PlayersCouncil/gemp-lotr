package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.json.simple.JSONObject;

public class AddCulture implements ModifierSourceProducer {
	@Override
	public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object, "filter", "culture", "requires");

		final String filter = FieldUtils.getString(object.get("filter"), "filter");
		final Culture culture = FieldUtils.getEnum(Culture.class, object.get("culture"), "culture");

		final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

		final JSONObject[] requirementsArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
		final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requirementsArray, environment);

		return actionContext ->
				new IsAdditionalCultureModifier(actionContext.getSource(),
						filterableSource.getFilterable(actionContext),
						RequirementCondition.createCondition(requirements, actionContext),
						culture);
	}
}
