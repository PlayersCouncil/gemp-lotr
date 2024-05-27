package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemorizeTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memory", "count");

        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a Memorize effect.");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Evaluator evaluator = countSource.getEvaluator(actionContext);
                String performingPlayer = actionContext.getPerformingPlayer();
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        int count = evaluator.evaluateExpression(game, null);
                        Collection<? extends PhysicalCard> deck = game.getGameState().getDeck(performingPlayer);
                        List<PhysicalCard> cards = new ArrayList<>();
                        for (PhysicalCard card : deck) {
                            cards.add(card);
                            if (cards.size() == count)
                                break;
                        }

                        actionContext.setCardMemory(memory, cards);
                    }
                };
            }
        };
    }
}
