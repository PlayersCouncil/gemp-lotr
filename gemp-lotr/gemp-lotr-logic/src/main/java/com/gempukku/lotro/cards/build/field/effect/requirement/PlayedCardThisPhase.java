package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

public class PlayedCardThisPhase implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "min", "max");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final ValueSource minSource = ValueResolver.resolveEvaluator(object.get("min"), 1, environment);
        final ValueSource maxSource = ValueResolver.resolveEvaluator(object.get("max"), 100, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            var filterable = Filters.changeToFilter(filterableSource.getFilterable(actionContext));
            int min = minSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            int total = 0;
            LotroGame game = actionContext.getGame();
            for (EffectResult effectResult : game.getActionsEnvironment().getPhaseEffectResults()) {
                if (effectResult instanceof PlayCardResult playResult) {
                    if (filterable.accepts(game, playResult.getPlayedCard())) {
                        ++total;
                    }
                }
            }

            return total >= min && total <= max;
        };
    }
}
