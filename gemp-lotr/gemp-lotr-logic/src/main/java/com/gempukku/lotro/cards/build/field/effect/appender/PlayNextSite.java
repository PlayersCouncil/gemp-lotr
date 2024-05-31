package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
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
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
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

                if (game.getFormat().isOrderedSites()) {
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
    }
}
