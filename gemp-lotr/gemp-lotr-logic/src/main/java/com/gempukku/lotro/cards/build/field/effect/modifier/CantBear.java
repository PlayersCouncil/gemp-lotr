package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.CantBearModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantBear implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "cardFilter", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String cardFilter = FieldUtils.getString(object.get("cardFilter"), "cardFilter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource cardFilterSource = environment.getFilterFactory().generateFilter(cardFilter, environment);

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new CantBearModifier(actionContext.getSource(), filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext),
                        cardFilterSource.getFilterable(actionContext));
            }
        };
    }
}
