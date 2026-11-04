package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
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

public class RemoveCultureTokens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "culture", "select", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        if (select == null)
            throw new InvalidCardDefinitionException("select needs to be defined");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final Token token = (culture != null) ? Token.findTokenForCulture(culture) : null;

        MultiEffectAppender result = new MultiEffectAppender();

        // "Remove N tokens from <a card>".  A cost, and whether the removal can be done in full (what a choice of
        // branches offers, F-0310), want a card holding the full N.  As an effect a chosen card is one holding the
        // full N while there is one to choose (a choice is of what can be fully performed, as Exert's is on F-0131);
        // with none, any card holding a token may be chosen and gives up as many as it has, up to N (CR 4.2
        // "effect"; Ketura on F-0702: "effects should do as much as they can").  Where nobody chooses ("self", a
        // memory, "all(...)") the effect takes what it can from each card holding a token.
        final boolean chosen = select.startsWith("choose(") && select.endsWith(")");
        final FilterableSource chosenFrom = chosen
                ? environment.getFilterFactory().generateFilter(select.substring(select.indexOf("(") + 1, select.lastIndexOf(")")), environment)
                : null;

        final FilterableSource inFull = actionContext -> tokenFilter(token,
                valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null));

        EffectAppender cardResolver = CardResolver.resolveCards(select, SpotOverride.NONE,
                actionContext -> {
                    final int tokenCount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    final Filter full = tokenFilter(token, tokenCount);
                    if (cost || tokenCount <= 1)
                        return full;
                    if (chosen && Filters.countActive(actionContext.getGame(), chosenFrom.getFilterable(actionContext), full) > 0)
                        return full;
                    return tokenFilter(token, 1);
                },
                inFull, actionContext -> new ConstantEvaluator(1), memory, "you", "Choose card to remove tokens from", environment);

        EffectUtils.validatePreEvaluate(cost, effectObject, valueSource, cardResolver);

        result.addEffectAppender(
                cardResolver);
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
                                result.add(new RemoveTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), card, token, tokenCount));
                            else {
                                result.add(new RemoveTokenEffect(actionContext.getPerformingPlayer(), actionContext.getSource(), card, getCultureTokenOnCard(game, card), tokenCount));
                            }
                        }

                        return result;
                    }
                });

        return result;
    }

    private static Filter tokenFilter(Token token, int count) {
        return (token != null) ? Filters.hasToken(token, count) : Filters.hasAnyCultureTokens(count);
    }

    private Token getCultureTokenOnCard(LotroGame game, PhysicalCard card) {
        for (Token token : Token.values())
            if (token.getCulture() != null && game.getGameState().getTokenCount(card, token) > 0)
                return token;

        return null;
    }
}
