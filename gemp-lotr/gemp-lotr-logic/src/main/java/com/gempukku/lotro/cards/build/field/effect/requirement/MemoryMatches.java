package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

import java.util.Collection;

public class MemoryMatches implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memory", "filter", "count");

        final String memory = FieldUtils.getString(object.get("memory"), "memory");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        String count = FieldUtils.getString(object.get("count"), "count", "1");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        ValueSource countSource = ValueResolver.resolveEvaluator(count, environment);

        return (actionContext) -> {
            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            Evaluator countEvaluator = countSource.getEvaluator(actionContext);
            return Filters.filter(cardsFromMemory, actionContext.getGame(), filterable).size() >= countEvaluator.evaluateExpression(actionContext.getGame(), null);
        };
    }
}
