package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PutPlayedEventOnTopOfDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

public class PutPlayedEventOnTopOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        // "self" is the event this is written on ("place this event on top of your draw deck").  "played" is the
        // card whose play is being answered, for another card's Played trigger ("place that event ...").
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");

        if (!filter.equalsIgnoreCase("self") && !filter.equalsIgnoreCase("played"))
            throw new InvalidCardDefinitionException("PutPlayedEventOnTopOfDrawDeck can only handle either 'self' or 'played'");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                if (filter.equalsIgnoreCase("played")) {
                    // Only the answer to a play has a card played; anywhere else there is nothing to place.
                    if (actionContext.getEffectResult() instanceof PlayCardResult playResult)
                        return new PutPlayedEventOnTopOfDeckEffect(playResult.getPlayedCard());
                    return null;
                }
                return new PutPlayedEventOnTopOfDeckEffect(actionContext.getSource());
            }
        };
    }
}
