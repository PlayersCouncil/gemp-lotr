package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromDeckResult;
import com.gempukku.lotro.logic.timing.results.DiscardCardFromHandResult;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;
import org.json.simple.JSONObject;

public class Discarded implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "source", "player", "fromZone", "ignoreVoluntary", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String source = FieldUtils.getString(value.get("source"), "source");
        final String discardingPlayer = FieldUtils.getString(value.get("player"), "player");
        Zone zone = FieldUtils.getEnum(Zone.class, value.get("fromZone"), "fromZone");
        final boolean ignoreVoluntary = FieldUtils.getBoolean(value.get("ignoreVoluntary"), "ignoreVoluntary", false);
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final PlayerSource playerSource = (discardingPlayer != null) ? PlayerResolver.resolvePlayer(discardingPlayer) : null;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource bySource = source != null ? environment.getFilterFactory().generateFilter(source,
                environment) : null;

        //Squelch incoherent zone values
        if (zone == Zone.FREE_CHARACTERS || zone == Zone.SHADOW_CHARACTERS || zone == Zone.STACKED || zone == Zone.ATTACHED
                || zone == Zone.SUPPORT || zone == Zone.VOID || zone == Zone.VOID_FROM_HAND || zone == Zone.REMOVED)
        {
            throw new InvalidCardDefinitionException("'fromZone' using an unsupported zone in Discarded trigger (omit the value if wanting to trigger from play).");
        }

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);

                boolean result = false;
                boolean forced = false;
                PhysicalCard source = null;
                String performingPlayer = null;
                PhysicalCard discardedCard = null;
                if(zone == null) { // From play
                    result = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(),
                            actionContext.getEffectResult(), filterable);

                    if(result) {
                        var discardResult = (DiscardCardsFromPlayResult) actionContext.getEffectResult();
                        source = discardResult.getSource();
                        //TODO: Add proper forcefulness detection to in-play discards
                        forced = false;
                        performingPlayer = discardResult.getPerformingPlayer();
                        discardedCard = discardResult.getDiscardedCard();
                    }
                }
                else if(zone == Zone.DECK) {
                    result = TriggerConditions.forEachDiscardedFromDeck(actionContext.getGame(),
                            actionContext.getEffectResult(), filterable);

                    if(result) {
                        var discardResult = (DiscardCardFromDeckResult) actionContext.getEffectResult();
                        source = discardResult.getSource();
                        forced = discardResult.isForced();
                        //TODO: split owner and performing player properly
                        performingPlayer = source.getOwner();
                        discardedCard = discardResult.getDiscardedCard();
                    }
                }
                else if(zone == Zone.HAND) {
                    result = TriggerConditions.forEachDiscardedFromHand(actionContext.getGame(),
                            actionContext.getEffectResult(), filterable);

                    if(result) {
                        var discardResult = (DiscardCardFromHandResult) actionContext.getEffectResult();
                        source = discardResult.getSource();
                        forced = discardResult.isForced();
                        //TODO: split owner and performing player properly
                        if(source != null) {
                            performingPlayer = source.getOwner();
                        }
                        discardedCard = discardResult.getDiscardedCard();
                    }
                }

                if(discardedCard == null)
                    return false;

                if (result && bySource != null) {
                    if (source == null || !Filters.accepts(actionContext.getGame(), bySource.getFilterable(actionContext), source))
                        result = false;
                }

                if (result && ignoreVoluntary && !forced) {
                    result = false;
                }

                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayer(actionContext)))
                        result = false;
                }

                if (result && memorize != null) {
                    actionContext.setCardMemory(memorize, discardedCard);
                }
                return result;
            }
        };
    }
}
