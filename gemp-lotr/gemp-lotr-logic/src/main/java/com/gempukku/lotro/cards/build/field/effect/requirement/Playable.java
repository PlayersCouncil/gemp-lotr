package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;

public class Playable implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "filter", "discount", "fromZone");

        final String playerName = FieldUtils.getString(object.get("player"), "player", "you");
        final String filterDef = FieldUtils.getString(object.get("filter"), "filter");
        final ValueSource discountValue = ValueResolver.resolveEvaluator(object.get("discount"), 0, environment);
        final Zone zone = FieldUtils.getEnum(Zone.class, object.get("fromZone"), "fromZone");

        if(zone != null && zone != Zone.HAND && zone != Zone.DISCARD && zone != Zone.DECK && zone != Zone.STACKED) {
            throw new InvalidCardDefinitionException("fromZone must be one of HAND, DISCARD, DECK, or STACKED for Playable.");
        }

        if(zone == null && !filterDef.equals("self") && !filterDef.startsWith("memory(")) {
            throw new InvalidCardDefinitionException("If fromZone is not set, only 'self' and 'memory' are supported filters for Playable.");
        }

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(playerName);

        final FilterableSource fallbackFilter = getFallbackFilter(filterDef, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filterDef, environment);
        return (actionContext) -> {
            final var game = actionContext.getGame();
            final int discount = discountValue.getEvaluator(actionContext).evaluateExpression(game, null);
            final var player = playerSource.getPlayer(actionContext);
            final var filter = filterableSource.getFilterable(actionContext);

            if(zone == Zone.DISCARD) {
                return PlayConditions.canPlayFromDiscard(player, game, discount, filter);
            }

            if(zone == Zone.DECK) {
                return PlayConditions.canPlayFromDrawDeck(player, game, discount, filter);
            }

            if(zone == Zone.HAND) {
                return PlayConditions.canPlayFromHand(player, game, discount, filter);
            }

            if(zone == Zone.STACKED) {
                return PlayConditions.canPlayFromStacked(player, game, discount, filter);
            }

            // We have ensured that only 'self' and 'memory' can make it to this situation
            return Filters.acceptsAny(game,
                    game.getGameState().getAllCards(),
                    fallbackFilter.getFilterable(actionContext),
					Filters.playable(0));
        };
    }

    private FilterableSource getFallbackFilter(String filter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (filter.equals("self"))
            return ActionContext::getSource;
        if (filter.startsWith("memory(") && filter.endsWith(")"))
            return environment.getFilterFactory().generateFilter(filter, environment);

        return null;
    }
}
