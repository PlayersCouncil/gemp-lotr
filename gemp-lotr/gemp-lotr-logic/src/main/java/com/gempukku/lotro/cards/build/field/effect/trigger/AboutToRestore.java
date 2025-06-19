package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AboutToRestore implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "source", "filter", "player");

        String source = FieldUtils.getString(value.get("source"), "source", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter");
        String player = FieldUtils.getString(value.get("player"), "player");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source, environment);
        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);
        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                String playerId = playerSource.getPlayer(actionContext);
                return TriggerConditions.isGettingRestoredBy(actionContext.getEffect(), actionContext.getGame(),
                        sourceFilter.getFilterable(actionContext),
                        playerId,
                        affectedFilter.getFilterable(actionContext));
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
