package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.FailedEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "on", "discount", "removedTwilight", "assumePlayable", "memorize");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String onFilter = FieldUtils.getString(effectObject.get("on"), "on", "any");
        final ValueSource costModifierSource = ValueResolver.resolveEvaluator(effectObject.get("discount"), 0, environment);
        final int removedTwilight = FieldUtils.getInteger(effectObject.get("removedTwilight"), "removedTwilight", 0);
        final boolean assumePlayable = FieldUtils.getBoolean(effectObject.get("assumePlayable"), "assumePlayable", false);
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        result.addEffectAppender(
                CardResolver.resolveStackedCards(select,
                        actionContext -> {
                            LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            return Filters.playable(actionContext.getGame(), costModifier);
                        },
                        assumePlayable ? null : actionContext -> {
                            LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            return Filters.playable(actionContext.getGame(), removedTwilight, costModifier, false, false, true);
                        },
                        actionContext -> new ConstantEvaluator(1), onFilterableSource, memory, "you", "Choose card to play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memory);
                        if (cardsToPlay.size() == 1) {
                            LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(actionContext.getGame(), cardsToPlay.iterator().next(), costModifier, Filters.any, false);
                            return new StackActionEffect(playCardAction);
                        } else {
                            return new FailedEffect();
                        }
                    }
                });

        return result;
    }
}
