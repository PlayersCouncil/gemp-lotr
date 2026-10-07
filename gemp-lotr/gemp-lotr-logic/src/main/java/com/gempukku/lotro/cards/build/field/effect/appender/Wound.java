package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Wound implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "times", "select", "memorize", "player");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final ValueSource times = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        // For telling beforehand whom a cost is certain to wound; a choice made by another player is not forecast.
        final FilterableSource chosenFrom = "you".equalsIgnoreCase(player) && select != null && select.startsWith("choose(") && select.endsWith(")")
                ? environment.getFilterFactory().generateFilter(select.substring(select.indexOf("(") + 1, select.lastIndexOf(")")), environment)
                : null;

        final EffectAppender cardResolver = CardResolver.resolveCards(select,
                        SpotOverride.NONE,
                        (actionContext) -> Filters.canTakeWounds(actionContext.getSource(), 1),
                        (actionContext) -> Filters.canTakeWounds(actionContext.getSource(), times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)),
                        valueSource, memory, player, "Choose cards to wound", environment);

        MultiEffectAppender result = new MultiEffectAppender() {
            // With nobody who can take a wound it does nothing; a wound is never a matter of one skirmish only.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.givenTo(cardResolver.forecast(actionContext), false);
            }

            // As a cost: each character who is certain to be wounded takes a wound for each time.
            @Override
            public void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
                final int timesCount = times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (timesCount <= 0)
                    return;
                for (PhysicalCard wounded : EffectUtils.getCardsCertainToBeReached(select, chosenFrom, valueSource,
                        Filters.canTakeWounds(actionContext.getSource(), timesCount), actionContext))
                    tally.merge(wounded, timesCount, Integer::sum);
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        final int timesCount = times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        List<Effect> result = new LinkedList<>();
                        for(var card : cardsFromMemory) {
                            for (int i = 0; i < timesCount; i++) {
                                result.add(new WoundCharactersEffect(actionContext.getSource(), card));
                            }
                        }

                        return result;
                    }
                });

        return result;
    }


}
