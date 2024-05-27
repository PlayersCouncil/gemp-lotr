package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class EndPhase implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new AddUntilEndOfPhaseModifierEffect(
                        new PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier(actionContext.getSource(), null, actionContext.getGame().getGameState().getCurrentPhase()));
            }
        };
    }
}
