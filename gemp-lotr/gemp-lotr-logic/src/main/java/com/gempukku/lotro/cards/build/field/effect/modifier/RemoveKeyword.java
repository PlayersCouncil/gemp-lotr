package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.RemoveKeywordModifier;
import org.json.simple.JSONObject;

public class RemoveKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "keyword");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        var keywords = FieldUtils.getEnumArray(Keyword.class, object.get("keyword"), "keyword");

        if (keywords.isEmpty())
            throw new InvalidCardDefinitionException("'keyword' is required for RemoveKeyword modifier.");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new RemoveKeywordModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext), keywords);
            }
        };
    }
}
