package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.CantHealModifier;
import org.json.simple.JSONObject;

public class CantHeal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter", "character");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantHealModifier(actionContext.getSource(),
                RequirementCondition.createCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
