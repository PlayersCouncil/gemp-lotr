package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.RemovePlayedEventFromTheGameEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

public class RemovePlayedEventFromTheGame implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");

        if (!filter.equalsIgnoreCase("self") && !filter.equalsIgnoreCase("played"))
            throw new InvalidCardDefinitionException("Can only return either 'self' or 'played'");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                if (filter.equalsIgnoreCase("played")) {
                    var result = (PlayCardResult) actionContext.getEffectResult();
                    if(result != null)
                        return new RemovePlayedEventFromTheGameEffect(result.getPlayedCard());

                    var playedCard = actionContext.getCardFromMemory("playedEvent");
                    if(playedCard != null)
                        return new RemovePlayedEventFromTheGameEffect(playedCard);

                    return null;
                } else {
                    return new RemovePlayedEventFromTheGameEffect(actionContext.getSource());
                }
            }
        };
        }
}
