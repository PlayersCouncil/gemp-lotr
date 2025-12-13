package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.DuplicateGameTextModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class DuplicateGameText implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "from", "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filterDef = FieldUtils.getString(object.get("filter"), "filter");
        final String fromDef = FieldUtils.getString(object.get("from"), "from");

        if(!filterDef.equals("self") && !filterDef.startsWith("memory(")) {
            throw new InvalidCardDefinitionException("Only 'self' and 'memory' are supported filters for DuplicateGameText.");
        }

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filterDef, environment);
        final FilterableSource fromSource = environment.getFilterFactory().generateFilter(fromDef, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new DuplicateGameTextModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext),
                        fromSource.getFilterable(actionContext)
                );
            }
        };
    }
}
