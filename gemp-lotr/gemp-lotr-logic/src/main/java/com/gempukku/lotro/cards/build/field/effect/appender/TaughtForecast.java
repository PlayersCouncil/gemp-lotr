package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DelegateActionContext;
import com.gempukku.lotro.cards.build.PlayerSource;
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
 * (IdleOffers.isAskingTaught); to the no-effect flag and a prevention the effect answers what it answered before.
 * A choice the effect is a branch of hears it as well (isCertainToDoNothingIfChosen, engine pass 32): a branch
 * taught to say it will do nothing is no reason to put the choice to a player when no other branch would do
 * anything either (F-0116), so "to remove (2) or to draw a card" with an empty pool and an empty draw deck asks
 * nothing.  Not the whole-hand discard (forOffersOnly): a player always has a hand, and discarding none of it is
 * a real choice (Ketura on Q-0255).  Everything but the forecast is the wrapped effect's own.
 */
public final class TaughtForecast implements EffectAppender {
    private final EffectAppender effect;
    private final Function<ActionContext, EffectForecast> taught;
    private final boolean heardByAChoice;

    private TaughtForecast(EffectAppender effect, Function<ActionContext, EffectForecast> taught, boolean heardByAChoice) {
        this.effect = effect;
        this.taught = taught;
        this.heardByAChoice = heardByAChoice;
    }

    public static EffectAppender teach(EffectAppender effect, Function<ActionContext, EffectForecast> taught) {
        return new TaughtForecast(effect, taught, true);
    }

    /**
     * The same effect, with what it was taught told to the rule of Q-0224 alone and not to a choice it is a branch
     * of: a choice hears only what the effect said before it was taught.
     */
    public static EffectAppender forOffersOnly(EffectAppender taughtEffect) {
        if (taughtEffect instanceof TaughtForecast taughtForecast)
            return new TaughtForecast(taughtForecast.effect, taughtForecast.taught, false);
        return taughtEffect;
    }

    /**
     * Teaches an effect that acts on the cards one of its card resolvers finds, and on nothing else: with none
     * found it does nothing.
     */
    public static EffectAppender actsOnWhatItFinds(EffectAppender effect, EffectAppender... cardResolvers) {
        return teach(effect, actionContext -> {
            for (EffectAppender cardResolver : cardResolvers) {
                final EffectForecast found = cardResolver.forecast(actionContext);
                if (found.getReach() != null && found.getReach().isEmpty())
                    return EffectForecast.NOTHING;
            }
            return EffectForecast.UNKNOWN;
        });
    }

    /**
     * Teaches an effect that acts on cards chosen from a hand, and on nothing else.  With that hand empty it does
     * nothing.  With cards in it and none that the choice could take it does nothing either, but that is said
     * only of the hand of the player the offer would be made to: what another player holds is not to be learned
     * from whether an offer is made.
     */
    public static EffectAppender actsOnWhatItFindsInHand(EffectAppender effect, PlayerSource hand, EffectAppender cardResolver) {
        return teach(effect, actionContext -> {
            final String handPlayer = hand.getPlayer(actionContext);
            if (actionContext.getGame().getGameState().getHand(handPlayer).isEmpty())
                return EffectForecast.NOTHING;
            if (!handPlayer.equals(actionContext.getPerformingPlayer()))
                return EffectForecast.UNKNOWN;
            final EffectForecast found = cardResolver.forecast(actionContext);
            return found.getReach() != null && found.getReach().isEmpty() ? EffectForecast.NOTHING : EffectForecast.UNKNOWN;
        });
    }

    @Override
    public EffectForecast forecast(ActionContext actionContext) {
        if (IdleOffers.isAskingTaught())
            return taught.apply(actionContext);
        return effect.forecast(actionContext);
    }

    // What the effect said of itself, or what it was taught, asked as it is when the branch is about to be carried
    // out (EffectForecast.isCertainToDoNothing): nothing to do, and not bookkeeping.
    @Override
    public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
        if (effect.isCertainToDoNothingIfChosen(actionContext))
            return true;
        if (!heardByAChoice)
            return false;
        final ActionContext atResolution = new DelegateActionContext(actionContext, actionContext.getPerformingPlayer()) {
            @Override
            public boolean isAtResolution() {
                return true;
            }
        };
        try {
            final EffectForecast forecast = taught.apply(atResolution);
            return forecast.getVerdict() != EffectForecast.Verdict.UNKNOWN && !forecast.isBookkeeping();
        } catch (RuntimeException exp) {
            return false;
        }
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
    public boolean isPlayabilityCheckedAsCostOfEffect() {
        return effect.isPlayabilityCheckedAsCostOfEffect();
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
    public void tallyWoundsOfPayingOnce(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
        effect.tallyWoundsOfPayingOnce(actionContext, tally);
    }

    @Override
    public boolean canPreEvaluate() {
        return effect.canPreEvaluate();
    }
}
