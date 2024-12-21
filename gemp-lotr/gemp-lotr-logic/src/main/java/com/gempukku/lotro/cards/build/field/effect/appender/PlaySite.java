package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class PlaySite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "block", "filter", "number", "memorize", "player");

        final String blockString = FieldUtils.getString(effectObject.get("block"), "block");
        final SitesBlock block = blockString != null ? SitesBlock.findBlock(blockString) : null;
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("number"), environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final int siteNumber = valueSource.getEvaluator(actionContext).evaluateExpression(game, null);
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final String playerId = playerSource.getPlayer(actionContext);

                if (siteNumber > 9 || siteNumber < 1)
                    return false;

                if (game.getFormat().isOrderedSites()) {
                    Filter printedSiteNumber = new Filter() {
                        @Override
                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                            return physicalCard.getBlueprint().getSiteNumber() == siteNumber;
                        }
                    };
                    if (block != null)
                        return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), Filters.and(filterable, printedSiteNumber, Filters.siteBlock(block)));
                    else
                        return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), Filters.and(filterable, printedSiteNumber));
                } else {
                    if (block != null)
                        return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), Filters.and(filterable, Filters.siteBlock(block)));
                    else
                        return Filters.acceptsAny(game, game.getGameState().getAdventureDeck(playerId), filterable);
                }
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final int siteNumber = valueSource.getEvaluator(actionContext).evaluateExpression(game, null);
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final String playerId = playerSource.getPlayer(actionContext);
                return new PlaySiteEffect(action, playerId, block, siteNumber, filterable) {
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
