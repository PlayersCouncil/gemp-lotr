package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.SiteControlledResult;
import org.json.simple.JSONObject;

public class SiteControlled implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player", "filter");

        String player = FieldUtils.getString(value.get("player"), "player");
        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;
        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                EffectResult effectResult = actionContext.getEffectResult();

                if (effectResult.getType() == EffectResult.Type.CONTROL_SITE) {
                    String playerId = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                    var siteFilter = sourceFilter.getFilterable(actionContext);
                    var takeResult = (SiteControlledResult) effectResult;
                    if(playerId != null && !takeResult.getPlayerId().equals(playerId))
                        return false;

                    return Filters.accepts(actionContext.getGame(), takeResult.getSite(), siteFilter);
                }
                return false;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
