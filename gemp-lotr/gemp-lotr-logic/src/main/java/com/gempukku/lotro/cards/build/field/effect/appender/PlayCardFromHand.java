package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.ExtraFilters;
import com.gempukku.lotro.logic.timing.FailedEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "on", "discount", "maxDiscount", "removedTwilight", "ignoreInDeadPile", "ignoreRoamingPenalty", "ignorePlayability", "memorize");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String onFilter = FieldUtils.getString(effectObject.get("on"), "on");
        final ValueSource costModifierSource = ValueResolver.resolveEvaluator(effectObject.get("discount"), 0, environment);
        final ValueSource maxDiscountSource = effectObject.get("maxDiscount") == null ? costModifierSource : ValueResolver.resolveEvaluator(effectObject.get("maxDiscount"), 0, environment);
        final int removedTwilight = FieldUtils.getInteger(effectObject.get("removedTwilight"), "removedTwilight", 0);
        final boolean ignoreInDeadPile = FieldUtils.getBoolean(effectObject.get("ignoreInDeadPile"), "ignoreInDeadPile", false);
        final boolean ignoreRoamingPenalty = FieldUtils.getBoolean(effectObject.get("ignoreRoamingPenalty"), "ignoreRoamingPenalty", false);
        final boolean ignorePlayability = FieldUtils.getBoolean(effectObject.get("ignorePlayability"), "ignorePlayability", false);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter, environment) : null;

        MultiEffectAppender result = new MultiEffectAppender();
        if(!ignorePlayability) {
            result.setPlayabilityCheckedForEffect(true);
        }

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(select,
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            if (onFilterableSource != null) {
                                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                                return Filters.and(Filters.playable(costModifier, 0, ignoreRoamingPenalty, ignoreInDeadPile, true), ExtraFilters.attachableTo(game, costModifier, onFilterable));
                            }
                            return Filters.playable(costModifier, 0, ignoreRoamingPenalty, ignoreInDeadPile, true);
                        },
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int maxDiscountModifier = maxDiscountSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            if (onFilterableSource != null) {
                                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                                return Filters.and(Filters.playable(maxDiscountModifier, removedTwilight, ignoreRoamingPenalty, ignoreInDeadPile, true), ExtraFilters.attachableTo(game, maxDiscountModifier, onFilterable));
                            }
                            return Filters.playable(maxDiscountModifier, removedTwilight, ignoreRoamingPenalty, ignoreInDeadPile, true);
                        },
                        actionContext -> new ConstantEvaluator(1), memorize, "you", "you", "Choose card to play from hand", false, environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());

                            Filterable onFilterable = (onFilterableSource != null) ? onFilterableSource.getFilterable(actionContext) : Filters.any;

                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(game, cardsToPlay.iterator().next(), costModifier, onFilterable, ignoreRoamingPenalty);
                            return new StackActionEffect(playCardAction);
                        } else {
                            return new FailedEffect();
                        }
                    }
                });

        return result;
    }
}
