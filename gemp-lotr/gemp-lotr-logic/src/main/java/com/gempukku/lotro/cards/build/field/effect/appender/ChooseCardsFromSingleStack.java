package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseCardsFromSingleStack implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "on", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String on = FieldUtils.getString(effectObject.get("on"), "on", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose stacked cards.");
        if (text == null)
            throw new InvalidCardDefinitionException("You need to define text to show");

        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on, environment);

        return CardResolver.resolveSingleStack(select, valueSource, onFilterSource, memorize, "you", text, environment);
    }
}
