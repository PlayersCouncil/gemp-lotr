package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.logic.modifiers.BearExtraItemsModifier;
import org.json.simple.JSONObject;

public class CanBearExtraItems implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "itemclass", "amount", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final var classes = FieldUtils.getEnumArray(PossessionClass.class, object.get("itemclass"), "itemclass");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), 1, environment);
        final JSONObject[] requiresArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requiresArray, environment);

        return (actionContext) -> new BearExtraItemsModifier(actionContext.getSource(),
				filterableSource.getFilterable(actionContext),
				classes,
				valueSource.getEvaluator(actionContext),
				RequirementCondition.createCondition(requirements, actionContext));
    }
}
