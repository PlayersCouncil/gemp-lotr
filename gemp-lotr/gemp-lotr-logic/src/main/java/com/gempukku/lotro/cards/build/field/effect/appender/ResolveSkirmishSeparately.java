package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ResolveSkirmishSeparatelyEffect;
import com.gempukku.lotro.logic.timing.DoNothingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ResolveSkirmishSeparately implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");

        if (filter == null)
            throw new InvalidCardDefinitionException("'filter' is required for ResolveSkirmishSeparately effect.");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
            CardResolver.resolveCard("choose(any)",
                    (actionContext) -> Filters.inSkirmishAgainst(filterableSource.getFilterable(actionContext)),
                    "_temp", "you", "Choose minion to resolve skirmish against first", environment)
            );
        result.addEffectAppender(
            new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final PhysicalCard firstMinion = actionContext.getCardFromMemory("_temp");
                    if (firstMinion != null)
                        return new ResolveSkirmishSeparatelyEffect(firstMinion, filterableSource.getFilterable(actionContext),
                                Filters.inSkirmish);
                    else
                        return new DoNothingEffect();
                }
            });

        return result;
    }

}
