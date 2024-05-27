package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.logic.modifiers.AddSignetModifier;
import org.json.simple.JSONObject;

public class AddSignet implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "signet", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final Signet signet = FieldUtils.getEnum(Signet.class, object.get("signet"), "signet");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext ->
                new AddSignetModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext),
                        signet);
    }
}
