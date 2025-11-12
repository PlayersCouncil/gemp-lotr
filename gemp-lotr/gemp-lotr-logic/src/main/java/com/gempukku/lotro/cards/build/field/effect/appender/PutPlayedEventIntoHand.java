package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PutPlayedEventIntoHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

public class PutPlayedEventIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");

        String memory = filter.contains("memory(") ? CardResolver.extractMemoryLocation(filter) : null;

        if (!filter.equalsIgnoreCase("self") && !filter.equalsIgnoreCase("played") && memory == null)
            throw new InvalidCardDefinitionException("PutPlayedEventIntoHand can only handle either 'self', 'played', or 'memory()'");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                if (filter.equalsIgnoreCase("played")) {
                    PhysicalCard playedCard = ((PlayCardResult) actionContext.getEffectResult()).getPlayedCard();
                    return new PutPlayedEventIntoHandEffect(playedCard);
                }
                //If a memory location was provided, then we trust that this is an event that is being played by some other
                // effect. See V3 Show Them the Meaning of Haste, where an event is "played three times" and thus knows which event is being returned.
                else if(memory != null) {
                    var cards = actionContext.getCardsFromMemory(memory);
                    if(cards.size() != 1)
                        return null;
                    return new PutPlayedEventIntoHandEffect(cards.stream().findFirst().get(), true);
                }
                else {
                    return new PutPlayedEventIntoHandEffect(actionContext.getSource());
                }
            }
        };
    }
}
