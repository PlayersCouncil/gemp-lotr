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
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ExhaustCharacterEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Map;

public class Exhaust implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count", "select", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        // For telling beforehand whom a cost is certain to exhaust; a choice made by another player is not forecast.
        final FilterableSource chosenFrom = "you".equalsIgnoreCase(player) && select != null && select.startsWith("choose(") && select.endsWith(")")
                ? environment.getFilterFactory().generateFilter(select.substring(select.indexOf("(") + 1, select.lastIndexOf(")")), environment)
                : null;

        final EffectAppender resolver = CardResolver.resolveCards(select,
                SpotOverride.NONE,
                (actionContext) -> Filters.canExert(actionContext.getSource(), 1),
                valueSource, memory, player, "Choose cards to exhaust", environment);

        EffectUtils.validatePreEvaluate(cost, effectObject, valueSource, resolver);

        MultiEffectAppender result = new MultiEffectAppender() {
            // With nobody who can exert it does nothing.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return EffectForecast.givenTo(resolver.forecast(actionContext), false);
            }

            // As a cost: each character who is certain to be exhausted is left with 1 vitality.
            @Override
            public void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
                final LotroGame game = actionContext.getGame();
                for (PhysicalCard exhausted : EffectUtils.getCardsCertainToBeReached(select, chosenFrom, valueSource,
                        Filters.canExert(actionContext.getSource(), 1), actionContext)) {
                    final int exertions = game.getModifiersQuerying().getVitality(game, exhausted) - 1;
                    if (exertions > 0)
                        tally.merge(exhausted, exertions, Integer::sum);
                }
            }
        };

        result.addEffectAppender(resolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        final ExhaustCharacterEffect exhaustion = new ExhaustCharacterEffect(actionContext.getSource(), action, Filters.in(cardsFromMemory));
                        // An exhaustion is exertions, and says of each what `Exert` says of its own (F-0478).  No
                        // card shows it: no minion's special ability exhausts that minion as a cost, and "exerts as
                        // a cost of its special ability" (Rush of Steeds 11_157) is the one text that asks.
                        exhaustion.setPaidAsCost(cost);
                        return exhaustion;
                    }
                });

        return result;
    }

}
