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
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.RemoveTokenEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ChooseAndRemoveTokens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "culture", "filter", "memorize", "memorizeCard", "limit");

        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String memorizeCard = FieldUtils.getString(effectObject.get("memorizeCard"), "memorizeCard", "_temp2");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("limit"), 0, environment);

        final Token token = (culture != null) ? Token.findTokenForCulture(culture) : null;

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCards(filter, (actionContext) -> token != null ? Filters.hasToken(token, 1) : Filters.hasAnyCultureTokens(1),
                        new ConstantEvaluator(1),
                        memorizeCard, "you", "Choose card to remove tokens from", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS);
                    }

                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memorizeCard);

                        int min = valueSource.getMinimum(actionContext);
                        int max = valueSource.getMaximum(actionContext);
                        if(max == 0)
                        {
                            max = actionContext.getGame().getGameState().getTokenCount(cardFromMemory, token);

                        }

                        if(min == max)
                        {
                            actionContext.setValueToMemory(memory, String.valueOf(max));
                            return null;
                        }

                        return new PlayoutDecisionEffect(actionContext.getPerformingPlayer(),
                                new IntegerAwaitingDecision(1, "How many tokens do you wish to remove", min, max) {
                                    @Override
                                    public void decisionMade(String result) throws DecisionResultInvalidException {
                                        actionContext.setValueToMemory(memory, String.valueOf(getValidatedResult(result)));
                                    }
                                });
                    }
                });
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS);
                    }

                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard cardFromMemory = actionContext.getCardFromMemory(memorizeCard);
                        int tokenCount = Integer.parseInt(actionContext.getValueFromMemory(memory));

                        List<Effect> result = new LinkedList<>();
                        if (token != null)
                            result.add(new RemoveTokenEffect(actionContext.getSource(), cardFromMemory, token, tokenCount));
                        else {
                            result.add(new RemoveTokenEffect(actionContext.getSource(), cardFromMemory,
                                    getCultureTokenOnCard(actionContext.getGame(), cardFromMemory), tokenCount));
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
