package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.RemoveTokenEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RemoveTokensCumulative implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "culture", "filter");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 0, environment);
        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");

        final Token token = Token.findTokenForCulture(culture);

        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        String memory = "_temp";

        EffectAppender resolveCardEffect = CardResolver.resolveCard("choose(" + filter + ")",
                actionContext -> {
                    if(token == null)
                        return Filters.hasAnyCultureTokens();

                    return Filters.hasToken(token, 1);
                },
                memory, "you", "Choose card to remove a token from", environment);

        EffectAppender removeTokenEffect = new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memory);
                if (cardFromMemory != null) {
                    if(token == null) {
                        return new RemoveTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), cardFromMemory,
                                Token.findTokenForCulture(cardFromMemory.getBlueprint().getCulture()), 1);
                    }
                    else {
                        return new RemoveTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), cardFromMemory,
                                token, 1);
                    }

                } else {
                    return null;
                }
            }
        };

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS))
                    return false;

                final int requiredCount = valueSource.getEvaluator(actionContext).evaluateExpression(game, null);

                Collection<PhysicalCard> targets;

                if(token == null) {
                    targets = Filters.filterActive(actionContext.getGame(), Filters.hasAnyCultureTokens(), filterableSource.getFilterable(actionContext));
                }
                else {
                    targets = Filters.filterActive(actionContext.getGame(), Filters.hasToken(token), filterableSource.getFilterable(actionContext));
                }

                int totalCount = 0;
                for (var card : targets) {
                    Integer count = null;
                    if(token == null){
                        count = actionContext.getGame().getGameState().getTokens(card)
                                .get(Token.findTokenForCulture(card.getBlueprint().getCulture()));
                    }
                    else {
                        count = actionContext.getGame().getGameState().getTokens(card).get(token);
                    }

                    if (count != null) {
                        totalCount += count;
                        if (totalCount >= requiredCount)
                            return true;
                    }
                }

                return false;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                SubAction subAction = new SubAction(action);
                final LotroGame game = actionContext.getGame();
                final int requiredCount = valueSource.getEvaluator(actionContext).evaluateExpression(game, null);

                for (int i = 0; i < requiredCount; i++) {
                    resolveCardEffect.appendEffect(cost, subAction, actionContext);
                    removeTokenEffect.appendEffect(cost, subAction, actionContext);
                }

                return new StackActionEffect(subAction);
            }
        };
    }
}
