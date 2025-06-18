package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.HinderedResult;
import org.json.simple.JSONObject;

public class Hindered implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "source", "player", "zone", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String source = FieldUtils.getString(value.get("source"), "source", "any");
        final String discardingPlayer = FieldUtils.getString(value.get("player"), "player");
        Zone zone = FieldUtils.getEnum(Zone.class, value.get("zone"), "zone");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final PlayerSource playerSource = (discardingPlayer != null) ? PlayerResolver.resolvePlayer(discardingPlayer) : null;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource bySource = source != null ? environment.getFilterFactory().generateFilter(source,
                environment) : null;

        //Squelch incoherent zone values
        if (zone == Zone.ADVENTURE_PATH || zone == Zone.ADVENTURE_DECK || zone == Zone.DEAD || zone == Zone.DECK ||
                zone == Zone.DISCARD || zone == Zone.REMOVED || zone == Zone.VOID )
        {
            throw new InvalidCardDefinitionException("'zone' using an unsupported zone in Hindered trigger (omit the value if wanting to trigger from anywhere in play).");
        }

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);

                PhysicalCard source = null;
                String performingPlayer = null;
                PhysicalCard hinderedCard = null;

                if(TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable)) {
                    var hinderedResult = (HinderedResult) actionContext.getEffectResult();
                    source = hinderedResult.getSource();
                    performingPlayer = hinderedResult.getPerformingPlayer();
                    hinderedCard = hinderedResult.getHinderedCard();
                }

                if(hinderedCard == null)
                    return false;

                if (bySource != null) {
                    if (source == null || !Filters.accepts(actionContext.getGame(), source, bySource.getFilterable(actionContext)))
                        return false;
                }

                if (playerSource != null) {
                    // Need to check if it was that player hindering the card
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayer(actionContext)))
                        return false;
                }

                if (memorize != null) {
                    actionContext.setCardMemory(memorize, hinderedCard);
                }
                return true;
            }
        };
    }
}
