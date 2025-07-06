package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.modifiers.CantTakeMoreThanXWoundsModifier;
import org.json.simple.JSONObject;

public class CantTakeMoreWoundsThan implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "phase", "requires", "amount");

        final ValueSource objectSource = ValueResolver.resolveEvaluator(object.get("amount"), 1, environment);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantTakeMoreThanXWoundsModifier(actionContext.getSource(), phase,
                objectSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null),
                RequirementCondition.createCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
