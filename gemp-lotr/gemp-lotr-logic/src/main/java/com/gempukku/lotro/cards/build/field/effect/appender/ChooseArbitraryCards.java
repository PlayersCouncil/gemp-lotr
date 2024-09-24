package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ChooseArbitraryCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "fromMemory", "count", "filter", "memorize", "text", "player");

        final String fromMemory = FieldUtils.getString(effectObject.get("fromMemory"), "fromMemory");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        if (fromMemory == null)
            throw new InvalidCardDefinitionException("'fromMemory' field required for ChooseArbitraryCards effect.");
        if (memorize == null)
            throw new InvalidCardDefinitionException("'memorize' field required for ChooseArbitraryCards effect.");
        if (text == null)
            throw new InvalidCardDefinitionException("'text' field required for ChooseArbitraryCards effect.");

        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(fromMemory);
                Collection<PhysicalCard> selectableCards = Filters.filter(actionContext.getGame(), cards, filterableSource.getFilterable(actionContext));

                Evaluator evaluator = valueSource.getEvaluator(actionContext);
                int minimum = Math.min(selectableCards.size(), evaluator.getMinimum(actionContext.getGame(), null));
                int maximum = Math.min(selectableCards.size(), evaluator.getMaximum(actionContext.getGame(), null));

                String choosingPlayer = playerSource.getPlayer(actionContext);

                return new PlayoutDecisionEffect(choosingPlayer,
                        new ArbitraryCardsSelectionDecision(1, GameUtils.substituteText(text, actionContext),
                                cards, selectableCards, minimum, maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                actionContext.setValueToMemory(memorize, String.valueOf(getSelectedCardsByResponse(result)));
                            }
                        });
            }
        };
    }
}
