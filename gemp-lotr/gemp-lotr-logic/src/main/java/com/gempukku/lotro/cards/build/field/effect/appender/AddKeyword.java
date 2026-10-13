package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilModifierEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class AddKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "memorize", "keyword", "amount", "until");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String keywordString = FieldUtils.getString(effectObject.get("keyword"), "keyword");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        if (keywordString == null)
            throw new InvalidCardDefinitionException("'keyword' needs to be defined for AddKeyword effect.");

        Function<ActionContext, Keyword> keywordFunction;
        ValueSource amount;
        // Damage given for the current phase by a skirmish action is gone when the skirmish is.  No other
        // keyword is taken to matter only to the skirmish being fought.
        boolean damageForTheSkirmish = false;
        if (keywordString.startsWith("fromMemory(") && keywordString.endsWith(")")) {
            String keywordMemory = keywordString.substring(keywordString.indexOf("(") + 1, keywordString.lastIndexOf(")"));
            keywordFunction = actionContext -> Keyword.valueOf(actionContext.getValueFromMemory(keywordMemory));
            amount = actionContext -> new ConstantEvaluator(1);
        } else {
            final String[] keywordSplit = keywordString.split("\\+");
            Keyword keyword = FieldUtils.getEnum(Keyword.class, keywordSplit[0], "keyword");
            keywordFunction = (actionContext) -> keyword;
            damageForTheSkirmish = keyword == Keyword.DAMAGE && until.endsWithCurrentPhase(Phase.SKIRMISH);
            int value = 1;
            if (keywordSplit.length == 2)
                value = Integer.parseInt(keywordSplit[1]);

            amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), value, environment);
        }

        final EffectAppender cardResolver = CardResolver.resolveCards(select, valueSource, memory, "you", "Choose cards to add keyword to", environment);
        final boolean endsWithSkirmish = damageForTheSkirmish;
        final ValueSource amountSource = amount;

        MultiEffectAppender result = new MultiEffectAppender() {
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                final EffectForecast given = EffectForecast.givenTo(cardResolver.forecast(actionContext), endsWithSkirmish);
                if (given.getVerdict() != EffectForecast.Verdict.UNKNOWN || given.getReach() == null)
                    return given;

                // A capped amount is worked out for each card the keyword is given to, and reaches only those
                // for whom its cap is not yet spent; with none of those it does nothing.
                final List<PhysicalCard> open = new LinkedList<>();
                for (PhysicalCard card : given.getReach()) {
                    if (!EffectForecast.isSpent(amountSource, actionContext, card))
                        open.add(card);
                }
                if (open.isEmpty())
                    return EffectForecast.AT_LIMIT;
                return EffectForecast.givenTo(open, endsWithSkirmish);
            }
        };

        result.addEffectAppender(cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        List<Effect> result = new LinkedList<>();
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            final int keywordCount = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), physicalCard);
                            result.add(new AddUntilModifierEffect(
                                    new AddKeywordModifier(actionContext.getSource(), physicalCard, null, keywordFunction.apply(actionContext), keywordCount), until));
                        }

                        actionContext.getGame().getGameState().sendMessage(
                                GameUtils.getCardLink(actionContext.getSource())
                                        + " adds " + keywordFunction.apply(actionContext).getHumanReadableGeneric()
                                        + " to " + GameUtils.getAppendedNames(cardsFromMemory)
                                        + " until " + until.getHumanReadable());
                        return result;
                    }
                });

        return result;
    }

}
