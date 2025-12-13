package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.CantBePlayedOnModifier;
import org.json.simple.JSONObject;

public class CantPlayCardsOn implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "on", "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String onFilter = FieldUtils.getString(object.get("on"), "on");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantBePlayedOnModifier(actionContext.getSource(),
                RequirementCondition.createCondition(requirements, actionContext),
                onFilterableSource.getFilterable(actionContext), filterableSource.getFilterable(actionContext));
    }
}
