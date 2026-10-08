package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
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

public class PlayCardFromDeadPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "discount", "memorize");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final ValueSource costModifierSource = ValueResolver.resolveDiscount(effectObject.get("discount"), environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        result.addEffectAppender(
                CardResolver.resolveCardsInDeadPile(select,
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateWithoutSpending(game, actionContext.getSource());

                            return Filters.playable(costModifier, 0, false, true, true);
                        },
                        (actionContext) -> {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateWithoutSpending(game, actionContext.getSource());

                            // This filter is the one asked BEFORE the action is paid for: the threats the action
                            // will add as its own cost are counted as added, so that a card which must itself add a
                            // threat to be played is not taken for playable when they leave it no room.  The filter
                            // above is the one used when the effect is carried out; by then they are on the table.
                            return Filters.playable(costModifier, 0, actionContext.getUnpaidThreatCost(), false, true, true);
                        },
                        actionContext -> new ConstantEvaluator(1), memorize, "you", "Choose card to play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final LotroGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());

                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(game, cardsToPlay.iterator().next(), costModifier, null, false);
                            return new StackActionEffect(playCardAction);
                        } else {
                            return new FailedEffect();
                        }
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }

                });

        return result;
    }
}
