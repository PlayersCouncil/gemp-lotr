package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.logic.modifiers.CancelKeywordBonusTargetModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CancelKeywordBonus implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "from", "requires", "keyword");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String from = FieldUtils.getString(object.get("from"), "from", "any");

        Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource fromFilterableSource = environment.getFilterFactory().generateFilter(from, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new CancelKeywordBonusTargetModifier(actionContext.getSource(), keyword,
                        RequirementCondition.createCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext),
                        fromFilterableSource.getFilterable(actionContext));
            }
        };
    }
}
