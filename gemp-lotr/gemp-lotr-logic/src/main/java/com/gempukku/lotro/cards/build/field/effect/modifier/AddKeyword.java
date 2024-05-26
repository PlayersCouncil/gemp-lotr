package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class AddKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "keyword", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String keywordString = FieldUtils.getString(object.get("keyword"), "keyword");
        if (keywordString == null)
            throw new InvalidCardDefinitionException("Keyword needs to be defined for 'addKeyword'");

        final String[] keywordSplit = keywordString.split("\\+");
        Keyword keyword = FieldUtils.getEnum(Keyword.class, keywordSplit[0], "keyword");
        int value = 1;
        if (keywordSplit.length == 2)
            value = Integer.parseInt(keywordSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), value, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
                    @Override
                    public Modifier getModifier(ActionContext actionContext) {
                        final Evaluator evaluator = amount.getEvaluator(actionContext);
                        return new AddKeywordModifier(actionContext.getSource(),
                                filterableSource.getFilterable(actionContext),
                                new RequirementCondition(requirements, actionContext), keyword, evaluator);
                    }
        };
    }
}
