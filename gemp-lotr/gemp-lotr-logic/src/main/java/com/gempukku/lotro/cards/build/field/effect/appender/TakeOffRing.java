package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.TakeOffTheOneRingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class TakeOffRing implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);
        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new TakeOffTheOneRingEffect();
            }

            // With the Ring not on there is nothing to take off.  This is read while the action is only on
            // offer, so it holds where no cost of the action could put the Ring on.  None of the definitions
            // that take it off has such a cost; a wound of the Ring-bearer would be one, since his Ring may
            // answer it.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return isPlayableInFull(actionContext) ? EffectForecast.UNKNOWN : EffectForecast.NOTHING;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return actionContext.getGame().getGameState().isWearingRing();
            }
        };
    }

}
