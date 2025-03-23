package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.GameUtils;
import org.json.simple.JSONObject;

public class SiteAvailableToControl implements RequirementProducer {

	@Override
	public Requirement getPlayRequirement(
			JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object);

		return (actionContext) -> GameUtils.anySiteAvailableToControl(actionContext.getGame());
	}
}
