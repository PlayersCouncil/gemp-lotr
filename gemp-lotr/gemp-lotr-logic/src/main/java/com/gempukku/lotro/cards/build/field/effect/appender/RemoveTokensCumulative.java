package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
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

public class RemoveTokensCumulative implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "culture", "filter");

        final int requiredCount = FieldUtils.getInteger(effectObject.get("count"), "count");
        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");

        final Token token = Token.findTokenForCulture(culture);

        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        String memory = "_temp";

        EffectAppender resolveCardEffect = CardResolver.resolveCard("choose(" + filter + ")",
                actionContext -> Filters.hasToken(token, 1),
                memory, "you", "Choose card to remove a token from", environment);

        EffectAppender removeTokenEffect = new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memory);
                if (cardFromMemory != null) {
                    return new RemoveTokenEffect(actionContext.getSource(), cardFromMemory, token, 1);
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

                int totalCount = 0;
                for (PhysicalCard physicalCard : Filters.filterActive(actionContext.getGame(), Filters.hasToken(token), filterableSource.getFilterable(actionContext))) {
                    Integer count = actionContext.getGame().getGameState().getTokens(physicalCard).get(token);
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

                for (int i = 0; i < requiredCount; i++) {
                    resolveCardEffect.appendEffect(cost, subAction, actionContext);
                    removeTokenEffect.appendEffect(cost, subAction, actionContext);
                }

                return new StackActionEffect(subAction);
            }
        };
    }
}
