package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import org.json.simple.JSONObject;

public class Multiple implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "effects");

        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effects"), "effects");

        return createEffectAppender(cost, effectArray, environment);
    }

    public EffectAppender createEffectAppender(boolean cost, JSONObject[] effectArray, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {

        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(cost, effectArray, environment);

        MultiEffectAppender multiEffectAppender = new MultiEffectAppender() {
            // As a branch of a choice: several effects come to nothing only if each of them does, in turn.
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                return EffectForecast.areCertainToDoNothingIfChosen(actionContext, effectAppenders);
            }
        };
        multiEffectAppender.setPlayabilityCheckedForEffect(true);
        for (EffectAppender effectAppender : effectAppenders) {
            multiEffectAppender.addEffectAppender(effectAppender);
        }
        return multiEffectAppender;
    }
}
