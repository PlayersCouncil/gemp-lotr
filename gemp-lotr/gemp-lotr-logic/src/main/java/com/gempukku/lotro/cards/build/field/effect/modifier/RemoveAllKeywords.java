package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.logic.modifiers.LoseAllKeywordsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class RemoveAllKeywords implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "keyword");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new LoseAllKeywordsModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext));
            }
        };
    }
}
