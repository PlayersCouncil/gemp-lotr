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
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddTokenEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReinforceTokens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "culture", "filter", "memorize", "times", "player");

        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final Token token = Token.findTokenForCulture(culture);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        Filter tokenFilter = token != null ? Filters.hasToken(token, 1) : Filters.hasAnyCultureTokens(1);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(filter, (actionContext) -> tokenFilter,
                        memory, player, "Choose card to reinforce tokens on", environment));
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

                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cardsFromMemory) {
                            if (token != null) {
                                result.add(new AddTokenEffect(actionContext.getSource(), card, token, tokenCount));
                            } else {
                                Token tokenOnCard = getFirstCultureToken(actionContext.getGame().getGameState().getTokens(card));
                                if (tokenOnCard != null) {
                                    result.add(new AddTokenEffect(actionContext.getSource(), card, tokenOnCard, tokenCount));
                                }
                            }
                        }

                        return result;
                    }
                });

        return result;
    }

    private Token getFirstCultureToken(Map<Token, Integer> tokens) {
        for (Map.Entry<Token, Integer> tokenCountEntry : tokens.entrySet()) {
            if (tokenCountEntry.getValue() > 0 && tokenCountEntry.getKey().getCulture() != null)
                return tokenCountEntry.getKey();
        }
        return null;
    }
}
