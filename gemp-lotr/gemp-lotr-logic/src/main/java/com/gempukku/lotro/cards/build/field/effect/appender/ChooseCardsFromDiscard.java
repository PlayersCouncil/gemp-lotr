package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class ChooseCardsFromDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        //The words the player is asked the choice in, where the definition has words of its own for it.
        final String text = FieldUtils.getString(effectObject.get("text"), "text");

        final EffectAppender cardResolver = CardResolver.resolveCardsInDiscard(select, null, null, valueSource, memorize, "you", "you", "Choose cards from discard.", text, environment);
        // Taught for Q-0224 (IdleOffers): with nothing to choose from nothing is chosen, and nothing remembered.
        return TaughtForecast.actsOnWhatItFinds(cardResolver, cardResolver);
    }
}
