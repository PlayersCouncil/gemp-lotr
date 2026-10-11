package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.Map;

public interface EffectAppender extends PreEvaluateAble {
    void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext);

    boolean isPlayableInFull(ActionContext actionContext);

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }

    /**
     * The threats this appender will add when it is carried out as a cost, as far as can be told beforehand;
     * zero for everything that adds none, and for anything that only adds them under a condition or by choice.
     */
    default int getThreatsAddedAsCost(ActionContext actionContext) {
        return 0;
    }

    /**
     * The wounds this appender will place when it is carried out as a cost, as far as can be told before the
     * action is taken: exertions and wounds on cards it names outright (the card itself, its bearer, a memory
     * that is already filled) or cannot avoid (a choice with exactly as many candidates as must be chosen).
     * They are added to the tally, card by card.  Nothing is added where the player has a real choice of whom
     * to exert, or where the wounds depend on a condition.  The action's later costs, and any card it would then
     * play, are judged with these wounds in place (DefaultActionSource.isValid).
     */
    default void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
    }

    default boolean canPreEvaluate() {
        return true;
    }

    /**
     * What can be told of this effect before the action it belongs to is taken, for judging whether the action
     * will do nothing (the no-effect flag).  Asked while the action is only on offer: no cost has been paid and
     * nothing has been chosen.  It must change nothing, and in particular must never work out an amount, since
     * an amount with a limit spends the limit as it is worked out.  An effect that cannot tell says UNKNOWN.
     */
    default EffectForecast forecast(ActionContext actionContext) {
        return EffectForecast.UNKNOWN;
    }

    /**
     * Whether this effect, were it carried out now, is certain to do nothing.  A choice asks it of each of its
     * branches as the choice comes up, when none of them can be carried out in full (Choice): nobody is asked to
     * pick between things none of which will happen.
     *
     * An effect answers for itself by its forecast, asked as it is when the effect is about to be carried out
     * (EffectForecast.isCertainToDoNothing).  An effect that holds others answers from what it holds: an "if" by
     * the side its check picks, a cost and what it buys by whether the cost can be paid, several effects by each
     * of them in turn, a play by whether there is a card to play.  One that holds others and has not been taught
     * to say (a "you may", a repeat, a prevention) answers false, and the choice it is a branch of is asked.
     */
    default boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
        return EffectForecast.isCertainToDoNothing(actionContext, new EffectAppender[]{this});
    }
}
