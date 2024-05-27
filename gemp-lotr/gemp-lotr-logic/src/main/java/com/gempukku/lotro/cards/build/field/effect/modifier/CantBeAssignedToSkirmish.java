package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedToSkirmishModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantBeAssignedToSkirmish implements ModifierSourceProducer {

    @Override
    public ModifierSource getModifierSource(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new CantBeAssignedToSkirmishModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
            }
        };
    }
}
