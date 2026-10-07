package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

/**
 * How many cards matching the filter have been played this turn: PlayedCardThisPhase, read from the turn's
 * history in place of the phase's.  A play is in the history from the moment it is announced and is taken out
 * again if its card has left play by the time the play is complete (see PlayCardEffect).
 *
 * With atCurrentSite only the cards played while the fellowship was at the site it is at now are counted, for a
 * text that counts the cards played "at" a site in a turn ("the first Uruk-hai played at Deep of Helm each
 * turn"): a card played at the site the fellowship has since moved on from was not played here.
 */
public class PlayedCardThisTurn implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "min", "max", "atCurrentSite");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final ValueSource minSource = ValueResolver.resolveEvaluator(object.get("min"), 1, environment);
        final ValueSource maxSource = ValueResolver.resolveEvaluator(object.get("max"), 100, environment);
        final boolean atCurrentSite = FieldUtils.getBoolean(object.get("atCurrentSite"), "atCurrentSite", false);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            var filterable = Filters.changeToFilter(filterableSource.getFilterable(actionContext));
            int min = minSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            int total = 0;
            LotroGame game = actionContext.getGame();
            final PhysicalCard currentSite = game.getGameState().getCurrentSite();
            for (EffectResult effectResult : game.getActionsEnvironment().getTurnEffectResults()) {
                if (effectResult instanceof PlayCardResult playResult) {
                    if (atCurrentSite && playResult.getPlayedAtSite() != currentSite)
                        continue;
                    if (filterable.accepts(game, playResult.getPlayedCard())) {
                        ++total;
                    }
                }
            }

            return total >= min && total <= max;
        };
    }
}
