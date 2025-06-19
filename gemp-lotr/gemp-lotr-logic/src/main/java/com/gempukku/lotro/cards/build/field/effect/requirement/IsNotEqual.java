package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class IsNotEqual implements RequirementProducer {
	@Override
	public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");

		final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), environment);
		final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), environment);

		return actionContext -> {
			final int first = firstNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
			final int second = secondNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
			return first != second;
		};
	}
}