package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
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
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "culture", "select", "memorize", "times", "player");

        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final Token token = Token.findTokenForCulture(culture);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        Filter tokenFilter = token != null ? Filters.hasToken(token, 1) : Filters.hasAnyCultureTokens(1);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        MultiEffectAppender result = new MultiEffectAppender();

        // CR 4.2 "reinforce": "You may choose any of your cards with the appropriate culture token": the cards of
        // the player who reinforces, never another player's, whatever the text leaves unsaid about the culture.
        final EffectAppender cardResolver = CardResolver.resolveCard(select,
                        (actionContext) -> Filters.and(tokenFilter, Filters.owner(playerSource.getPlayer(actionContext))),
                        memory, player, "Choose card to reinforce tokens on", environment);
        result.addEffectAppender(cardResolver);
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
                                result.add(new AddTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), card, token, tokenCount));
                            } else {
                                Token tokenOnCard = getFirstCultureToken(actionContext.getGame().getGameState().getTokens(card));
                                if (tokenOnCard != null) {
                                    result.add(new AddTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), card, tokenOnCard, tokenCount));
                                }
                            }
                        }

                        return result;
                    }
                });

        // Taught for Q-0224 (IdleOffers): with no card that has such a token there is nothing to reinforce, and
        // where culture tokens cannot be reinforced (Redhorn Pass 13_194) nothing is reinforced either.
        return TaughtForecast.teach(result, actionContext -> {
            final LotroGame game = actionContext.getGame();
            if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS))
                return EffectForecast.NOTHING;
            final EffectForecast found = cardResolver.forecast(actionContext);
            return found.getReach() != null && found.getReach().isEmpty() ? EffectForecast.NOTHING : EffectForecast.UNKNOWN;
        });
    }

    private Token getFirstCultureToken(Map<Token, Integer> tokens) {
        for (Map.Entry<Token, Integer> tokenCountEntry : tokens.entrySet()) {
            if (tokenCountEntry.getValue() > 0 && tokenCountEntry.getKey().getCulture() != null)
                return tokenCountEntry.getKey();
        }
        return null;
    }
}
