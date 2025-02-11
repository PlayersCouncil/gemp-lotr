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
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.RangeEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.ExtraFilters;
import com.gempukku.lotro.logic.timing.FailedEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject,  "player", "select", "on", "discount", "maxDiscount", "removedTwilight", "optional", "extraEffects", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String onFilter = FieldUtils.getString(effectObject.get("on"), "on");

        final ValueSource costModifierSource = ValueResolver.resolveEvaluator(effectObject.get("discount"), 0, environment);
        final ValueSource maxDiscountSource = effectObject.get("maxDiscount") == null ? costModifierSource : ValueResolver.resolveEvaluator(effectObject.get("maxDiscount"), 0, environment);
        final int removedTwilight = FieldUtils.getInteger(effectObject.get("removedTwilight"), "removedTwilight", 0);

        final boolean optional = FieldUtils.getBoolean(effectObject.get("optional"), "optional", false);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final JSONObject[] extraEffectsArray = FieldUtils.getObjectArray(effectObject.get("extraEffects"), "extraEffects");
        final EffectAppender[] extraEffectsAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, extraEffectsArray, environment);

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter, environment) : null;

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        Evaluator countEvaluator = optional ? new RangeEvaluator(0, 1) : new ConstantEvaluator(1);

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(select,
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            if (onFilterableSource != null) {
                                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                                return Filters.and(Filters.playable(game, costModifier), ExtraFilters.attachableTo(game, costModifier, onFilterable));
                            }

                            return Filters.playable(game, costModifier);
                        },
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int maxDiscountModifier = maxDiscountSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            if (onFilterableSource != null) {
                                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                                return Filters.and(Filters.playable(actionContext.getGame(), removedTwilight, maxDiscountModifier, false, false, true), ExtraFilters.attachableTo(actionContext.getGame(), maxDiscountModifier, onFilterable));
                            }

                            return Filters.playable(actionContext.getGame(), removedTwilight, maxDiscountModifier, false, false, true);
                        },
                        actionContext -> countEvaluator, memorize, player, "Choose card to play from discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            Filterable onFilterable = (onFilterableSource != null) ? onFilterableSource.getFilterable(actionContext) : Filters.any;

                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(game, cardsToPlay.iterator().next(), costModifier, onFilterable, false);
                            for (EffectAppender extraEffectsAppender : extraEffectsAppenders) {
                                extraEffectsAppender.appendEffect(false, playCardAction, actionContext);
                            }

                            return new StackActionEffect(playCardAction);
                        } else {
                            return new FailedEffect();
                        }
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }
                });

        return result;
    }
}
