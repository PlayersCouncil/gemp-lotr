package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.SpotOverride;
import org.json.simple.JSONObject;

public class ChooseActiveCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "player", "select", "hindered", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final boolean hindered = FieldUtils.getBoolean(effectObject.get("hindered"), "hindered", false);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        if (text == null)
            throw new InvalidCardDefinitionException("You need to define text to show");

        if(hindered)
            return CardResolver.resolveCards(select, SpotOverride.INCLUDE_HINDERED, null, valueSource, memorize, player, text, environment);
        else
            return CardResolver.resolveCards(select, SpotOverride.NONE, null, valueSource, memorize, player, text, environment);
    }
}
