package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class ThreatLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount");
        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return (actionContext) -> {
            final int amount = amountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

			return PlayConditions.getThreatLimit(actionContext.getGame()) >= amount;
		};
    }
}
