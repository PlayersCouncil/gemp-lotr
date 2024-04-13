package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.CancelSkirmishEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class CancelSkirmishBetween implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "fp", "shadow");

        final String fpFilter = FieldUtils.getString(effectObject.get("fp"), "fp", "any");
        final String shadowFilter = FieldUtils.getString(effectObject.get("shadow"), "shadow", "any");

        final FilterableSource fpFilterableSource = environment.getFilterFactory().generateFilter(shadowFilter, environment);
        final FilterableSource shadowFilterableSource = environment.getFilterFactory().generateFilter(shadowFilter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable involvingFp = fpFilterableSource.getFilterable(actionContext);
                final Filterable involvingShadow = shadowFilterableSource.getFilterable(actionContext);
                final LotroGame game = actionContext.getGame();
                if ((Filters.countActive(game, Filters.and(Side.FREE_PEOPLE, involvingFp, Filters.inSkirmish)) > 0)
                        && (Filters.countActive(game, Filters.and(Side.SHADOW, involvingShadow, Filters.inSkirmish)) > 0)) {
                    return new CancelSkirmishEffect(involvingFp);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Filterable involvingFp = fpFilterableSource.getFilterable(actionContext);
                final Filterable involvingShadow = shadowFilterableSource.getFilterable(actionContext);
                final LotroGame game = actionContext.getGame();
                return game.getGameState().getSkirmish() != null
                        && !game.getGameState().getSkirmish().isCancelled()
                        && (Filters.countActive(game, Filters.and(Side.FREE_PEOPLE, involvingFp, Filters.inSkirmish)) > 0)
                        && (Filters.countActive(game, Filters.and(Side.SHADOW, involvingShadow, Filters.inSkirmish)) > 0)
                        && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getSkirmish().getFellowshipCharacter())
                        && (game.getFormat().canCancelRingBearerSkirmish() || Filters.countActive(game, Filters.ringBearer, Filters.inSkirmish) == 0);
            }
        };
    }
}
