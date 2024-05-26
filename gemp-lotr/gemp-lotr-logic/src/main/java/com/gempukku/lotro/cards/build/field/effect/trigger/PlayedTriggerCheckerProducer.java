package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import com.gempukku.lotro.logic.timing.results.PlayEventResult;
import org.json.simple.JSONObject;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "on", "memorize", "exertsRanger", "from", "player");

        final String filterString = FieldUtils.getString(value.get("filter"), "filter");
        final String onString = FieldUtils.getString(value.get("on"), "on");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        boolean exertsRanger = FieldUtils.getBoolean(value.get("exertsRanger"), "exertsRanger", false);
        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString, environment);
        final FilterableSource onFilter = (onString != null) ? environment.getFilterFactory().generateFilter(onString, environment) : null;
        Zone zone = FieldUtils.getEnum(Zone.class, value.get("from"), "from");
        String player = FieldUtils.getString(value.get("player"), "player");

        // TODO: this should be used by most cards - need to revise the HJSONs
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player, environment) : null;

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                boolean played;
                if (onFilter != null) {
                    final Filterable onFilterable = onFilter.getFilterable(actionContext);
                    played = TriggerConditions.playedOn(actionContext.getGame(), actionContext.getEffectResult(), onFilterable, filterable);
                } else {
                    played = TriggerConditions.played(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                }

                if (played) {
                    PlayCardResult playCardResult = (PlayCardResult) actionContext.getEffectResult();

                    String playerId = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                    if (playerId != null && !playerId.equals(playCardResult.getPerformingPlayerId()))
                        return false;

                    if (zone != null && playCardResult.getPlayedFrom() != zone)
                        return false;

                    if (exertsRanger && playCardResult instanceof PlayEventResult && !((PlayEventResult) playCardResult).isRequiresRanger())
                        return false;

                    if (memorize != null) {
                        PhysicalCard playedCard = playCardResult.getPlayedCard();
                        actionContext.setCardMemory(memorize, playedCard);
                    }
                }
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
