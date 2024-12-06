package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.OverwhelmedByMultiplierModifier;
import org.json.simple.JSONObject;

public class CantBeOverwhelmedMultiplier implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "multiplier");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 3);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
                    @Override
                    public Modifier getModifier(ActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        return new OverwhelmedByMultiplierModifier(actionContext.getSource(), filterable,
                                RequirementCondition.createCondition(requirements, actionContext),
                                multiplier);
                    }
        };
    }
}
