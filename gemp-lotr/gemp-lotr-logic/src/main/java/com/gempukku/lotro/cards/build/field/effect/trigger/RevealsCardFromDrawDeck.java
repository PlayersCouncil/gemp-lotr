package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class RevealsCardFromDrawDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "source", "deck");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String source = FieldUtils.getString(value.get("source"), "source", "any");
        String deck = FieldUtils.getString(value.get("deck"), "deck");

        final FilterableSource cardSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource sourceSource = environment.getFilterFactory().generateFilter(source, environment);
        final PlayerSource deckSource = (deck != null) ? PlayerResolver.resolvePlayer(deck) : null;

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.revealedCardsFromDeck(
                        actionContext.getEffectResult(),
                        actionContext.getGame(),
                        deckSource != null ? deckSource.getPlayer(actionContext) : null,
                        sourceSource.getFilterable(actionContext),
                        cardSource.getFilterable(actionContext));
            }
        };
    }
}
