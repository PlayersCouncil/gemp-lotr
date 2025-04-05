package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

public class HasMemory implements RequirementProducer {

	@Override
	public Requirement getPlayRequirement(
			JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object, "memory");

		final String memory = FieldUtils.getString(object.get("memory"), "memory");

		return actionContext -> {
			try {
				return actionContext.getValueFromMemory(memory) != null && actionContext.getCardsFromMemory(
						memory) != null;
			}
			catch (IllegalArgumentException ex) {
				return false;
			}
		};
	}
}


