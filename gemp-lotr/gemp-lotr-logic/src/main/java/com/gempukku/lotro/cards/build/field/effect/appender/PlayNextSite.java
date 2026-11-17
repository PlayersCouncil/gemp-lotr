package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PlayNextSiteEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class PlayNextSite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        final DelayedAppender playNextSite = new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final int nextSiteNumber = game.getGameState().getCurrentSiteNumber() + 1;
                final PhysicalCard nextSite = game.getGameState().getSite(nextSiteNumber);
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final String playerId = actionContext.getPerformingPlayer();

                if (nextSiteNumber > 9 || nextSiteNumber < 1)
                    return false;

                if (nextSite != null && !game.getModifiersQuerying().canReplaceSite(game, actionContext.getPerformingPlayer(), nextSite))
                    return false;

                if (!game.getGameState().usesUnorderedSites(playerId)) {
                    Filter printedSiteNumber = new Filter() {
                        @Override
                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                            return physicalCard.getBlueprint().getSiteNumber() == nextSiteNumber;
                        }
                    };
                    return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), Filters.and(filterable, printedSiteNumber));
                } else {
                    return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), filterable);
                }
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                return new PlayNextSiteEffect(action, actionContext.getPerformingPlayer(), filterable) {
                    @Override
                    protected void sitePlayedCallback(PhysicalCard site) {
                        if (memorize != null) {
                            actionContext.setCardMemory(memorize, site);
                        }
                    }
                };
            }
        };

        // Taught for Q-0224 (IdleOffers): where no site can be played (on a numbered path, the player's own site of
        // the next number already lies there, or he has none; no site after 9; a next site that cannot be replaced)
        // the effect plays nothing (F-0496, Andúril 7_79).  On a Shadows-block path his own site lying next is
        // replaced (Q-0238), and that is still offered.
        return TaughtForecast.teach(playNextSite, actionContext ->
                playNextSite.isPlayableInFull(actionContext) ? EffectForecast.UNKNOWN : EffectForecast.NOTHING);
    }
}
