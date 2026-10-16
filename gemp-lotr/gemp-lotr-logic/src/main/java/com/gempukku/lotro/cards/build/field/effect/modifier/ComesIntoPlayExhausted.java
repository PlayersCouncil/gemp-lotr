package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.ComesIntoPlayExhaustedModifier;
import org.json.simple.JSONObject;

/**
 * "Each <filter> comes into play exhausted": the character is placed exhausted, before its play is answered, with
 * wounds that are no exertions (Q-0302).  `requires` is asked as the card is placed ("to site 4 or higher").
 */
public class ComesIntoPlayExhausted implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new ComesIntoPlayExhaustedModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                RequirementCondition.createCondition(requirements, actionContext));
    }
}
