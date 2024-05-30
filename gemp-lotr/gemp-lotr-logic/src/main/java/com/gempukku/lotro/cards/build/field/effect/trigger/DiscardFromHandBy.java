package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromHandResult;
import org.json.simple.JSONObject;

public class DiscardFromHandBy implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "player", "by", "forced");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String player = FieldUtils.getString(value.get("player"), "player", "you");
        final String byFilter = FieldUtils.getString(value.get("by"), "by");
        final boolean forced = FieldUtils.getBoolean(value.get("forced"), "forced", false);

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player, environment) : null;
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                boolean result = TriggerConditions.forEachDiscardedFromHandBy(actionContext.getGame(), actionContext.getEffectResult(),
                        byFilterableSource.getFilterable(actionContext), filterableSource.getFilterable(actionContext));
                if (result && forced) {
                    // Need to check if the discard was forced
                    if (!((DiscardCardFromHandResult) actionContext.getEffectResult()).isForced())
                        result = false;
                }
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String handPlayerId = ((DiscardCardFromHandResult) actionContext.getEffectResult()).getHandPlayerId();
                    if (handPlayerId == null || !handPlayerId.equals(playerSource.getPlayer(actionContext)))
                        result = false;
                }
                if (result && memorize != null) {
                    actionContext.setCardMemory(memorize, ((DiscardCardFromHandResult) actionContext.getEffectResult()).getDiscardedCard());
                }
                return result;
            }
        };
    }
}
