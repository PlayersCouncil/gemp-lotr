package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.google.common.base.Predicates;
import org.json.simple.JSONObject;

public class PreventableAppenderProducer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "text", "player", "effect", "cost", "instead");

        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final String player = FieldUtils.getString(effectObject.get("player"), "player");
        JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");
        JSONObject[] insteadArray = FieldUtils.getObjectArray(effectObject.get("instead"), "instead");

        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for preventable effect");
        if (player == null)
            throw new InvalidCardDefinitionException("Player is required for preventable effect");

        final PlayerSource preventingPlayerSource = PlayerResolver.resolvePlayer(player);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);
        final EffectAppender[] insteadAppenders = environment.getEffectAppenderFactory().getEffectAppenders(insteadArray, environment);

        return new PreventableEffectAppender(preventingPlayerSource, text, Predicates.alwaysTrue(), costAppenders, effectAppenders, insteadAppenders);
    }
}
