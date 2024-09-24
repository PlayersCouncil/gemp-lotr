package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import com.gempukku.lotro.logic.timing.results.PlayEventResult;
import org.json.simple.JSONObject;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "from", "fromZone", "player", "on", "memorize", "exertsRanger");

        final String filterString = FieldUtils.getString(value.get("filter"), "filter");
        final String fromString = FieldUtils.getString(value.get("from"), "from");
        final String player = FieldUtils.getString(value.get("player"), "player");
        final String onString = FieldUtils.getString(value.get("on"), "on");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        boolean exertsRanger = FieldUtils.getBoolean(value.get("exertsRanger"), "exertsRanger", false);

        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString, environment);
        final FilterableSource onFilter = (onString != null) ? environment.getFilterFactory().generateFilter(onString, environment) : null;
        final FilterableSource fromFilter = (fromString != null) ? environment.getFilterFactory().generateFilter(fromString, environment): null;
        final Zone zone = FieldUtils.getEnum(Zone.class, value.get("fromZone"), "fromZone");

        //Squelch incoherent zone values
        if (zone == Zone.FREE_CHARACTERS || zone == Zone.SHADOW_CHARACTERS || zone == Zone.ATTACHED || zone == Zone.ADVENTURE_PATH
                || zone == Zone.SUPPORT || zone == Zone.VOID || zone == Zone.VOID_FROM_HAND )
        {
            throw new InvalidCardDefinitionException("'fromZone' using an unsupported zone in Played trigger.");
        }

        // TODO: this should be used by most cards - need to revise the HJSONs
        // Not sure about that assertion.  The vast majority are "when you play this" triggers, which has a "you" but
        // that's an instruction to the acting player and not an assertion that it isn't in force if your opponent
        // somehow plays the card for you.
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                final Filterable from = fromFilter != null ? fromFilter.getFilterable(actionContext) : null;
                final Filterable on = onFilter != null ? onFilter.getFilterable(actionContext) : null;

                final boolean played = TriggerConditions.played(actionContext.getGame(),
                        actionContext.getEffectResult(), on, from, zone, filterable);

                if (played) {
                    var playCardResult = (PlayCardResult)actionContext.getEffectResult();

                    String playerId = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                    if (playerId != null && !playerId.equals(playCardResult.getPerformingPlayerId()))
                        return false;

                    if (exertsRanger && playCardResult instanceof PlayEventResult && !((PlayEventResult) playCardResult).isRequiresRanger())
                        return false;

                    if (memorize != null) {
                        var playedCard = playCardResult.getPlayedCard();
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
