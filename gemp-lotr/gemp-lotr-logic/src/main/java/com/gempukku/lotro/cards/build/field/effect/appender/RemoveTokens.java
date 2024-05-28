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
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.RemoveTokenEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RemoveTokens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "culture", "select", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        if (select == null)
            throw new InvalidCardDefinitionException("select needs to be defined");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final Token token = (culture != null) ? Token.findTokenForCulture(culture) : null;

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select,
                        actionContext -> {
                            final int tokenCount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            if (token != null)
                                return Filters.hasToken(token, tokenCount);
                            else
                                return Filters.hasAnyCultureTokens(tokenCount);
                        },
                        actionContext -> new ConstantEvaluator(1), memory, "you", "Choose card to remove tokens from", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS);
                    }

                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        final int tokenCount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                        final LotroGame game = actionContext.getGame();

                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cardsFromMemory) {
                            if (token != null)
                                result.add(new RemoveTokenEffect(actionContext.getSource(), card, token, tokenCount));
                            else {
                                result.add(new RemoveTokenEffect(actionContext.getSource(), card, getCultureTokenOnCard(game, card), tokenCount));
                            }
                        }

                        return result;
                    }
                });

        return result;
    }

    private Token getCultureTokenOnCard(LotroGame game, PhysicalCard card) {
        for (Token token : Token.values())
            if (token.getCulture() != null && game.getGameState().getTokenCount(card, token) > 0)
                return token;

        return null;
    }
}
