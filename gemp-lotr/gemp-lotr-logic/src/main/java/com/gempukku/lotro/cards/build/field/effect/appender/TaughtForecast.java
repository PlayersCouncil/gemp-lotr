package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.IdleOffers;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.Map;
import java.util.function.Function;

/**
 * For the rule of Q-0224 (IdleOffers), which withholds an offer that pays nothing and would do nothing: an effect
 * as it was, with one more thing it can say of itself.  The new forecast is given only while that rule is asking
 * (IdleOffers.isAskingTaught); to everything else, the no-effect flag and a prevention included, the effect
 * answers what it answered before.  Everything but the forecast is the wrapped effect's own.
 */
public final class TaughtForecast implements EffectAppender {
    private final EffectAppender effect;
    private final Function<ActionContext, EffectForecast> taught;

    private TaughtForecast(EffectAppender effect, Function<ActionContext, EffectForecast> taught) {
        this.effect = effect;
        this.taught = taught;
    }

    public static EffectAppender teach(EffectAppender effect, Function<ActionContext, EffectForecast> taught) {
        return new TaughtForecast(effect, taught);
    }

    /**
     * Teaches an effect that acts on the cards one of its card resolvers finds, and on nothing else: with none
     * found it does nothing.
     */
    public static EffectAppender actsOnWhatItFinds(EffectAppender effect, EffectAppender... cardResolvers) {
        return new TaughtForecast(effect, actionContext -> {
            for (EffectAppender cardResolver : cardResolvers) {
                final EffectForecast found = cardResolver.forecast(actionContext);
                if (found.getReach() != null && found.getReach().isEmpty())
                    return EffectForecast.NOTHING;
            }
            return EffectForecast.UNKNOWN;
        });
    }

    @Override
    public EffectForecast forecast(ActionContext actionContext) {
        if (IdleOffers.isAskingTaught())
            return taught.apply(actionContext);
        return effect.forecast(actionContext);
    }

    @Override
    public void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        effect.appendEffect(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return effect.isPlayableInFull(actionContext);
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return effect.isPlayabilityCheckedForEffect();
    }

    @Override
    public int getThreatsAddedAsCost(ActionContext actionContext) {
        return effect.getThreatsAddedAsCost(actionContext);
    }

    @Override
    public void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
        effect.tallyWoundsPlacedAsCost(actionContext, tally);
    }

    @Override
    public boolean canPreEvaluate() {
        return effect.canPreEvaluate();
    }
}
