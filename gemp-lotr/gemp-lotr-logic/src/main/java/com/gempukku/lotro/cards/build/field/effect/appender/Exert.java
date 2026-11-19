package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Exert implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count", "times", "select", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final ValueSource timesSource = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        // "Exert X twice": where the player chooses who exerts, the choice is of those able to exert that many
        // times (CR 4.2 "exert"; Ketura on F-0131), so that no definition has to say so with a vitality filter.
        // A cost is held to them outright.  An effect is held to them while there are enough of them to choose
        // (a choice is of what can be fully performed); with none it falls back to anyone able to exert at all,
        // who then exerts as often as he can (do what you can and ignore the rest).  Where nobody chooses
        // ("self", "bearer", a memory, "all(...)") each exerts as often as he can, as before.
        final boolean chosen = select != null && select.startsWith("choose(") && select.endsWith(")");
        final FilterableSource chosenFrom = chosen
                ? environment.getFilterFactory().generateFilter(select.substring(select.indexOf("(") + 1, select.lastIndexOf(")")), environment)
                : null;

        final EffectAppender cardResolver = CardResolver.resolveCards(select,
                SpotOverride.NONE,
                (actionContext) -> {
                    final PhysicalCard source = actionContext.getSource();
                    if (!chosen)
                        return Filters.canExert(source, 1);
                    final int times = timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    if (times <= 1)
                        return Filters.canExert(source, 1);
                    final Filter inFull = Filters.canExert(source, times);
                    if (cost)
                        return inFull;
                    final int needed = Math.max(1, valueSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null));
                    final int able = Filters.countActive(actionContext.getGame(), chosenFrom.getFilterable(actionContext), inFull);
                    return able >= needed ? inFull : Filters.canExert(source, 1);
                },
                (actionContext) -> Filters.canExert(actionContext.getSource(), timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)),
                valueSource, memory, player, "Choose cards to exert", environment);

        EffectUtils.validatePreEvaluate(cost, effectObject, valueSource, timesSource, cardResolver);

        final boolean timesReadsMemory = NoTimesNoChoice.readsMemory(effectObject.get("times"));

        MultiEffectAppender result = new MultiEffectAppender() {
            // With nobody who can exert it does nothing; as an effect, nor with X times come to 0 (Q-0337).
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (NoTimesNoChoice.isCertainlyNoTimes(cost, actionContext, timesSource, timesReadsMemory))
                    return EffectForecast.NOTHING;
                return EffectForecast.givenTo(cardResolver.forecast(actionContext), false);
            }

            // As a cost: each character who is certain to exert takes a wound for each time.
            @Override
            public void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
                final int times = timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (times <= 0)
                    return;
                // A choice made by another player is not forecast: the filter is his to read.
                final FilterableSource certainFrom = "you".equalsIgnoreCase(player) ? chosenFrom : null;
                for (PhysicalCard exerted : EffectUtils.getCardsCertainToBeReached(select, certainFrom, valueSource,
                        Filters.canExert(actionContext.getSource(), times), actionContext))
                    tally.merge(exerted, times, Integer::sum);
            }

            // One paying of "exert a companion" where there is a choice of who exerts: the candidates with the
            // most vitality left, as many as must be chosen, which leaves the most able to exert again.  The
            // filter is read as the player who chooses reads it, as the choice itself is.  Morgul Regiment
            // 7_197 repeats a choice of one companion who exerts once; no printed card repeats, as a cost, a
            // choice of several or an "exert twice", so those are worked out here and shown by no card.
            @Override
            public void tallyWoundsOfPayingOnce(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
                if (!chosen) {
                    tallyWoundsPlacedAsCost(actionContext, tally);
                    return;
                }
                final LotroGame game = actionContext.getGame();
                final int times = timesSource.getEvaluator(actionContext).evaluateExpression(game, null);
                final int needed = valueSource.getEvaluator(actionContext).getMinimum(game, null);
                if (times <= 0 || needed <= 0)
                    return;
                final ActionContext choosersContext = new DelegateActionContext(actionContext, playerSource.getPlayer(actionContext));
                final List<PhysicalCard> able = new ArrayList<>(Filters.filterActive(game,
                        chosenFrom.getFilterable(choosersContext), Filters.canExert(actionContext.getSource(), times)));
                // Card order among equals, which is the order filterActive gives
                able.sort(Comparator.comparingInt((PhysicalCard card) -> game.getModifiersQuerying().getVitality(game, card)).reversed());
                for (PhysicalCard exerted : able.subList(0, Math.min(needed, able.size())))
                    tally.merge(exerted, times, Integer::sum);
            }
        };

        // As an effect, "exert X times" with X come to 0 chooses nobody and asks nothing (Q-0337: B).
        result.addEffectAppender(
                NoTimesNoChoice.resolver(cost, chosen, cardResolver, timesSource, memory));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        final int times = timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        List<Effect> result = new LinkedList<>();
                        for (int i = 0; i < times; i++) {
                            final ExertCharactersEffect exertion = new ExertCharactersEffect(action, actionContext.getSource(), cardsFromMemory.toArray(new PhysicalCard[0]));
                            // "Exerts as a cost of its special ability" (Rush of Steeds 11_157; F-0478): the
                            // result of the exertion says whether it was paid as a cost.
                            exertion.setPaidAsCost(cost);
                            result.add(exertion);
                        }
                        return result;
                    }
                });

        return result;
    }

}
