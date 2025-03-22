package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class RemovesCultureToken implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "on", "player");

        String player = FieldUtils.getString(value.get("player"), "player");
        String onFilter = FieldUtils.getString(value.get("on"), "on", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;
        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onSourceFilter = environment.getFilterFactory().generateFilter(onFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                String playerId = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                return TriggerConditions.removedCultureToken(actionContext.getGame(), playerId, actionContext.getEffectResult(),
                        sourceFilter.getFilterable(actionContext), onSourceFilter.getFilterable(actionContext));
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
