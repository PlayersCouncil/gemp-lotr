package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import org.json.simple.JSONObject;

public class Discarded implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "player", "source");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String player = FieldUtils.getString(value.get("player"), "player");
        final String source = FieldUtils.getString(value.get("source"), "source");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player, environment) : null;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource sourceSource = source != null ? environment.getFilterFactory().generateFilter(source, environment) : null;

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                boolean result = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) actionContext.getEffectResult();
                if (result && sourceSource != null) {
                    PhysicalCard discardSource = discardResult.getSource();
                    if (discardSource == null || !Filters.accepts(actionContext.getGame(), sourceSource.getFilterable(actionContext), discardSource))
                        result = false;
                }
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String performingPlayer = discardResult.getPerformingPlayer();
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayer(actionContext)))
                        result = false;
                }
                if (result && memorize != null) {
                    final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }
                return result;
            }
        };
    }
}
