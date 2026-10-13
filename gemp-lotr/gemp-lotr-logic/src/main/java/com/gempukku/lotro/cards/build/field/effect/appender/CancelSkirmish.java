package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.CancelSkirmishEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class CancelSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "involving", "fierceOnly");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String involving = FieldUtils.getString(effectObject.get("involving"), "involving", "any");
        boolean fierceOnly = FieldUtils.getBoolean(effectObject.get("fierceOnly"), "fierceOnly", false);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource involvingSource = environment.getFilterFactory().generateFilter(involving, environment);
        final boolean filtersReadMemory = CardResolver.readsMemory(filter) || CardResolver.readsMemory(involving);
        final boolean namesSecondKind = effectObject.get("involving") != null;

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable first = filterableSource.getFilterable(actionContext);
                if (!fierceOnly || actionContext.getGame().getGameState().isFierceSkirmishes()) {
                    if (!namesSecondKind)
                        return new CancelSkirmishEffect(first);

                    // "A skirmish involving X and Y": the skirmish has to hold a card of each kind when the
                    // effect is carried out, as it has to when the effect is checked as a cost.
                    final Filterable second = involvingSource.getFilterable(actionContext);
                    return new CancelSkirmishEffect(first) {
                        @Override
                        public boolean isPlayableInFull(LotroGame game) {
                            return super.isPlayableInFull(game)
                                    && Filters.countActive(game, Filters.and(second, Filters.inSkirmish)) > 0;
                        }
                    };
                } else {
                    return null;
                }
            }

            // Nothing to cancel, or no leave to cancel it: nobody of the kind named is in the skirmish being
            // fought, the format keeps the Ring-bearer's skirmish from being cancelled, or a card forbids it.
            // A cancelled skirmish is never a matter of that skirmish only, so there is no more to say.
            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                if (filtersReadMemory && !actionContext.isAtResolution())
                    return EffectForecast.UNKNOWN;
                return isPlayableInFull(actionContext) ? EffectForecast.UNKNOWN : EffectForecast.NOTHING;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Filterable first = filterableSource.getFilterable(actionContext);
                final Filterable second = involvingSource.getFilterable(actionContext);
                final LotroGame game = actionContext.getGame();
                return game.getGameState().getSkirmish() != null
                        && (!fierceOnly || game.getGameState().isFierceSkirmishes())
                        && !game.getGameState().getSkirmish().isCancelled()
                        && (Filters.countActive(game, Filters.and(first, Filters.inSkirmish)) > 0)
                        && (Filters.countActive(game, Filters.and(second, Filters.inSkirmish)) > 0)
                        && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getSkirmish().getFellowshipCharacter())
                        && (game.getFormat().canCancelRingBearerSkirmish() || Filters.countActive(game, Filters.ringBearer, Filters.inSkirmish) == 0);
            }
        };
    }
}
