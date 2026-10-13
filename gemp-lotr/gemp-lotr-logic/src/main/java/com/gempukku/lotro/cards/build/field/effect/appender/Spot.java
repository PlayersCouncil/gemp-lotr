package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

public class Spot implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "select", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose card to spot");

        final boolean selecting = !StringUtils.isEmpty(select);

        if(StringUtils.isEmpty(filter) && !selecting) {
            throw new InvalidCardDefinitionException("One of 'filter' or 'select' must be provided for Spot effect.");
        }

        if(!StringUtils.isEmpty(filter) && selecting) {
            throw new InvalidCardDefinitionException("Cannot set both 'filter' and 'select' in Spot effect; use filter if the specific cards matter, and select if you memorize the result for later.");
        }

        if(selecting && StringUtils.isEmpty(memorize)) {
            throw new InvalidCardDefinitionException("If 'select' is set, 'memorize' must also be set in Spot effect.");
        }


        if (selecting) {
            return CardResolver.resolveCards(
                    select, valueSource, memorize, "you", text, environment);
        }
        else {
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            return new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    // Spotting changes nothing on the table, but it is carried out only if the cards are there to be
                    // spotted.  An action's own costs are checked before it is offered; a spot that is the cost of a
                    // sub-action (a CostToEffect, which may be one branch of a Choice whose "no valid choices"
                    // fallback offers every branch) is checked here and nowhere else, and a spot that fails must fail
                    // the cost so that the sub-action produces none of its effects.
                    return new AbstractEffect() {
                        @Override
                        public boolean isPlayableInFull(LotroGame game) {
                            return canSpot(actionContext);
                        }

                        @Override
                        protected FullEffectResult playEffectReturningResult(LotroGame game) {
                            return new FullEffectResult(canSpot(actionContext));
                        }
                    };
                }

                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    return canSpot(actionContext);
                }

                private boolean canSpot(ActionContext actionContext) {
                    final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    Filterable filterable = filterableSource.getFilterable(actionContext);
                    return PlayConditions.canSpot(actionContext.getGame(), count, filterable);
                }
            };
        }
    }
}
